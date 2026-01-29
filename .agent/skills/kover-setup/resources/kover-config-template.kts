kover {
    reports {
        filters {
            excludes {
                // UI Packages - Pure presentation code
                packages(
                    "io.github.smithjustinn.ui",
                    "io.github.smithjustinn.ui.*",
                    "io.github.smithjustinn.ui.**",
                    "io.github.smithjustinn.theme",
                    "io.github.smithjustinn.theme.*",
                    "io.github.smithjustinn.services.*",
                    "io.github.smithjustinn.di.*"
                )
                
                // Annotation-based exclusions (Best Practice for Compose)
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.runtime.Composable"
                )
                
                // Generated and framework classes
                classes(
                    "*Generated*",
                    "*_Factory",
                    "*_Impl",
                    "*_Module",
                    "*.di.*",
                    "*Koin*",
                    "Res",
                    "Res$*",
                    "*.AppKt",
                    "*.ComposableSingletons*",
                    "*ComponentScopeKt*",
                    "*Dao_Impl*",
                    "*Database_Impl*",
                    "*Test*Util*",
                    "*TestHelper*",
                    "*Fake*",
                    "*Mock*",
                    "*PlatformUtils*"
                )
            }
        }
        
        total {
            xml { onCheck = true }
            html { onCheck = true }
            verify {
                rule("Minimum coverage") {
                    minBound(80) 
                }
            }
        }
    }
}
