#!/usr/bin/env python3
import argparse
import subprocess
import os
import sys
import shutil
import threading
import time
import re
from pathlib import Path
from datetime import datetime

# --- Configuration ---
APP_VERSION = "1.4.0"
CONFIG_FILES = ["local.properties", ".env", "google-services.json"]
ANDROID_PACKAGE = "io.github.smithjustinn.androidApp"
ANDROID_ACTIVITY = ".AppActivity"

# --- UI / Colors ---
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    PURPLE = '\033[0;35m'
    CYAN = '\033[0;36m'
    GRAY = '\033[0;90m'
    BOLD = '\033[1m'
    ITALIC = '\033[3m'
    NC = '\033[0m'

ASCII_ART = fr"""{Colors.CYAN}
   __  __                                __  __       _       _     
  |  \/  | ___ _ __ ___   ___  _ __ _   _|  \/  | __ _| |_ ___| |__  
  | |\/| |/ _ \ '_ ` _ \ / _ \| '__| | | | |\/| |/ _` | __/ __| '_ \ 
  | |  | |  __/ | | | | | (_) | |  | |_| | |  | | (_| | || (__| | | |
  |_|  |_|\___|_| |_| |_|\___/|_|   \__, |_|  |_|\__,_|\__\___|_| |_|
                                     |___/                           {Colors.NC}"""

class Spinner:
    def __init__(self, message="Working..."):
        self.message = message
        self.stop_event = threading.Event()
        self.thread = threading.Thread(target=self._spin)

    def _spin(self):
        chars = "⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏"
        i = 0
        while not self.stop_event.is_set():
            sys.stdout.write(f"\r{Colors.BLUE}{chars[i % len(chars)]}{Colors.NC} {self.message}")
            sys.stdout.flush()
            time.sleep(0.1)
            i += 1
        sys.stdout.write("\r" + " " * (len(self.message) + 2) + "\r")
        sys.stdout.flush()

    def __enter__(self):
        if sys.stdout.isatty():
            self.thread.start()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if sys.stdout.isatty():
            self.stop_event.set()
            self.thread.join()

# --- Utility Functions ---

def validate_task_name(name):
    if not re.match(r'^[a-zA-Z0-9_-]+$', name):
        error(f"Invalid task name '{name}'. Only alphanumeric characters, hyphens, and underscores are allowed.")
    return name

def log(msg, color=Colors.BLUE):
    print(f"{Colors.GRAY}•{Colors.NC} {color}{Colors.BOLD}{msg}{Colors.NC}")

def success(msg):
    print(f"{Colors.GREEN}✔{Colors.NC} {Colors.GREEN}{Colors.BOLD}{msg}{Colors.NC}")

def warn(msg):
    print(f"{Colors.YELLOW}⚠{Colors.NC} {Colors.YELLOW}{Colors.BOLD}{msg}{Colors.NC}")

def error(msg):
    print(f"{Colors.RED}✘{Colors.NC} {Colors.RED}{Colors.BOLD}{msg}{Colors.NC}")
    sys.exit(1)

def check_command(cmd):
    return shutil.which(cmd) is not None

def run_command(cmd, cwd=None, capture_output=False, shell=False, show_spinner=False, spinner_msg="Running..."):
    if show_spinner:
        with Spinner(spinner_msg):
            return _run_command(cmd, cwd, capture_output, shell)
    return _run_command(cmd, cwd, capture_output, shell)

def _run_command(cmd, cwd=None, capture_output=False, shell=False):
    try:
        result = subprocess.run(
            cmd, 
            cwd=cwd, 
            check=True, 
            text=True, 
            capture_output=capture_output, 
            shell=shell
        )
        return result
    except subprocess.CalledProcessError as e:
        if not capture_output:
            error(f"Command failed: {' '.join(cmd) if isinstance(cmd, list) else cmd}")
        return e

def run_gradle(tasks, params=None, show_spinner=True, msg="Gradle executing..."):
    cmd = ["./gradlew"] + tasks
    if params:
        cmd.extend(params)
    return run_command(cmd, show_spinner=show_spinner, spinner_msg=msg)

def jj(*args, capture_output=False, cwd=None):
    """Runs a Jujutsu command. Version control here goes through jj, not git."""
    return run_command(["jj", "--no-pager", *args], cwd=cwd, capture_output=capture_output)

def jj_lines(*args, cwd=None):
    """Runs a jj command and returns its non-empty output lines (empty if it failed)."""
    res = jj(*args, capture_output=True, cwd=cwd)
    if res.returncode != 0:
        return []
    return [line for line in (res.stdout or "").splitlines() if line.strip()]

