package io.github.smithjustinn.domain.models

import io.github.smithjustinn.utils.ImmutableListSerializer
import kotlinx.serialization.builtins.serializer

// Type-specific serializers for ImmutableList
object CardStateListSerializer : ImmutableListSerializer<CardState>(CardState.serializer())

object IntListSerializer : ImmutableListSerializer<Int>(Int.serializer())
