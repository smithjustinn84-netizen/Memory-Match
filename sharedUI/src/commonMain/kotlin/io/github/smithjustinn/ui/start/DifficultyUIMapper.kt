package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.difficulty_casual
import io.github.smithjustinn.resources.difficulty_master
import io.github.smithjustinn.resources.difficulty_shark
import io.github.smithjustinn.resources.difficulty_tourist
import org.jetbrains.compose.resources.StringResource

val DifficultyType.displayNameRes: StringResource
    get() =
        when (this) {
            DifficultyType.TOURIST -> Res.string.difficulty_tourist
            DifficultyType.CASUAL -> Res.string.difficulty_casual
            DifficultyType.MASTER -> Res.string.difficulty_master
            DifficultyType.SHARK -> Res.string.difficulty_shark
        }
