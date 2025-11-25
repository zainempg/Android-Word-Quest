@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.aar.app.wsp.features.gameplay

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.aar.app.wsp.R
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.commons.DurationFormatter
import com.aar.app.wsp.commons.Util
import com.aar.app.wsp.custom.LetterBoard
import com.aar.app.wsp.custom.StreakView
import com.aar.app.wsp.features.SoundPlayer
import com.aar.app.wsp.features.gameover.GameOverActivity
import com.aar.app.wsp.features.settings.Preferences
import com.aar.app.wsp.model.Difficulty
import com.aar.app.wsp.model.GameData
import com.aar.app.wsp.model.GameMode
import com.aar.app.wsp.model.UsedWord
import com.aar.app.wsp.model.UsedWord.AnswerLine
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import javax.inject.Inject

class GamePlayActivityCompose : ComponentActivity() {

    @Inject
    lateinit var soundPlayer: SoundPlayer

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var preferences: Preferences

    private lateinit var viewModel: GamePlayViewModel

    private val extraGameMode: GameMode by lazy {
        (intent.extras?.get(EXTRA_GAME_MODE) as? GameMode) ?: GameMode.Normal
    }

    private val extraDifficulty: Difficulty by lazy {
        (intent.extras?.get(EXTRA_GAME_DIFFICULTY) as? Difficulty) ?: Difficulty.Easy
    }

    private val extraGameThemeId: Int by lazy {
        intent.extras?.getInt(EXTRA_GAME_THEME_ID) ?: 0
    }

    private val extraRowCount: Int by lazy {
        intent.extras?.getInt(EXTRA_ROW_COUNT) ?: 0
    }

    private val extraColumnCount: Int by lazy {
        intent.extras?.getInt(EXTRA_COL_COUNT) ?: 0
    }

    private val extraGameId: Int by lazy {
        intent.extras?.getInt(EXTRA_GAME_DATA_ID) ?: 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordSearchApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[GamePlayViewModel::class.java]

        // Load or generate game
        if (intent.extras?.containsKey(EXTRA_GAME_DATA_ID) == true) {
            viewModel.loadGameRound(extraGameId)
        } else {
            viewModel.generateNewGameRound(
                rowCount = extraRowCount,
                colCount = extraColumnCount,
                gameThemeId = extraGameThemeId,
                gameMode = extraGameMode,
                difficulty = extraDifficulty
            )
        }

        setContent {
            val gameState by viewModel.onGameState.observeAsState()
            val timer by viewModel.onTimer.observeAsState(0)
            val answerResult by viewModel.onAnswerResult.observeAsState()

            // Handle answer result for sounds
            LaunchedEffect(answerResult) {
                answerResult?.let { result ->
                    if (result.correct) {
                        soundPlayer.play(SoundPlayer.Sound.Correct)
                    } else {
                        soundPlayer.play(SoundPlayer.Sound.Wrong)
                    }
                }
            }

            GamePlayScreen(
                gameState = gameState,
                timer = timer,
                preferences = preferences,
                answerResult = answerResult,
                onWordSelected = { word, answerLine, reverseMatching ->
                    viewModel.answerWord(word, answerLine, reverseMatching)
                },
                onGameFinished = { gameDataId, win ->
                    if (win) {
                        soundPlayer.play(SoundPlayer.Sound.Winning)
                    } else {
                        soundPlayer.play(SoundPlayer.Sound.Lose)
                    }
                    // Navigate to game over
                    val intent = Intent(this, GameOverActivity::class.java)
                    intent.putExtra(GameOverActivity.EXTRA_GAME_ROUND_ID, gameDataId)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.resumeGame()
    }

    override fun onStop() {
        super.onStop()
        viewModel.pauseGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopGame()
    }

    companion object {
        const val EXTRA_GAME_DIFFICULTY = "game_max_duration"
        const val EXTRA_GAME_DATA_ID = "game_data_id"
        const val EXTRA_GAME_MODE = "game_mode"
        const val EXTRA_GAME_THEME_ID = "game_theme_id"
        const val EXTRA_ROW_COUNT = "row_count"
        const val EXTRA_COL_COUNT = "col_count"
    }
}

@Composable
fun GamePlayScreen(
    gameState: GamePlayViewModel.GameState?,
    timer: Int,
    preferences: Preferences?,
    answerResult: GamePlayViewModel.AnswerResult?,
    onWordSelected: (String, AnswerLine, Boolean) -> Unit,
    onGameFinished: (Int, Boolean) -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    // Pink gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B9D),
                        Color(0xFFFF8FB1),
                        Color(0xFFFFB6C1)
                    )
                )
            )
    ) {
        when (gameState) {
            is GamePlayViewModel.Generating -> {
                LoadingContent("Generating ${gameState.rowCount}x${gameState.colCount} grid...")
            }
            is GamePlayViewModel.Loading -> {
                LoadingContent("Loading game...")
            }
            is GamePlayViewModel.Playing -> {
                gameState.gameData?.let { gameData ->
                    GameContent(
                        gameData = gameData,
                        timer = timer,
                        kidsFont = kidsFont,
                        preferences = preferences,
                        answerResult = answerResult,
                        onWordSelected = onWordSelected
                    )
                }
            }
            is GamePlayViewModel.Finished -> {
                // Show completion then navigate
                LaunchedEffect(gameState) {
                    kotlinx.coroutines.delay(1500)
                    onGameFinished(gameState.gameData?.id ?: 0, gameState.win)
                }
                GameFinishedOverlay(win = gameState.win, kidsFont = kidsFont)
            }
            else -> {
                LoadingContent("Starting...")
            }
        }
    }
}