def get_project_root():
    try:
        res = run_command(["jj", "--no-pager", "workspace", "root"], capture_output=True)
        if res.returncode == 0 and res.stdout.strip():
            return Path(res.stdout.strip())
    except Exception:
        pass
    return Path(os.getcwd())

PROJECT_ROOT = get_project_root()
WORKSPACES_DIR = PROJECT_ROOT.parent / "worktrees"

WORKSPACE_TEMPLATE = (
    'name ++ "\\t" ++ if(current_workspace, "1", "0") ++ "\\t" ++ root ++ "\\t" ++ '
    'target.change_id().shortest(8) ++ "\\t" ++ target.description().first_line() ++ "\\n"'
)

def list_workspaces():
    """Returns every jj workspace as a dict of name, current, path, change and description."""
    workspaces = []
    for line in jj_lines("workspace", "list", "-T", WORKSPACE_TEMPLATE):
        name, current, root, change, desc = (line.split("\t") + [""] * 5)[:5]
        is_current = current == "1"
        # jj's recorded root goes stale if the folder is moved, so trust the live one for this workspace.
        path = PROJECT_ROOT if is_current else Path(root)
        workspaces.append({"name": name, "current": is_current, "path": path, "change": change, "desc": desc})
    return workspaces

def other_workspaces():
    return [ws for ws in list_workspaces() if not ws["current"]]

def prune_git_worktrees():
    """jj backs each colocated workspace with a Git worktree but has no command to drop a stale record."""
    if check_command("git"):
        run_command(["git", "worktree", "prune"], cwd=PROJECT_ROOT, capture_output=True)

def nearest_bookmarks():
    """Bookmarks on the working-copy change or its closest bookmarked ancestors."""
    return jj_lines(
        "log", "--no-graph", "-r", "heads(::@ & bookmarks())",
        "-T", 'local_bookmarks.map(|b| b.name()).join("\\n") ++ "\\n"',
    )

def working_copy_changes():
    """Files changed in the working-copy change, as (mode, path) pairs."""
    return [(line[0], line[2:]) for line in jj_lines("diff", "--summary", "-r", "@")]

def safe_path_join(base, *paths):
    try:
        # Resolve the base directory to an absolute path
        base_resolved = base.resolve()

        # Join the paths and resolve the final path
        final_path = base.joinpath(*paths).resolve()

        # Check if the final path is within the base directory
        # We check if base_resolved is a parent of final_path
        # OR if they are the same directory (though unlikely for a task)
        if base_resolved != final_path and base_resolved not in final_path.parents:
            error(f"Security Error: Path traversal detected. Target '{final_path}' is outside '{base_resolved}'.")

        return final_path
    except Exception as e:
        error(f"Invalid path: {e}")

# --- Command Handlers ---

def handle_build(args):
    target = args.target
    gradle_args = args.params or []
    
    if args.release:
        gradle_args.append("-Prelease=true")
    
    tasks = []
    if args.clean:
        tasks.append("clean")

    log(f"Building {target}...")
    
    if target == "android":
        task = ":androidApp:assembleRelease" if args.release else ":androidApp:assembleDebug"
        if args.run:
            task = ":androidApp:installDebug"
        
        tasks.append(task)
        run_gradle(tasks, params=gradle_args, msg=f"Building Android ({'Release' if args.release else 'Debug'})...")
        
        if args.run:
            log("Launching Android app...")
            run_command(["adb", "shell", "am", "start", "-n", f"{ANDROID_PACKAGE}/{ANDROID_PACKAGE}{ANDROID_ACTIVITY}"])
            if args.log:
                handle_android_logs(args)
                
    elif target == "ios":
        if sys.platform != "darwin":
            error("iOS builds require macOS.")
        config = "Release" if args.release else "Debug"
        log(f"Building iOS for Simulator ({config})...")
        run_command([
            "xcodebuild", "-project", "iosApp/iosApp.xcodeproj", 
            "-scheme", "iosApp", "-sdk", "iphonesimulator", 
            "-configuration", config,
            # The Kotlin targets are arm64-only (no iosX64), so don't let Xcode ask for x86_64.
            "ARCHS=arm64", "build"
        ], show_spinner=True, spinner_msg=f"Building iOS ({config})...")
        
    elif target == "desktop":
        if args.hot:
            # Compose Hot Reload: recompiles and reloads the running app when sources change.
            tasks.extend([":desktopApp:hotRun", "--auto"])
        else:
            tasks.append(":desktopApp:run" if args.run else ":desktopApp:assemble")
        run_gradle(tasks, params=gradle_args, msg=f"Building Desktop...")
        
    elif target == "metadata":
        run_gradle([":sharedUI:compileCommonMainKotlinMetadata"], params=gradle_args, msg="Compiling KMP Metadata...")

