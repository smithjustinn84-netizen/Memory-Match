package io.github.smithjustinn.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.settings
import io.github.smithjustinn.resources.settings_appearance
import io.github.smithjustinn.resources.settings_enable_heat_shield
import io.github.smithjustinn.resources.settings_enable_heat_shield_desc
import io.github.smithjustinn.resources.settings_enable_peek
import io.github.smithjustinn.resources.settings_enable_peek_desc
import io.github.smithjustinn.resources.settings_enable_third_eye
import io.github.smithjustinn.resources.settings_enable_third_eye_desc
import io.github.smithjustinn.resources.settings_four_color_deck
import io.github.smithjustinn.resources.settings_four_color_deck_desc
import io.github.smithjustinn.resources.settings_game_music
import io.github.smithjustinn.resources.settings_game_music_desc
import io.github.smithjustinn.resources.settings_gameplay_audio
import io.github.smithjustinn.resources.settings_percentage_format
import io.github.smithjustinn.resources.settings_reset
import io.github.smithjustinn.resources.settings_reset_walkthrough
import io.github.smithjustinn.resources.settings_reset_walkthrough_desc
import io.github.smithjustinn.resources.settings_sound_effects
import io.github.smithjustinn.resources.settings_sound_effects_desc
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.pokerBackground
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsContent(
    component: SettingsComponent,
    modifier: Modifier = Modifier,
) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService

    LaunchedEffect(Unit) {
        component.events.collect { event: SettingsUiEvent ->
            when (event) {
                SettingsUiEvent.PlayClick -> audioService.playEffect(AudioService.SoundEffect.CLICK)
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SettingsTopBar(audioService, component) },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                    verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
                ) {
                    SettingsAppearanceSection(state, audioService, component)
                    SettingsGameplaySection(state, audioService, component)
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    audioService: AudioService,
    component: SettingsComponent,
) {
    val hapticsService = LocalAppGraph.current.hapticsService
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
                hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
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
            text = stringResource(Res.string.settings),
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

@Composable
private fun SettingsAppearanceSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    val hapticsService = LocalAppGraph.current.hapticsService
    val playFeedback = {
        hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
        audioService.playEffect(AudioService.SoundEffect.CLICK)
    }
    if (state.isFourColorUnlocked || state.isThirdEyeUnlocked || state.isHeatShieldUnlocked) {
        SettingsSection(title = stringResource(Res.string.settings_appearance)) {
            if (state.isFourColorUnlocked) {
                SettingsToggle(
                    title = stringResource(Res.string.settings_four_color_deck),
                    description = stringResource(Res.string.settings_four_color_deck_desc),
                    checked = state.areSuitsMultiColored,
                    onCheckedChange = {
                        playFeedback()
                        component.toggleSuitsMultiColored(it)
                    },
                )
            }

            if (state.isFourColorUnlocked && state.isThirdEyeUnlocked) {
                SettingsHairline()
            }

            if (state.isThirdEyeUnlocked) {
                SettingsToggle(
                    title = stringResource(Res.string.settings_enable_third_eye),
                    description = stringResource(Res.string.settings_enable_third_eye_desc),
                    checked = state.isThirdEyeEnabled,
                    onCheckedChange = {
                        playFeedback()
                        component.toggleThirdEyeEnabled(it)
                    },
                )
            }

            if (state.isHeatShieldUnlocked) {
                if (state.isFourColorUnlocked || state.isThirdEyeUnlocked) {
                    SettingsHairline()
                }

                SettingsToggle(
                    title = stringResource(Res.string.settings_enable_heat_shield),
                    description = stringResource(Res.string.settings_enable_heat_shield_desc),
                    checked = state.isHeatShieldEnabled,
                    onCheckedChange = {
                        playFeedback()
                        component.toggleHeatShieldEnabled(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsGameplaySection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    val hapticsService = LocalAppGraph.current.hapticsService
    val playFeedback = {
        hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
        audioService.playEffect(AudioService.SoundEffect.CLICK)
    }
    SettingsSection(title = stringResource(Res.string.settings_gameplay_audio)) {
        AudioSetting(
            title = stringResource(Res.string.settings_sound_effects),
            description = stringResource(Res.string.settings_sound_effects_desc),
            enabled = state.isSoundEnabled,
            volume = state.soundVolume,
            onEnabledChange = {
                playFeedback()
                component.toggleSoundEnabled(it)
            },
            onVolumeChange = { component.setSoundVolume(it) },
        )

        SettingsHairline()

        AudioSetting(
            title = stringResource(Res.string.settings_game_music),
            description = stringResource(Res.string.settings_game_music_desc),
            enabled = state.isMusicEnabled,
            volume = state.musicVolume,
            onEnabledChange = {
                playFeedback()
                component.toggleMusicEnabled(it)
            },
            onVolumeChange = { component.setMusicVolume(it) },
        )

        SettingsHairline()

        SettingsToggle(
            title = stringResource(Res.string.settings_enable_peek),
            description = stringResource(Res.string.settings_enable_peek_desc),
            checked = state.isPeekEnabled,
            onCheckedChange = {
                playFeedback()
                component.togglePeekEnabled(it)
            },
        )

        SettingsHairline()

        ResetWalkthroughRow(
            enabled = state.isWalkthroughCompleted,
            onReset = {
                // PokerButton already performs the light haptic.
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.resetWalkthrough()
            },
        )
    }
}

/** An uppercase gold label above a raised panel whose rows are evenly spaced. */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SECTION_LABEL_GAP_DP.dp)) {
        Text(
            text = title.uppercase(),
            style = PokerTheme.typography.labelMedium.copy(letterSpacing = LABEL_LETTER_SPACING_EM.em),
            color = PokerTheme.colors.goldenYellow,
            modifier = Modifier.semantics { heading() },
        )
        AppCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(PANEL_ROW_GAP_DP.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun SettingsHairline() {
    HorizontalDivider(
        thickness = 1.dp,
        color = PokerTheme.colors.ivory.copy(alpha = HAIRLINE_ALPHA),
    )
}

@Composable
private fun AudioSetting(
    title: String,
    description: String,
    enabled: Boolean,
    volume: Float,
    onEnabledChange: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small)) {
        SettingsToggle(
            title = title,
            description = description,
            checked = enabled,
            onCheckedChange = onEnabledChange,
        )

        if (enabled) {
            VolumeSlider(
                value = volume,
                onValueChange = onVolumeChange,
                label = title,
            )
        }
    }
}

@Composable
private fun ResetWalkthroughRow(
    enabled: Boolean,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
    ) {
        SettingsRowText(
            title = stringResource(Res.string.settings_reset_walkthrough),
            description = stringResource(Res.string.settings_reset_walkthrough_desc),
            modifier = Modifier.weight(1f),
        )
        PokerButton(
            text = stringResource(Res.string.settings_reset),
            onClick = onReset,
            modifier = Modifier.height(RESET_BUTTON_HEIGHT_DP.dp),
            enabled = enabled,
            containerColor = Color.Transparent,
        )
    }
}

@Composable
private fun SettingsRowText(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style =
                PokerTheme.typography.bodyLarge.copy(
                    fontSize = ROW_TITLE_SP.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            color = PokerTheme.colors.ivory,
        )
        Text(
            text = description,
            style =
                PokerTheme.typography.bodySmall.copy(
                    fontSize = ROW_DESCRIPTION_SP.sp,
                    lineHeight = ROW_DESCRIPTION_LINE_HEIGHT_SP.sp,
                ),
            color = PokerTheme.colors.sage,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = PokerTheme.colors
    val volumeIcon =
        when {
            value == 0f -> AppIcons.VolumeOff
            value <= HALF_VOLUME -> AppIcons.VolumeDown
            else -> AppIcons.VolumeUp
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
    ) {
        Icon(
            imageVector = volumeIcon,
            contentDescription = null,
            tint = colors.goldenYellow,
            modifier = Modifier.size(VOLUME_ICON_SIZE_DP.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = label },
            thumb = { VolumeThumb() },
            track = { sliderState -> VolumeTrack(fraction = sliderState.value.coerceIn(0f, 1f)) },
        )
        Text(
            text = stringResource(Res.string.settings_percentage_format, (value * PERCENT).toInt()),
            style =
                PokerTheme.typography.labelMedium.copy(
                    fontFamily = PokerTheme.fonts.mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = VOLUME_READOUT_SP.sp,
                ),
            color = colors.sageLight,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(VOLUME_READOUT_WIDTH_DP.dp),
        )
    }
}

@Composable
private fun VolumeThumb() {
    Box(
        modifier =
            Modifier
                .size(SLIDER_THUMB_SIZE_DP.dp)
                .shadow(SLIDER_THUMB_ELEVATION_DP.dp, CircleShape)
                .background(PokerTheme.colors.ivory, CircleShape),
    )
}

@Composable
private fun VolumeTrack(fraction: Float) {
    val colors = PokerTheme.colors
    val activeColor = colors.goldenYellow
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(SLIDER_TRACK_HEIGHT_DP.dp)
                .clip(CircleShape)
                .background(colors.feltGreenDark)
                .drawBehind {
                    drawRect(color = activeColor, size = Size(size.width * fraction, size.height))
                },
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
    ) {
        SettingsRowText(
            title = title,
            description = description,
            modifier = Modifier.weight(1f),
        )
        SettingsSwitch(checked = checked)
    }
}

/** Purely visual: the enclosing row owns the toggle semantics. */
@Composable
private fun SettingsSwitch(checked: Boolean) {
    val colors = PokerTheme.colors
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) SWITCH_THUMB_TRAVEL_DP.dp else 0.dp,
        label = "switchThumbOffset",
    )

    Box(
        modifier =
            Modifier
                .size(width = SWITCH_WIDTH_DP.dp, height = SWITCH_HEIGHT_DP.dp)
                .clip(CircleShape)
                .background(if (checked) colors.goldenYellow else colors.feltGreenDark)
                .then(
                    if (checked) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, colors.ivory.copy(alpha = SWITCH_OFF_BORDER_ALPHA), CircleShape)
                    },
                ).padding(SWITCH_PADDING_DP.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = thumbOffset)
                    .size(SWITCH_THUMB_SIZE_DP.dp)
                    .background(if (checked) colors.onGold else colors.sage, CircleShape),
        )
    }
}

