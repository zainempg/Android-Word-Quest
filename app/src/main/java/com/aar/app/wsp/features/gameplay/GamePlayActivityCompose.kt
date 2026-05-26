@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@file:OptIn(ExperimentalLayoutApi::class)

package com.aar.app.wsp.features.gameplay

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.aar.app.wsp.R
import com.aar.app.wsp.commons.PreviewDevices
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.commons.DurationFormatter
import com.aar.app.wsp.commons.Util
import com.aar.app.wsp.custom.LetterBoard
import com.aar.app.wsp.custom.StreakView
import com.aar.app.wsp.features.SoundPlayer
import com.aar.app.wsp.features.gameover.GameOverActivityCompose
import com.aar.app.wsp.features.settings.Preferences
import com.aar.app.wsp.model.Difficulty
import com.aar.app.wsp.model.GameData
import com.aar.app.wsp.model.GameMode
import com.aar.app.wsp.model.UsedWord
import com.aar.app.wsp.model.UsedWord.AnswerLine
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

    private val extraGameThemeName: String? by lazy {
        intent.getStringExtra(EXTRA_GAME_THEME_NAME)
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
                difficulty = extraDifficulty,
                themeName = extraGameThemeName
            )
        }

        setContent {
            val gameState by viewModel.onGameState.observeAsState()
            val timer by viewModel.onTimer.observeAsState(0)
            val answerResult by viewModel.onAnswerResult.observeAsState()
            val countDown by viewModel.onCountDown.observeAsState(0)
            val currentWord by viewModel.onCurrentWordChanged.observeAsState()
            val currentWordCountDown by viewModel.onCurrentWordCountDown.observeAsState(0)

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

            // Determine theme name: use provided name, or game name from data if loading from history
            val displayThemeName = extraGameThemeName 
                ?: (gameState as? GamePlayViewModel.Playing)?.gameData?.name 
                ?: "Puzzle"

            GamePlayScreen(
                gameState = gameState,
                timer = timer,
                countDown = countDown,
                currentWord = currentWord,
                currentWordCountDown = currentWordCountDown,
                preferences = preferences,
                answerResult = answerResult,
                themeName = displayThemeName,
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
                    val intent = Intent(this, GameOverActivityCompose::class.java)
                    intent.putExtra(GameOverActivityCompose.EXTRA_GAME_ROUND_ID, gameDataId)
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
        const val EXTRA_GAME_THEME_NAME = "game_theme_name"
        const val EXTRA_ROW_COUNT = "row_count"
        const val EXTRA_COL_COUNT = "col_count"
    }
}