def handle_android_logs(args):
    log("Streaming Android logs...")
    pkg = ANDROID_PACKAGE
    try:
        pid_res = run_command(["adb", "shell", "pidof", "-s", pkg], capture_output=True)
        pid = pid_res.stdout.strip()
        cmd = ["adb", "logcat", "-v", "color"]
        if pid:
            cmd.append(f"--pid={pid}")
        if args.filter:
            cmd.extend([f"{args.filter}:V", "*:S"])
        subprocess.run(cmd) # Streaming
    except KeyboardInterrupt:
        pass

def handle_test(args):
    log(f"Running tests for module: {args.module}...")
    tasks = []
    if args.module == "all":
        tasks = [":sharedUI:allTests", ":androidApp:testDebugUnitTest", ":desktopApp:test"]
    elif args.module == "shared":
        tasks = [":sharedUI:allTests"]
    elif args.module == "android":
        tasks = [":androidApp:testDebugUnitTest"]
    elif args.module == "desktop":
        tasks = [":desktopApp:test"]
    
    run_gradle(tasks, msg=f"Executing {len(tasks)} test suites...")
    success("Tests completed successfully.")

def handle_task(args):
    sub = args.subcommand
    if sub == "new":
        task_type = args.type
        name = validate_task_name(args.name)
        branch = f"{task_type}/{name}"
        target_dir = safe_path_join(WORKSPACES_DIR, name)
        
        if target_dir.exists():
            error(f"Workspace directory already exists: {target_dir}")
        if any(ws["name"] == name for ws in list_workspaces()):
            error(f"A jj workspace named '{name}' already exists. Run: ./mm.py task remove {name}")
        
        log(f"Creating workspace for '{branch}'...")
        WORKSPACES_DIR.mkdir(exist_ok=True)
        jj("workspace", "add", str(target_dir), "--name", name)
        # The bookmark labels the task; `mm done` reads it to suggest a commit message.
        jj("bookmark", "create", branch, "-r", "@", cwd=target_dir)
        
        # Sync configs
        for f in CONFIG_FILES:
            src = PROJECT_ROOT / f
            if src.exists():
                shutil.copy(src, target_dir / f)
        
        success(f"Workspace ready: {target_dir}")
        print(f"Tip: cd {target_dir}")
        
    elif sub == "list":
        jj("workspace", "list")

    elif sub == "locate":
        name = args.name
        path = next((ws["path"] for ws in list_workspaces() if ws["name"] == name), None)
        if path:
            print(path)
        else:
            error(f"Workspace '{name}' not found.")

    elif sub == "clean":
        log("Forgetting workspaces whose directory is gone...")
        for ws in other_workspaces():
            if not ws["path"].exists():
                log(f"Forgetting {ws['name']}...")
                jj("workspace", "forget", ws["name"])
        prune_git_worktrees()
        success("Cleanup complete.")
        
    elif sub == "remove":
        name = args.name
        if not name:
            worktrees = [ws["name"] for ws in other_workspaces()]
            
            if not worktrees:
                log("No additional workspaces found.")
                return
                
            print(f"\n{Colors.BOLD}Select workspace to remove:{Colors.NC}")
            for i, wt in enumerate(worktrees):
                print(f"  {i+1}) {wt}")
            
            try:
                choice = input(f"\nSelection (1-{len(worktrees)}, or 'c' to cancel): ")
                if choice.lower() == 'c': return
                idx = int(choice) - 1
                name = worktrees[idx]
            except (ValueError, IndexError):
                error("Invalid selection.")

        validate_task_name(name)
        target_dir = safe_path_join(WORKSPACES_DIR, name)
        if str(target_dir) in os.getcwd():
            error("Cannot remove the current workspace.")
        
        log(f"Removing workspace {name}...")
        registered = any(ws["name"] == name and not ws["current"] for ws in list_workspaces())
        if target_dir.exists():
            if registered:
                # Any jj command snapshots the directory, so unsaved edits become a change before it goes.
                jj("status", capture_output=True, cwd=target_dir)
            shutil.rmtree(target_dir)
        if registered:
            jj("workspace", "forget", name)
        prune_git_worktrees()
        
        # Find and remove the task bookmark (the listing repeats a name once per remote)
        for b in sorted(set(jj_lines("bookmark", "list", "-T", 'name ++ "\\n"'))):
            if b.endswith(f"/{name}"):
                jj("bookmark", "delete", b)
        success(f"Workspace '{name}' and its bookmark removed.")
        print("Its changes are still in `jj log`; drop any you don't want with `jj abandon`.")

