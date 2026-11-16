package io.github.smithjustinn.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

/**
 * iOS-specific concrete dependency graph.
 * Extends AppGraph and all provider interfaces so Metro can compile the full graph.
 */
@DependencyGraph(AppScope::class)
interface IosAppGraph :
    AppGraph,
    CoreProviders,
    DataProviders,
    IosUiModule

/**
 * Creates the iOS dependency graph using Metro's compile-time graph creator.
 * The graph takes no runtime inputs, so no @DependencyGraph.Factory is needed.
 */
fun createIosGraph(): AppGraph = createGraph<IosAppGraph>()