@Composable
fun GamePlayScreen(
    gameState: GamePlayViewModel.GameState?,
    timer: Int,
    countDown: Int,
    currentWord: UsedWord?,
    currentWordCountDown: Int,
    preferences: Preferences?,
    answerResult: GamePlayViewModel.AnswerResult?,
    themeName: String,
    onWordSelected: (String, AnswerLine, Boolean) -> Unit,
    onGameFinished: (Int, Boolean) -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    // Track if we should show the overlay (delayed for last word animation)
    var showFinishedOverlay by remember { mutableStateOf(false) }
    
    // Remember the last playing game data to show during finish animation
    var lastGameData by remember { mutableStateOf<GameData?>(null) }
    
    // Update last game data when in Playing state
    if (gameState is GamePlayViewModel.Playing) {
        lastGameData = gameState.gameData
    }

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
                        countDown = countDown,
                        currentWord = currentWord,
                        currentWordCountDown = currentWordCountDown,
                        kidsFont = kidsFont,
                        preferences = preferences,
                        answerResult = answerResult,
                        themeName = themeName,
                        onWordSelected = onWordSelected
                    )
                }
            }
            is GamePlayViewModel.Finished -> {
                // Go directly to Game Over screen with short delay for last word animation
                LaunchedEffect(gameState) {
                    // Brief delay for last word animation
                    kotlinx.coroutines.delay(800)
                    onGameFinished(gameState.gameData?.id ?: 0, gameState.win)
                }
                
                // Show the game content during the brief transition
                val gameDataToShow = gameState.gameData ?: lastGameData
                if (gameDataToShow != null) {
                    GameContent(
                        gameData = gameDataToShow,
                        timer = timer,
                        countDown = countDown,
                        currentWord = currentWord,
                        currentWordCountDown = currentWordCountDown,
                        kidsFont = kidsFont,
                        preferences = preferences,
                        answerResult = answerResult,
                        themeName = themeName,
                        onWordSelected = { _, _, _ -> } // Disable selection during finish
                    )
                }
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
    countDown: Int,
    currentWord: UsedWord?,
    currentWordCountDown: Int,
    kidsFont: FontFamily,
    preferences: Preferences?,
    answerResult: GamePlayViewModel.AnswerResult?,
    themeName: String,
    onWordSelected: (String, AnswerLine, Boolean) -> Unit
) {
    var letterBoardView by remember { mutableStateOf<LetterBoard?>(null) }
    
    // Flying text animation state
    var flyingWord by remember { mutableStateOf<String?>(null) }
    var showFlyingText by remember { mutableStateOf(false) }
    val flyAnimationProgress = remember { Animatable(0f) }
    var flyStartX by remember { mutableStateOf(0f) }
    var flyStartY by remember { mutableStateOf(0f) }
    var lastStreakEndX by remember { mutableStateOf(0f) }
    var lastStreakEndY by remember { mutableStateOf(0f) }

    // Handle answer result - remove streak line if wrong
    LaunchedEffect(answerResult) {
        answerResult?.let { result ->
            if (!result.correct) {
                // Wrong answer - remove the last streak line
                letterBoardView?.popStreakLine()
            } else {
                // Correct answer - trigger flying animation from streak line position
                result.usedWord?.let { word ->
                    flyingWord = word.string
                    flyStartX = lastStreakEndX
                    flyStartY = lastStreakEndY
                    showFlyingText = true
                    flyAnimationProgress.snapTo(0f)
                    flyAnimationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
                    )
                    showFlyingText = false
                }
            }
        }
    }

    val isLargeScreen = LocalConfiguration.current.screenWidthDp >= 600
    val contentPadding = if (isLargeScreen) 12.dp else 16.dp
    val pillFontSize = if (isLargeScreen) 16.sp else 13.sp
    val pillPaddingH = if (isLargeScreen) 22.dp else 18.dp
    val pillPaddingV = if (isLargeScreen) 10.dp else 8.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            GameHeader(
                gameData = gameData,
                timer = timer,
                countDown = countDown,
                answeredCount = gameData.answeredWordsCount,
                totalWords = gameData.usedWords.size,
                kidsFont = kidsFont
            )

            Spacer(modifier = Modifier.height(if (isLargeScreen) 6.dp else 12.dp))

            ThemeNameBadge(
                themeName = themeName,
                kidsFont = kidsFont
            )

            Spacer(modifier = Modifier.height(if (isLargeScreen) 4.dp else 8.dp))

            WordPills(
                words = gameData.usedWords,
                gameMode = gameData.gameMode,
                kidsFont = kidsFont,
                lastAnsweredWord = answerResult?.usedWord,
                grayscale = preferences?.grayscale() == true,
                fontSize = pillFontSize,
                horizontalPadding = pillPaddingH,
                verticalPadding = pillPaddingV
            )

            if (gameData.gameMode == GameMode.Marathon && currentWord != null) {
                MarathonWordProgress(
                    currentWord = currentWord,
                    currentWordCountDown = currentWordCountDown,
                    kidsFont = kidsFont
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (gameData.gameMode == GameMode.CountDown && gameData.maxDuration > 0) {
                CountDownProgressBar(
                    progress = countDown.toFloat() / gameData.maxDuration.toFloat(),
                    countDown = countDown,
                    kidsFont = kidsFont,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val frameInset = 8.dp
                val gridSide = minOf(maxWidth, maxHeight)
                val gridSidePx = with(density) {
                    (gridSide - frameInset * 2).coerceAtLeast(1.dp).roundToPx()
                }

                val rowCount = gameData.grid?.rowCount ?: 5
                val colCount = gameData.grid?.colCount ?: 5
                val maxDim = maxOf(rowCount, colCount)
                val defaultGridSize = 50 * maxDim
                val autoScale = preferences?.autoScaleGrid() != false
                val scaleFactor = if (autoScale || defaultGridSize > gridSidePx) {
                    gridSidePx.toFloat() / defaultGridSize.toFloat()
                } else {
                    1f
                }

                val isGrayscale = preferences?.grayscale() == true
                val grayColor = android.graphics.Color.parseColor("#aa808080")
                val streakColors = if (isGrayscale) {
                    intArrayOf(grayColor)
                } else {
                    intArrayOf(
                        android.graphics.Color.parseColor("#80FF3EA5"),
                        android.graphics.Color.parseColor("#804FC3F7"),
                        android.graphics.Color.parseColor("#8066BB6A"),
                        android.graphics.Color.parseColor("#80FFA726"),
                        android.graphics.Color.parseColor("#80AB47BC"),
                        android.graphics.Color.parseColor("#80EF5350"),
                        android.graphics.Color.parseColor("#8026C6DA"),
                        android.graphics.Color.parseColor("#80FFEE58"),
                        android.graphics.Color.parseColor("#808D6E63"),
                        android.graphics.Color.parseColor("#805C6BC0"),
                    )
                }
                val answeredCount = gameData.usedWords.count { it.isAnswered }

                Box(
                    modifier = Modifier
                        .size(gridSide)
                        .clip(RoundedCornerShape(24.dp))
                        .border(6.dp, Color(0xFFFEC84D), RoundedCornerShape(24.dp))
                        .background(Color(0xFFB8E6F6))
                        .padding(4.dp)
                ) {
                    key(gridSidePx, rowCount, colCount, scaleFactor) {
                        AndroidView(
                            factory = { context ->
                                LetterBoard(context, null).apply {
                                    letterBoardView = this
                                    gameData.grid?.let { grid ->
                                        dataAdapter = ArrayLetterGridDataAdapter(grid.array)
                                    }
                                    gridLineBackground.visibility = if (preferences?.showGridLine() == true) {
                                        android.view.View.VISIBLE
                                    } else {
                                        android.view.View.INVISIBLE
                                    }
                                    streakView.isSnapToGrid =
                                        preferences?.snapToGrid ?: StreakView.SnapType.ALWAYS_SNAP
                                    scale(scaleFactor, scaleFactor)

                                    gameData.usedWords.filter { it.isAnswered }
                                        .forEachIndexed { index, usedWord ->
                                            usedWord.answerLine?.let { line ->
                                                val streakLine = StreakView.StreakLine().apply {
                                                    startIndex.set(line.startRow, line.startCol)
                                                    endIndex.set(line.endRow, line.endCol)
                                                    color = if (line.color != 0) {
                                                        line.color
                                                    } else {
                                                        streakColors[index % streakColors.size]
                                                    }
                                                }
                                                addStreakLine(streakLine)
                                            }
                                        }

                                    selectionListener = createSelectionListener(
                                        streakColors = streakColors,
                                        colorIndex = answeredCount,
                                        onStreakEnd = { x, y, str, line ->
                                            lastStreakEndX = x
                                            lastStreakEndY = y
                                            onWordSelected(
                                                str,
                                                line,
                                                preferences?.reverseMatching() ?: false
                                            )
                                        }
                                    )
                                }
                            },
                            update = { letterBoard ->
                                val currentAnsweredCount = gameData.usedWords.count { it.isAnswered }
                                letterBoard.selectionListener = createSelectionListener(
                                    streakColors = streakColors,
                                    colorIndex = currentAnsweredCount,
                                    onStreakEnd = { x, y, str, line ->
                                        lastStreakEndX = x
                                        lastStreakEndY = y
                                        onWordSelected(
                                            str,
                                            line,
                                            preferences?.reverseMatching() ?: false
                                        )
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    }

                    if (showFlyingText && flyingWord != null) {
                        val progress = flyAnimationProgress.value
                        val textScale = 1f + (0.6f * progress)
                        val alpha = if (progress > 0.6f) {
                            1f - ((progress - 0.6f) / 0.4f)
                        } else {
                            1f
                        }
                        val liftPx = 100f * progress

                        Text(
                            text = flyingWord!!,
                            fontFamily = kidsFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFFFEE440).copy(alpha = alpha),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = with(density) { flyStartX.toDp() } - 20.dp,
                                    y = with(density) { (flyStartY - liftPx).toDp() } - 12.dp
                                )
                                .scale(textScale),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = alpha * 0.8f),
                                    blurRadius = 8f,
                                    offset = Offset(2f, 2f)
                                )
                            )
                        )
                    }
                }
            }
        }
}

