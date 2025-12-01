package com.aar.app.wsp.features.gamehistory

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aar.app.wsp.R
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.commons.DurationFormatter
import com.aar.app.wsp.features.gameplay.GamePlayActivityCompose
import com.aar.app.wsp.features.settings.SettingsActivityCompose
import com.aar.app.wsp.model.GameDataInfo
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import androidx.compose.foundation.Image

class GameHistoryActivityCompose : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: GameHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        (application as WordSearchApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[GameHistoryViewModel::class.java]

        setContent {
            val gameDataList by viewModel.onGameDataInfoLoaded.observeAsState(emptyList())

            GameHistoryScreen(
                gameDataList = gameDataList,
                onGameClick = { gameDataInfo ->
                    val intent = Intent(this, GamePlayActivityCompose::class.java)
                    intent.putExtra(GamePlayActivityCompose.EXTRA_GAME_DATA_ID, gameDataInfo.id)
                    intent.putExtra(GamePlayActivityCompose.EXTRA_GAME_THEME_NAME, gameDataInfo.name)
                    startActivity(intent)
                },
                onDeleteClick = { gameDataInfo ->
                    lifecycleScope.launch {
                        viewModel.deleteGameData(gameDataInfo.id)
                    }
                },
                onClearAllClick = {
                    lifecycleScope.launch {
                        viewModel.clear()
                    }
                },
                onBackClick = { finish() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.loadGameHistory()
        }
    }
}

@Composable
fun GameHistoryScreen(
    gameDataList: List<GameDataInfo>,
    onGameClick: (GameDataInfo) -> Unit,
    onDeleteClick: (GameDataInfo) -> Unit,
    onClearAllClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    Box(modifier = Modifier.fillMaxSize()) {
        // Pink gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B9D),
                            Color(0xFFFF7DAB),
                            Color(0xFFFF8FB8)
                        )
                    )
                )
        )

        // Floating bubbles
        FloatingBubbles()

        // Sparkles
        Sparkles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with back button and title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Back button on the left
                Box(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    CircleBackButton(onClick = onBackClick)
                }

                // Title in center
                GameHistoryTitle(text = "Game History", kidsFont = kidsFont)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clear All Button
            ClearAllButton(onClick = onClearAllClick, kidsFont = kidsFont)

            Spacer(modifier = Modifier.height(20.dp))

            // Game list or empty state
            if (gameDataList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved games",
                        fontFamily = kidsFont,
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(gameDataList) { index, gameData ->
                        GameHistoryItem(
                            gameDataInfo = gameData,
                            colorIndex = index,
                            kidsFont = kidsFont,
                            onClick = { onGameClick(gameData) },
                            onDeleteClick = { onDeleteClick(gameData) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

    }
}

@Composable
fun CircleBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(0xFF4FC3F7))
            .border(3.dp, Color(0xFFFEC84D), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Arrow icon
        Canvas(modifier = Modifier.size(20.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.2f)
                lineTo(size.width * 0.3f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.8f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun GameHistoryTitle(text: String, kidsFont: FontFamily) {
    Box {
        // Pink outline/shadow
        Text(
            text = text,
            fontFamily = kidsFont,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63),
            style = TextStyle(
                drawStyle = Stroke(width = 16f)
            )
        )
        // White fill
        Text(
            text = text,
            fontFamily = kidsFont,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ClearAllButton(onClick: () -> Unit, kidsFont: FontFamily) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF6B35),
                        Color(0xFFFF4444)
                    )
                )
            )
            .border(4.dp, Color(0xFFFEC84D), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 40.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CLEAR ALL",
            fontFamily = kidsFont,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun GameHistoryItem(
    gameDataInfo: GameDataInfo,
    colorIndex: Int,
    kidsFont: FontFamily,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Alternating colors - purple and cyan
    val backgroundColor = if (colorIndex % 2 == 0) {
        Color(0xFFD4A5FF) // Light purple
    } else {
        Color(0xFF7DD3F4) // Cyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .border(4.dp, Color(0xFFFEC84D), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Name and duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Puzzle name
                Text(
                    text = gameDataInfo.name ?: "Puzzle",
                    fontFamily = kidsFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                // Duration (if > 0)
                if (gameDataInfo.duration > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = DurationFormatter.fromInteger(gameDataInfo.duration),
                        fontFamily = kidsFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7AC943) // Green color for time
                    )
                }
            }

            // Right side - Grid info and delete button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grid size and word count
                Text(
                    text = "${gameDataInfo.gridRowCount}x${gameDataInfo.gridColCount}, ${gameDataInfo.usedWordsCount} Words",
                    fontFamily = kidsFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63) // Pink color
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4081))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingBubbles() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    val bubbles = remember {
        List(8) {
            BubbleData(
                startX = Random.nextFloat(),
                startY = Random.nextFloat() * 0.3f + 0.7f,
                size = Random.nextFloat() * 50f + 40f,
                speed = Random.nextFloat() * 0.3f + 0.2f
            )
        }
    }

    bubbles.forEachIndexed { index, bubble ->
        val yOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (8000 / bubble.speed).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "bubble_$index"
        )

        val xWobble by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wobble_$index"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = (bubble.startX + xWobble * 0.02f) * size.width
            val y = (bubble.startY - yOffset * 1.2f) * size.height

            if (y > -bubble.size && y < size.height + bubble.size) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = bubble.size,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = bubble.size * 0.3f,
                    center = Offset(x - bubble.size * 0.3f, y - bubble.size * 0.3f)
                )
            }
        }
    }
}

data class BubbleData(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speed: Float
)

@Composable
fun Sparkles() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")

    val sparklePositions = remember {
        listOf(
            Offset(0.85f, 0.2f),
            Offset(0.1f, 0.3f),
            Offset(0.9f, 0.5f),
            Offset(0.15f, 0.7f),
            Offset(0.88f, 0.85f)
        )
    }

    sparklePositions.forEachIndexed { index, position ->
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000 + index * 200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle_$index"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = position.x * size.width
            val y = position.y * size.height

            // Draw 4-pointed star
            val starPath = Path().apply {
                moveTo(x, y - 8f)
                lineTo(x + 3f, y - 3f)
                lineTo(x + 8f, y)
                lineTo(x + 3f, y + 3f)
                lineTo(x, y + 8f)
                lineTo(x - 3f, y + 3f)
                lineTo(x - 8f, y)
                lineTo(x - 3f, y - 3f)
                close()
            }
            drawPath(starPath, Color.White.copy(alpha = alpha))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameHistoryScreenPreview() {
    val sampleData = listOf(
        GameDataInfo().apply {
            id = 1
            name = "Puzzle - 12"
            duration = 0
            gridRowCount = 4
            gridColCount = 4
            usedWordsCount = 1
        },
        GameDataInfo().apply {
            id = 2
            name = "Puzzle"
            duration = 3
            gridRowCount = 4
            gridColCount = 4
            usedWordsCount = 1
        },
        GameDataInfo().apply {
            id = 3
            name = "Puzzle - 11"
            duration = 3
            gridRowCount = 7
            gridColCount = 4
            usedWordsCount = 9
        }
    )

    GameHistoryScreen(
        gameDataList = sampleData,
        onGameClick = {},
        onDeleteClick = {},
        onClearAllClick = {},
        onBackClick = {}
    )
}