def handle_sync(args):
    log("Syncing config files...")
    for ws in other_workspaces():
        path = ws["path"]
        if not path.exists():
            warn(f"Skipping {ws['name']}: {path} is missing (run: ./mm.py task clean)")
            continue
        log(f"Syncing to {path.name}...")
        for f in CONFIG_FILES:
            src = PROJECT_ROOT / f
            if src.exists():
                shutil.copy(src, path / f)
    success("All workspaces synchronized.")

def handle_context(args):
    log("Generating AI Context Summary...")
    change = "".join(jj_lines("log", "--no-graph", "-r", "@", "-T", 'change_id.shortest(8) ++ "\\n"'))
    bookmarks = ", ".join(nearest_bookmarks()) or "none"
    changes = working_copy_changes()
    modified_files = [file for _, file in changes]
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- AI CONTEXT SUMMARY ---{Colors.NC}")
    print(f"{Colors.BOLD}Current Change:{Colors.NC} {Colors.PURPLE}{change}{Colors.NC}")
    print(f"{Colors.BOLD}Nearest Bookmark:{Colors.NC} {Colors.PURPLE}{bookmarks}{Colors.NC}")
    print(f"{Colors.BOLD}Status:{Colors.NC} {Colors.YELLOW}{len(modified_files)}{Colors.NC} files changed in the working copy")
    
    # Coverage Info
    report_path = PROJECT_ROOT / "docs/KOVER_COVERAGE_SUMMARY.md"
    if report_path.exists():
        with open(report_path, "r") as f:
            for line in f:
                if "Mission Accomplished" in line or "Line Coverage" in line:
                    coverage_text = line.strip().replace('#', '').strip()
                    print(f"{Colors.BOLD}Coverage:{Colors.NC} {Colors.GREEN}{coverage_text}{Colors.NC}")
                    break

    print(f"\n{Colors.CYAN}{Colors.BOLD}Impacted Modules:{Colors.NC}")
    mapping = {
        "shared/core/": f"{Colors.BLUE}[CORE]{Colors.NC} Business Logic & Models",
        "shared/data/": f"{Colors.BLUE}[DATA]{Colors.NC} Persistence & Network",
        "sharedUI/": f"{Colors.BLUE}[UI]{Colors.NC} Compose Components",
        "androidApp/": f"{Colors.BLUE}[PLATFORM]{Colors.NC} Android",
        "desktopApp/": f"{Colors.BLUE}[PLATFORM]{Colors.NC} Desktop",
        "mm.py": f"{Colors.BLUE}[TOOLING]{Colors.NC} CLI Manager"
    }
    impacted = set()
    for f in modified_files:
        for path, label in mapping.items():
            if f.startswith(path):
                impacted.add(label)
    
    for label in sorted(impacted):
        print(f" {Colors.GRAY}•{Colors.NC} {label}")
            
    print(f"\n{Colors.CYAN}{Colors.BOLD}Recent Changes:{Colors.NC}")
    if changes:
        for mode, file in changes:
            color = Colors.GREEN if mode == 'A' else Colors.YELLOW
            print(f" {color}{mode}{Colors.NC} {file}")
    else:
        print(f" {Colors.GRAY}The working-copy change is empty.{Colors.NC}")
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}Recent History (Last 3 Changes):{Colors.NC}")
    history = jj_lines(
        "log", "--no-graph", "-n", "3", "-r", "::@-",
        "-T", 'change_id.shortest(8) ++ " " ++ description.first_line() ++ "\\n"',
    )
    for line in history:
        change_id, _, msg = line.partition(' ')
        print(f" {Colors.YELLOW}{change_id}{Colors.NC} {msg or '(no description set)'}")
    print(f"{Colors.CYAN}--------------------------{Colors.NC}\n")