private fun createSelectionListener(
    streakColors: IntArray,
    colorIndex: Int,
    onStreakEnd: (Float, Float, String, UsedWord.AnswerLine) -> Unit
): LetterBoard.OnLetterSelectionListener {
    return object : LetterBoard.OnLetterSelectionListener {
        override fun onSelectionBegin(streakLine: StreakView.StreakLine, str: String) {
            streakLine.color = streakColors[colorIndex % streakColors.size]
        }

        override fun onSelectionDrag(streakLine: StreakView.StreakLine, str: String) = Unit

        override fun onSelectionEnd(streakLine: StreakView.StreakLine, str: String) {
            val centerX = (streakLine.start.x + streakLine.end.x) / 2f
            val centerY = (streakLine.start.y + streakLine.end.y) / 2f
            val answerLine = UsedWord.AnswerLine(
                streakLine.startIndex.row,
                streakLine.startIndex.col,
                streakLine.endIndex.row,
                streakLine.endIndex.col
            )
            onStreakEnd(centerX, centerY, str, answerLine)
        }
    }
}

@Composable
fun GameHeader(
    gameData: GameData,
    timer: Int,
    countDown: Int,
    answeredCount: Int,
    totalWords: Int,
    kidsFont: FontFamily
) {
    Spacer(Modifier.height(10.dp))
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main header row
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
}

