package io.github.smithjustinn.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.high_scores
import io.github.smithjustinn.resources.pairs_format
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.ChipBlue
import io.github.smithjustinn.theme.ChipGreen
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.PokerChip
import io.github.smithjustinn.ui.components.pokerBackground
import io.github.smithjustinn.ui.start.displayNameRes
import io.github.smithjustinn.ui.stats.components.LeaderboardSection
import io.github.smithjustinn.ui.stats.components.ModeSelector
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatsContent(
    component: StatsComponent,
    modifier: Modifier = Modifier,
) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                StatsUiEvent.PlayClick -> audioService.playEffect(AudioService.SoundEffect.CLICK)
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        AuroraEffect(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        StatsMainContent(state, component, audioService)
    }
}

@Composable
private fun StatsMainContent(
    state: StatsState,
    component: StatsComponent,
    audioService: AudioService,
) {
    // Which difficulty tab is showing is view state only; it falls back to the first leaderboard.
    var selectedDifficultyName by rememberSaveable { mutableStateOf<String?>(null) }
    val leaderboards = state.difficultyLeaderboards
    val selected =
        leaderboards.firstOrNull { (level, _) -> level.type.name == selectedDifficultyName }
            ?: leaderboards.firstOrNull()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            StatsTopBar(audioService, component)
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = SCREEN_PADDING_DP.dp,
                            end = SCREEN_PADDING_DP.dp,
                            top = PokerTheme.spacing.small,
                            bottom = PokerTheme.spacing.large,
                        ).widthIn(max = MAX_CONTENT_WIDTH_DP.dp)
                        .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(SECTION_GAP_DP.dp),
            ) {
                ModeSelector(
                    selectedMode = state.selectedGameMode,
                    onModeSelected = {
                        audioService.playEffect(AudioService.SoundEffect.CLICK)
                        component.onGameModeSelected(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (selected != null) {
                    val (level, entries) = selected

                    DifficultyTabs(
                        levels = leaderboards.map { it.first }.toImmutableList(),
                        selectedLevel = level,
                        onLevelSelected = {
                            audioService.playEffect(AudioService.SoundEffect.CLICK)
                            selectedDifficultyName = it.type.name
                        },
                    )

                    LeaderboardSection(level, entries)
                }
            }
        }
    }
}

@Composable
private fun DifficultyTabs(
    levels: ImmutableList<DifficultyLevel>,
    selectedLevel: DifficultyLevel,
    onLevelSelected: (DifficultyLevel) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        levels.forEach { level ->
            DifficultyTab(
                level = level,
                isSelected = level == selectedLevel,
                onSelected = { onLevelSelected(level) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DifficultyTab(
    level: DifficultyLevel,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val hapticsService = LocalAppGraph.current.hapticsService
    val shape = PokerTheme.shapes.medium
    val difficultyName = stringResource(level.type.displayNameRes)
    val pairsDescription = stringResource(Res.string.pairs_format, level.pairs)
    val chipColor =
        when (level.pairs) {
            PAIRS_TOURIST -> ChipBlue
            PAIRS_CASUAL -> colors.tacticalRed
            PAIRS_MASTER -> ChipGreen
            else -> colors.ink
        }
    val select = { if (!isSelected) onSelected() }

    Column(
        modifier =
            modifier
                .clip(shape)
                .background(if (isSelected) colors.pillUnselected else Color.Transparent)
                .border(
                    width = TAB_RING_WIDTH_DP.dp,
                    color = if (isSelected) colors.goldenYellow else Color.Transparent,
                    shape = shape,
                ).selectable(
                    selected = isSelected,
                    onClick = {
                        hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
                        select()
                    },
                    role = Role.RadioButton,
                ).semantics { contentDescription = "$difficultyName, $pairsDescription" }
                .padding(
                    start = PokerTheme.spacing.extraSmall,
                    end = PokerTheme.spacing.extraSmall,
                    top = PokerTheme.spacing.small,
                    bottom = TAB_BOTTOM_PADDING_DP.dp,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TAB_CONTENT_GAP_DP.dp),
    ) {
        // The tab itself is the radio button; the chip and label are decoration for it.
        Box(modifier = Modifier.clearAndSetSemantics { }) {
            PokerChip(
                text = level.pairs.toString(),
                contentColor = chipColor,
                isSelected = isSelected,
                onClick = select,
                chipSize = TAB_CHIP_SIZE_DP.dp,
            )
        }

        Text(
            text = difficultyName,
            style =
                PokerTheme.typography.labelLarge.copy(
                    fontSize = TAB_LABEL_SP.sp,
                    lineHeight = TAB_LABEL_LINE_HEIGHT_SP.sp,
                ),
            color = if (isSelected) colors.ivory else colors.sageLight,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun StatsTopBar(
    audioService: AudioService,
    component: StatsComponent,
) {
    val colors = PokerTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ).padding(
                    start = HEADER_START_PADDING_DP.dp,
                    end = SCREEN_PADDING_DP.dp,
                    top = PokerTheme.spacing.small,
                    bottom = PokerTheme.spacing.small,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
    ) {
        val backDesc = stringResource(Res.string.back_content_description)
        IconButton(
            onClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onBack()
            },
            modifier =
                Modifier
                    .size(BACK_BUTTON_SIZE_DP.dp)
                    .semantics { contentDescription = backDesc },
        ) {
            Icon(
                imageVector = AppIcons.ChevronLeft,
                contentDescription = null,
                tint = colors.ivory,
            )
        }
        Text(
            text = stringResource(Res.string.high_scores),
            style =
                PokerTheme.typography.headlineSmall.copy(
                    fontSize = HEADER_TITLE_SP.sp,
                    lineHeight = HEADER_TITLE_LINE_HEIGHT_SP.sp,
                ),
            color = colors.ivory,
            modifier = Modifier.semantics { heading() },
        )
    }
}

private const val SCREEN_PADDING_DP = 20
private const val MAX_CONTENT_WIDTH_DP = 600
private const val SECTION_GAP_DP = 20
private const val HEADER_START_PADDING_DP = 10
private const val BACK_BUTTON_SIZE_DP = 44
private const val HEADER_TITLE_SP = 26
private const val HEADER_TITLE_LINE_HEIGHT_SP = 32

private const val PAIRS_TOURIST = 6
private const val PAIRS_CASUAL = 8
private const val PAIRS_MASTER = 10
private const val TAB_CHIP_SIZE_DP = 40
private const val TAB_RING_WIDTH_DP = 1.5f
private const val TAB_CONTENT_GAP_DP = 6
private const val TAB_BOTTOM_PADDING_DP = 10
private const val TAB_LABEL_SP = 12
private const val TAB_LABEL_LINE_HEIGHT_SP = 15