def handle_done(args):
    log("Finalizing task...")
    log("Running verification...")
    run_gradle(["spotlessApply", "detekt"], msg="Verifying and formatting...")
    
    print(f"\n{Colors.BOLD}Final Status:{Colors.NC}")
    jj("status")
    
    # A task bookmark looks like "feat/scoreboard"; plain ones such as "main" carry no task.
    branch = next((b for b in nearest_bookmarks() if "/" in b), "")
    if branch:
        task_type, desc = branch.split("/", 1)
        desc = desc.replace("-", " ")
        success(f"Suggested command: jj commit -m \"{task_type}: {desc}\"")
    else:
        success("Suggested command: jj commit -m \"feat: description\"")

def handle_doctor(args):
    log("Checking environment health...")
    checks = [
        ("JDK", ["java", "-version"]),
        ("jj", ["jj", "--version"]),
        ("Git", ["git", "--version"]),
        ("ADB", ["adb", "version"]),
        ("Gradle", ["./gradlew", "--version"]),
    ]
    if sys.platform == "darwin":
        checks.append(("Xcode", ["xcodebuild", "-version"]))

    all_passed = True
    for name, cmd in checks:
        if check_command(cmd[0]):
            try:
                res = subprocess.run(cmd, capture_output=True, text=True)
                lines = (res.stdout or res.stderr).splitlines()
                ver = "Unknown"
                for line in lines:
                    line = line.strip()
                    if line and not line.startswith("-") and not line.startswith("("):
                        ver = line
                        break
                print(f" {Colors.GREEN}✓{Colors.NC} {name:8} : {ver}")
            except Exception:
                print(f" {Colors.YELLOW}?{Colors.NC} {name:8} : Found but could not get version")
        else:
            print(f" {Colors.RED}✗{Colors.NC} {name:8} : Not found")
            all_passed = False

    if all_passed:
        success("Environment looks good!")
    else:
        warn("Some dependencies are missing. Please check your PATH.")

def handle_lint(args):
    tasks = []
    if args.fix:
        log("Applying lint fixes...")
        tasks = ["spotlessApply", "detekt"]
    else:
        log("Checking linting...")
        tasks = ["spotlessCheck", "detekt"]
    
    run_gradle(tasks, msg="Linting...")
    success("Linting complete.")

def handle_clean(args):
    log("Cleaning project...")
    run_gradle(["clean"], msg="Gradle cleaning...")
    success("Project cleaned.")

def handle_open(args):
    if args.target == "android":
        log("Opening in Android Studio...")
        if sys.platform == "darwin":
            run_command(["open", "-a", "Android Studio", "."])
        else:
            run_command(["studio", "."])
    elif args.target == "ios":
        if sys.platform != "darwin":
            error("iOS targets require macOS.")
        log("Opening in Xcode...")
        run_command(["open", "iosApp/iosApp.xcodeproj"])

def handle_coverage(args):
    log("Generating coverage report...")
    run_gradle(["koverHtmlReport"], msg="Generating Kover reports...")
    report_path = PROJECT_ROOT / "build/reports/kover/html/index.html"
    if report_path.exists():
        success(f"Report generated: {report_path}")
        if args.open:
            if sys.platform == "darwin":
                run_command(["open", str(report_path)])
            else:
                log(f"View report at: file://{report_path}")
    else:
        error("Coverage report not found. Ensure Kover is correctly configured.")

def handle_status(args):
    log("Project Status Overview")
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- JJ STATUS ---{Colors.NC}")
    changes = working_copy_changes()
    if changes:
        for mode, file in changes:
            color = Colors.GREEN if mode == 'A' else Colors.YELLOW
            print(f" {color}{mode}{Colors.NC} {file}")
    else:
        print(f" {Colors.GREEN}Clean (the working-copy change is empty){Colors.NC}")
    
    print(f"\n{Colors.CYAN}{Colors.BOLD}--- ACTIVE WORKSPACES ---{Colors.NC}")
    for ws in list_workspaces():
        marker = "*" if ws["current"] else " "
        desc = ws["desc"] or "(no description set)"
        print(f" {marker} {Colors.BLUE}{str(ws['path']):40}{Colors.NC} {Colors.YELLOW}{ws['change']}{Colors.NC} {Colors.PURPLE}{ws['name']}{Colors.NC} {Colors.GRAY}{desc}{Colors.NC}")

# --- Main ---

