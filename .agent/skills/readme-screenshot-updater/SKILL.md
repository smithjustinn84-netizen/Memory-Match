---
name: readme-screenshot-updater
description: Automates updating the README.md file with the latest screenshots from the screenshots/ directory.
---

# README Screenshot Updater

This skill automates the process of updating the project's `README.md` file to reference the most recent screenshots found in the `screenshots/` directory.

## when to use
- When the user asks to "update screenshots" or "refresh readme images".
- After generating new screenshots for the project.
- When `README.md` references missing or outdated image files.

## Instructions

1.  **Locate Screenshots**:
    - List all files in the `screenshots/` directory.
    - Filter the list to include only valid image formats (e.g., `.png`, `.jpg`, `.jpeg`, `.gif`).
    - Sort the images if necessary (e.g., alphabetically or by modification time) to promote a consistent order.

2.  **Verify README Content**:
    - Read the current `README.md` file.
    - Locate the section containing the screenshots (usually a grid of `<img>` tags or markdown image links).

3.  **Update References**:
    - Construct new HTML `<img>` tags or markdown links for the identified screenshots.
    - Replace the old image references in `README.md` with the new ones.
    - **Important**: Try to maintain the existing layout structure (e.g., if images are in a `<p align="center">` block or a table).

4.  **Verification**:
    - Verify that the path to the images is relative and correct (e.g., `screenshots/MyImage.png`).
    - Confirm the changes to `README.md` look correct.

## Example

If `screenshots/` contains:
- `home.png`
- `details.png`

And `README.md` has:
```html
<p>
  <img src="screenshots/old_1.png">
</p>
```

Update it to:
```html
<p>
  <img src="screenshots/home.png">
  <img src="screenshots/details.png">
</p>
```