@Composable
fun CountDownProgressBar(
    progress: Float,
    countDown: Int,
    kidsFont: FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0)) // Gray background
    ) {
        // Progress fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4FC3F7)) // Sky blue
        )
        
        // Countdown text on top
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = DurationFormatter.fromInteger(countDown),
                fontFamily = kidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1976D2) // Dark blue text
            )
        }
    }
}

@Composable
fun MarathonWordProgress(
    currentWord: UsedWord,
    currentWordCountDown: Int,
    kidsFont: FontFamily
) {
    val progress = if (currentWord.maxDuration > 0) {
        currentWordCountDown.toFloat() / currentWord.maxDuration.toFloat()
    } else 1f

    // Yellow pill with word and progress bar inside
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0)) // Gray background (empty part)
    ) {
        // Yellow progress that shrinks from right to left
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEC84D))
        )
        
        // Word text on top
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentWord.string,
                fontFamily = kidsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF8B5E3C) // Brown color
            )
        }
    }
}

@Composable
fun ThemeNameBadge(
    themeName: String,
    kidsFont: FontFamily
) {
    // Use the same font as main menu
    val wordQuestFont = FontFamily(Font(R.font.word_quest))
    
    Box(
        modifier = Modifier.width(IntrinsicSize.Max),
        contentAlignment = Alignment.Center
    ) {
        // Gradient line at bottom - behind the text, moved up to touch text
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp)
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        )
                    )
                )
        )
        
        // Text with yellow stroke (on top of line)
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Yellow stroke/outline text (drawn behind)
            Text(
                text = themeName.uppercase(),
                fontFamily = wordQuestFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFFFEC84D), // Yellow color for stroke
                style = TextStyle(
                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 5f
                    )
                )
            )
            // Main text (drawn on top)
            Text(
                text = themeName.uppercase(),
                fontFamily = wordQuestFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White
            )
        }
    }
}
@Preview(showBackground = true, widthDp = PreviewDevices.PHONE_WIDTH, heightDp = 200, name = "Phone")
@Preview(showBackground = true, widthDp = PreviewDevices.TABLET_WIDTH, heightDp = 240, name = "Tablet")
@Composable
fun ThemeNamePreview() {
    // Preview with mock data
    ThemeNameBadge(themeName = "Theme Name", kidsFont = FontFamily.Default)
}