def main():
    parser = argparse.ArgumentParser(
        description=f"Memory-Match CLI v{APP_VERSION}",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=f"""
{Colors.BOLD}Examples:{Colors.NC}
  {Colors.GREEN}mm build android --run{Colors.NC}      Build and run Android app
  {Colors.GREEN}mm task new feat scoreboard{Colors.NC}   Create a new task workspace
  {Colors.GREEN}mm doctor{Colors.NC}                     Check environment health
  {Colors.GREEN}mm context{Colors.NC}                    Generate AI summary
"""
    )
    parser.add_argument("--no-banner", action="store_true", help="Do not display the ASCII art banner")
    subparsers = parser.add_subparsers(dest="command", help="Available commands")
    
    # ... (rest of subparsers remain the same)
    build_p = subparsers.add_parser("build", help="Build the project for various platforms")
    build_p.add_argument("target", choices=["android", "ios", "desktop", "metadata"], help="Target platform")
    build_p.add_argument("--run", action="store_true", help="Launch the app after build")
    build_p.add_argument("--hot", action="store_true", help="Launch with Compose Hot Reload (desktop only)")
    build_p.add_argument("--release", action="store_true", help="Build in release mode")
    build_p.add_argument("--clean", action="store_true", help="Run clean before build")
    build_p.add_argument("--log", action="store_true", help="Stream logs (Android only)")
    build_p.add_argument("--filter", help="Filter logs by tag (Android only)")
    build_p.add_argument("params", nargs="*", help="Extra Gradle parameters")
    
    # Test
    test_p = subparsers.add_parser("test", help="Run project unit tests")
    test_p.add_argument("module", choices=["all", "shared", "android", "desktop"], default="all", nargs="?", 
                        help="Module to test (default: all)")
    
    # Task (jj workspace)
    task_p = subparsers.add_parser("task", help="Manage development tasks using jj workspaces")
    task_sub = task_p.add_subparsers(dest="subcommand", required=True)
    
    new_p = task_sub.add_parser("new", help="Create a new workspace for a feature or fix")
    new_p.add_argument("type", choices=["feat", "fix", "refactor", "chore"], help="Task category")
    new_p.add_argument("name", help="Task name (used for the bookmark, workspace and directory)")
    
    task_sub.add_parser("list", help="List all active task workspaces")
    
    locate_p = task_sub.add_parser("locate", help="Find the absolute path of a workspace")
    locate_p.add_argument("name", help="Task name to locate")
    
    task_sub.add_parser("clean", help="Forget workspaces whose directory is gone")
    
    remove_p = task_sub.add_parser("remove", help="Delete a workspace and its bookmark (its changes stay in jj log)")
    remove_p.add_argument("name", nargs="?", help="Task name to remove (interactive if omitted)")
    
    # Sync
    subparsers.add_parser("sync", help="Synchronize config files (.env, local.properties) to all workspaces")
    
    # Context
    subparsers.add_parser("context", help="Generate high-signal summary for AI agents")
    
    # Done
    subparsers.add_parser("done", help="Verify task, format code, and prepare commit")

    # Doctor
    subparsers.add_parser("doctor", help="Check development environment for missing dependencies")

    # Lint
    lint_p = subparsers.add_parser("lint", help="Run linting and code style checks")
    lint_p.add_argument("--fix", action="store_true", help="Automatically fix style issues")

    # Clean
    subparsers.add_parser("clean", help="Run gradle clean")

    # Open
    open_p = subparsers.add_parser("open", help="Open project in IDE")
    open_p.add_argument("target", choices=["android", "ios"], help="Target IDE (Android Studio or Xcode)")

    # Coverage
    cov_p = subparsers.add_parser("coverage", help="Generate and view code coverage reports")
    cov_p.add_argument("--open", action="store_true", help="Open HTML report in browser")

    # Status
    subparsers.add_parser("status", help="Show unified project and workspace status")

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit(0)
        
    args = parser.parse_args()
    
    if not args.no_banner and args.command != "locate":
        print(ASCII_ART)
        print(f"  {Colors.BOLD}Memory-Match CLI{Colors.NC} {Colors.GRAY}v{APP_VERSION}{Colors.NC}\n")

    if args.command == "build": handle_build(args)
    elif args.command == "test": handle_test(args)
    elif args.command == "task": handle_task(args)
    elif args.command == "sync": handle_sync(args)
    elif args.command == "context": handle_context(args)
    elif args.command == "done": handle_done(args)
    elif args.command == "doctor": handle_doctor(args)
    elif args.command == "lint": handle_lint(args)
    elif args.command == "clean": handle_clean(args)
    elif args.command == "open": handle_open(args)
    elif args.command == "coverage": handle_coverage(args)
    elif args.command == "status": handle_status(args)

if __name__ == "__main__":
    main()