private const val SCREEN_PADDING_DP = 20
private const val MAX_CONTENT_WIDTH_DP = 600
private const val HEADER_START_PADDING_DP = 10
private const val BACK_BUTTON_SIZE_DP = 44
private const val HEADER_TITLE_SP = 26
private const val HEADER_TITLE_LINE_HEIGHT_SP = 32

private const val SECTION_LABEL_GAP_DP = 10
private const val LABEL_LETTER_SPACING_EM = 0.08f
private const val PANEL_ROW_GAP_DP = 14
private const val ROW_GAP_DP = 12
private const val HAIRLINE_ALPHA = 0.1f
private const val ROW_TITLE_SP = 15
private const val ROW_DESCRIPTION_SP = 13
private const val ROW_DESCRIPTION_LINE_HEIGHT_SP = 18
private const val RESET_BUTTON_HEIGHT_DP = 44

private const val HALF_VOLUME = 0.5f
private const val PERCENT = 100
private const val VOLUME_ICON_SIZE_DP = 18
private const val VOLUME_READOUT_SP = 13
private const val VOLUME_READOUT_WIDTH_DP = 44
private const val SLIDER_TRACK_HEIGHT_DP = 6
private const val SLIDER_THUMB_SIZE_DP = 22
private const val SLIDER_THUMB_ELEVATION_DP = 3

private const val SWITCH_WIDTH_DP = 52
private const val SWITCH_HEIGHT_DP = 32
private const val SWITCH_PADDING_DP = 4
private const val SWITCH_THUMB_SIZE_DP = 24
private const val SWITCH_THUMB_TRAVEL_DP = 20
private const val SWITCH_OFF_BORDER_ALPHA = 0.35f
