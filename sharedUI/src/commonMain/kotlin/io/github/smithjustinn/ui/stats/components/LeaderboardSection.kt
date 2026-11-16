package io.github.smithjustinn.ui.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.no_stats_yet
import io.github.smithjustinn.resources.pairs_format
import io.github.smithjustinn.resources.score_label
import io.github.smithjustinn.resources.stats_moves_header
import io.github.smithjustinn.resources.stats_time_header
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.start.displayNameRes
import io.github.smithjustinn.utils.formatTime
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun LeaderboardSection(
    level: DifficultyLevel,
    entries: ImmutableList<LeaderboardEntry>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HEADING_GAP_DP.dp),
    ) {
        LeaderboardHeading(level)

        AppCard {
            if (entries.isEmpty()) {
                LeaderboardEmptyState()
            } else {
                entries.forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = ROW_VERTICAL_PADDING_DP.dp),
                            thickness = 1.dp,
                            color = PokerTheme.colors.ivory.copy(alpha = HAIRLINE_ALPHA),
                        )
                    }
                    LeaderboardRow(index + 1, entry)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeading(level: DifficultyLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        Text(
            text = stringResource(level.type.displayNameRes),
            style =
                PokerTheme.typography.titleLarge.copy(
                    fontSize = HEADING_SP.sp,
                    lineHeight = HEADING_LINE_HEIGHT_SP.sp,
                ),
            color = PokerTheme.colors.ivory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .alignByBaseline()
                    .semantics { heading() },
        )
        Text(
            text = stringResource(Res.string.pairs_format, level.pairs),
            style = PokerTheme.typography.labelLarge.copy(fontSize = PAIRS_SP.sp),
            color = PokerTheme.colors.sage,
            maxLines = 1,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@Composable
private fun LeaderboardEmptyState() {
    Column(
        modifier = Modifier.padding(vertical = PokerTheme.spacing.medium).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = AppIcons.Trophy,
            contentDescription = null,
            modifier = Modifier.size(EMPTY_ICON_SIZE_DP.dp),
            tint = PokerTheme.colors.sage.copy(alpha = EMPTY_ICON_ALPHA),
        )

        Spacer(modifier = Modifier.height(PokerTheme.spacing.small))

        Text(
            text = stringResource(Res.string.no_stats_yet),
            style = PokerTheme.typography.bodyMedium,
            color = PokerTheme.colors.sage,
        )
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
) {
    val scoreDescription = stringResource(Res.string.score_label, entry.score)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankMedal(rank)

        Text(
            text = formatScore(entry.score),
            style =
                PokerTheme.typography.titleLarge.copy(
                    fontFamily = PokerTheme.fonts.mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = SCORE_SP.sp,
                    lineHeight = SCORE_LINE_HEIGHT_SP.sp,
                ),
            color = PokerTheme.colors.ivory,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = scoreDescription },
        )

        StatMiniItem(
            label = stringResource(Res.string.stats_time_header),
            value = formatTime(entry.timeSeconds),
        )
        StatMiniItem(
            label = stringResource(Res.string.stats_moves_header),
            value = entry.moves.toString(),
            modifier = Modifier.widthIn(min = MOVES_MIN_WIDTH_DP.dp),
        )
    }
}

@Composable
private fun RankMedal(rank: Int) {
    val colors = PokerTheme.colors
    val medalColor =
        when (rank) {
            RANK_1 -> colors.goldenYellow
            RANK_2 -> colors.silver
            RANK_3 -> colors.bronze
            else -> null
        }

    Surface(
        modifier = Modifier.size(MEDAL_SIZE_DP.dp),
        shape = CircleShape,
        color = medalColor ?: Color.Transparent,
        border =
            if (medalColor == null) {
                BorderStroke(1.dp, colors.ivory.copy(alpha = UNRANKED_BORDER_ALPHA))
            } else {
                null
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = rank.toString(),
                style =
                    PokerTheme.typography.labelLarge.copy(
                        fontFamily = PokerTheme.fonts.mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = MEDAL_RANK_SP.sp,
                    ),
                color = if (medalColor == null) colors.sageLight else colors.onGold,
            )
        }
    }
}

@Composable
private fun StatMiniItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(),
            style =
                PokerTheme.typography.labelSmall.copy(
                    fontSize = STAT_LABEL_SP.sp,
                    letterSpacing = LABEL_LETTER_SPACING_EM.em,
                ),
            color = PokerTheme.colors.sage,
            maxLines = 1,
        )
        Text(
            text = value,
            style =
                PokerTheme.typography.bodyMedium.copy(
                    fontFamily = PokerTheme.fonts.mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = STAT_VALUE_SP.sp,
                ),
            color = PokerTheme.colors.ivory,
            maxLines = 1,
        )
    }
}

/** Groups the digits of a (non-negative) score in threes: 7389 becomes "7,389". */
private fun formatScore(score: Int): String =
    score
        .toString()
        .reversed()
        .chunked(DIGIT_GROUP_SIZE)
        .joinToString(",")
        .reversed()

private const val HEADING_GAP_DP = 12
private const val HEADING_SP = 20
private const val HEADING_LINE_HEIGHT_SP = 26
private const val PAIRS_SP = 13

private const val ROW_VERTICAL_PADDING_DP = 14
private const val ROW_GAP_DP = 14
private const val HAIRLINE_ALPHA = 0.1f
private const val SCORE_SP = 22
private const val SCORE_LINE_HEIGHT_SP = 26
private const val MOVES_MIN_WIDTH_DP = 48
private const val STAT_LABEL_SP = 11
private const val STAT_VALUE_SP = 15
private const val LABEL_LETTER_SPACING_EM = 0.08f
private const val DIGIT_GROUP_SIZE = 3

private const val RANK_1 = 1
private const val RANK_2 = 2
private const val RANK_3 = 3
private const val MEDAL_SIZE_DP = 36
private const val MEDAL_RANK_SP = 15
private const val UNRANKED_BORDER_ALPHA = 0.2f

private const val EMPTY_ICON_SIZE_DP = 48
private const val EMPTY_ICON_ALPHA = 0.5f
