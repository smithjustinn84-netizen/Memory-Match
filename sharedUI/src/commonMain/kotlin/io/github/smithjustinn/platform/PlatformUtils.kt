package io.github.smithjustinn.platform

/**
 * A platform-agnostic interface for serialization.
 * Maps to java.io.Serializable on Android.
 */
expect interface JavaSerializable

@Target(AnnotationTarget.FIELD)
expect annotation class CommonTransient()