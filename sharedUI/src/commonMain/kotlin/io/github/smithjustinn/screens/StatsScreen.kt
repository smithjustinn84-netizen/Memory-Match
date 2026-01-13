package io.github.smithjustinn.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.components.AppIcons
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.utils.formatTime
import kotlinx.coroutines.flow.*
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

data class StatsState(
    val difficultyLeaderboards: List<Pair<DifficultyLevel, List<LeaderboardEntry>>> = emptyList()
)

@Inject
class StatsScreenModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        loadLeaderboards()
    }

    private fun loadLeaderboards() {
        val difficulties = DifficultyLevel.defaultLevels
        val flows = difficulties.map { level ->
            leaderboardRepository.getTopEntries(level.pairs).map { entries -> level to entries }
        }

        combine(flows) { pairs ->
            StatsState(difficultyLeaderboards = pairs.toList())
        }.onEach { newState ->
            _state.update { newState }
        }.catch { e ->
            logger.e(e) { "Error loading leaderboards" }
        }.launchIn(screenModelScope)
    }
}

class StatsScreen : Screen, JavaSerializable {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.statsScreenModel }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.high_scores)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.difficultyLeaderboards) { (level, entries) ->
                    LeaderboardSection(level, entries)
                }
            }
        }
    }

    @Composable
    private fun LeaderboardSection(level: DifficultyLevel, entries: List<LeaderboardEntry>) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(level.nameRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.pairs_format, level.pairs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                if (entries.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.no_stats_yet),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        entries.forEachIndexed { index, entry ->
                            LeaderboardRow(index + 1, entry)
                            if (index < entries.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.score_label, entry.score),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatMiniItem(
                    label = "Time",
                    value = formatTime(entry.timeSeconds)
                )
                StatMiniItem(
                    label = "Moves",
                    value = entry.moves.toString()
                )
            }
        }
    }

    @Composable
    private fun StatMiniItem(label: String, value: String) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