@Composable
fun LoadingContent(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun GameContent(
    gameData: GameData,
    timer: Int,
    kidsFont: FontFamily,
    preferences: Preferences?,
    answerResult: GamePlayViewModel.AnswerResult?,
    onWordSelected: (String, AnswerLine, Boolean) -> Unit
) {
    var letterBoardView by remember { mutableStateOf<LetterBoard?>(null) }

    // Handle answer result - remove streak line if wrong
    LaunchedEffect(answerResult) {
        answerResult?.let { result ->
            if (!result.correct) {
                // Wrong answer - remove the last streak line
                letterBoardView?.popStreakLine()
            }
            // If correct, the line stays (already added by the LetterBoard)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with timer and progress
        GameHeader(
            timer = timer,
            answeredCount = gameData.answeredWordsCount,
            totalWords = gameData.usedWords.size,
            kidsFont = kidsFont
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Word pills - give it weight to take space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f),
            contentAlignment = Alignment.TopCenter
        ) {
            WordPills(
                words = gameData.usedWords,
                gameMode = gameData.gameMode,
                kidsFont = kidsFont
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Letter Board (using AndroidView to embed custom view)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .clip(RoundedCornerShape(24.dp))
                .border(6.dp, Color(0xFFFEC84D), RoundedCornerShape(24.dp))
                .background(Color(0xFFB8E6F6))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val availableWidthPx = constraints.maxWidth
            val availableHeightPx = constraints.maxHeight
            val gridSizePx = minOf(availableWidthPx, availableHeightPx)
            val gridSizeDp = with(density) { gridSizePx.toDp() }

            // Calculate cell size based on grid dimensions
            val rowCount = gameData.grid?.rowCount ?: 5
            val colCount = gameData.grid?.colCount ?: 5
            val cellSizePx = gridSizePx / maxOf(rowCount, colCount)

            AndroidView(
                factory = { context ->
                    LetterBoard(context, null).apply {
                        letterBoardView = this

                        // Set up the grid with calculated size
                        gameData.grid?.let { grid ->
                            dataAdapter = ArrayLetterGridDataAdapter(grid.array)
                        }

                        // Configure appearance - show grid lines
                        gridLineBackground.visibility = android.view.View.VISIBLE
                        streakView.isSnapToGrid = preferences?.snapToGrid ?: StreakView.SnapType.ALWAYS_SNAP

                        // Set selection listener
                        selectionListener = object : LetterBoard.OnLetterSelectionListener {
                            override fun onSelectionBegin(streakLine: StreakView.StreakLine, str: String) {
                                streakLine.color = android.graphics.Color.parseColor("#FF3EA5")
                            }

                            override fun onSelectionDrag(streakLine: StreakView.StreakLine, str: String) {
                                // Update during drag
                            }

                            override fun onSelectionEnd(streakLine: StreakView.StreakLine, str: String) {
                                val answerLine = AnswerLine(
                                    streakLine.startIndex.row,
                                    streakLine.startIndex.col,
                                    streakLine.endIndex.row,
                                    streakLine.endIndex.col
                                )
                                onWordSelected(str, answerLine, preferences?.reverseMatching() ?: false)
                            }
                        }

                        // Add existing streak lines for answered words
                        gameData.usedWords.filter { it.isAnswered }.forEach { usedWord ->
                            usedWord.answerLine?.let { line ->
                                val streakLine = StreakView.StreakLine().apply {
                                    startIndex.set(line.startRow, line.startCol)
                                    endIndex.set(line.endRow, line.endCol)
                                    color = line.color
                                }
                                addStreakLine(streakLine)
                            }
                        }
                    }
                },
                update = { letterBoard ->
                    // Scale the board to fill available space
                    letterBoard.post {
                        val currentWidth = letterBoard.width
                        if (currentWidth > 0 && gridSizePx > 0) {
                            val scale = gridSizePx.toFloat() / currentWidth.toFloat()
                            letterBoard.scaleX = scale
                            letterBoard.scaleY = scale
                        }
                    }
                },
                modifier = Modifier.size(gridSizeDp)
            )
        }
    }
}

@Composable
fun GameHeader(
    timer: Int,
    answeredCount: Int,
    totalWords: Int,
    kidsFont: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFFEE440))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFF3EA5))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = DurationFormatter.fromInteger(timer),
                    fontFamily = kidsFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            // Progress pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFF3EA5))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$answeredCount/$totalWords",
                    fontFamily = kidsFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WordPills(
    words: List<UsedWord>,
    gameMode: GameMode,
    kidsFont: FontFamily
) {
    // Colors for word pills
    val pillColors = listOf(
        Color(0xFF4FC3F7), // Light blue
        Color(0xFFFEE440), // Yellow
        Color(0xFF81D4FA), // Sky blue
        Color(0xFFFFD54F), // Amber
    )

    // Use FlowRow-like layout with AndroidView for FlexboxLayout
    AndroidView(
        factory = { context ->
            FlexboxLayout(context).apply {
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.CENTER
            }
        },
        update = { flexbox ->
            flexbox.removeAllViews()
            words.forEachIndexed { index, word ->
                val pillColor = if (word.isAnswered) {
                    Color(0xFFFEE440) // Yellow for answered
                } else {
                    pillColors[index % pillColors.size]
                }

                val textView = android.widget.TextView(flexbox.context).apply {
                    text = if (gameMode == GameMode.Hidden && !word.isAnswered) {
                        "•".repeat(word.string.length)
                    } else {
                        word.string
                    }
                    textSize = 16f
                    setTextColor(android.graphics.Color.parseColor("#333333"))
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    setPadding(32, 16, 32, 16)

                    if (word.isAnswered) {
                        paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    }

                    // Create pill background
                    val drawable = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        cornerRadius = 50f
                        setColor(android.graphics.Color.argb(
                            255,
                            (pillColor.red * 255).toInt(),
                            (pillColor.green * 255).toInt(),
                            (pillColor.blue * 255).toInt()
                        ))
                        setStroke(6, android.graphics.Color.parseColor("#FEC84D"))
                    }
                    background = drawable
                }

                val params = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                flexbox.addView(textView, params)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}

@Composable
fun GameFinishedOverlay(win: Boolean, kidsFont: FontFamily) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (win) "🎉" else "😢",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (win) "COMPLETE!" else "GAME OVER",
                fontFamily = kidsFont,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xFFFF3EA5),
                        blurRadius = 20f,
                        offset = Offset(0f, 4f)
                    )
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GamePlayScreenPreview() {
    // Preview with mock data
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B9D),
                        Color(0xFFFF8FB1),
                        Color(0xFFFFB6C1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val kidsFont = FontFamily.Default

            GameHeader(
                timer = 5,
                answeredCount = 1,
                totalWords = 7,
                kidsFont = kidsFont
            )
        }
    }
}