@Composable
fun WordPills(
    words: List<UsedWord>,
    gameMode: GameMode,
    kidsFont: FontFamily,
    lastAnsweredWord: UsedWord? = null,
    grayscale: Boolean = false,
    fontSize: TextUnit = 13.sp,
    horizontalPadding: Dp = 18.dp,
    verticalPadding: Dp = 8.dp
) {
    // Colors for word pills - gray when grayscale is enabled
    val grayColor = Color(0xFF808080)
    val pillColors = if (grayscale) {
        listOf(grayColor, grayColor)
    } else {
        listOf(
            Color(0xFF4FC3F7), // Light blue
            Color(0xFF81D4FA), // Sky blue
        )
    }
    
    // Answered pill color - gray when grayscale, yellow otherwise
    val answeredColor = if (grayscale) grayColor else Color(0xFFFEE440)

    // Track which words have already been animated to prevent re-animation on recomposition
    val animatedWordIds = remember { mutableSetOf<Int>() }
    
    // Capture the last answered word ID to trigger animation only once
    val lastAnsweredId = lastAnsweredWord?.id

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        words.forEachIndexed { index, word ->
            val pillColor = if (word.isAnswered) {
                answeredColor
            } else {
                pillColors[index % pillColors.size]
            }
            val shouldAnimate = lastAnsweredId == word.id &&
                    word.isAnswered &&
                    !animatedWordIds.contains(word.id)

            val scale = remember { Animatable(1f) }
            LaunchedEffect(shouldAnimate) {
                if (shouldAnimate) {
                    animatedWordIds.add(word.id)
                    scale.animateTo(
                        targetValue = 1.4f,
                        animationSpec = repeatable(
                            iterations = 3,
                            animation = tween(75),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .clip(RoundedCornerShape(50))
                    .background(pillColor)
                    .border(
                        width = 4.dp,
                        color = Color(0xFFFEC84D),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            ) {
                Text(
                    text = if (gameMode == GameMode.Hidden && !word.isAnswered) {
                        "•".repeat(word.string.length)
                    } else {
                        word.string
                    },
                    fontSize = fontSize,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (word.isAnswered) TextDecoration.LineThrough else null
                )
            }
        }
    }
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

@Preview(
    showBackground = true,
    widthDp = PreviewDevices.PHONE_WIDTH,
    heightDp = PreviewDevices.PHONE_HEIGHT,
    name = "Phone"
)
@Preview(
    showBackground = true,
    widthDp = PreviewDevices.TABLET_WIDTH,
    heightDp = PreviewDevices.TABLET_HEIGHT,
    name = "Tablet"
)
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

            // Preview CountDown progress bar
            CountDownProgressBar(
                progress = 0.6f,
                countDown = 45,
                kidsFont = kidsFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

