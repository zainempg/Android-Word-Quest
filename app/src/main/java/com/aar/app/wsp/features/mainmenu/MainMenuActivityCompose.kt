package com.aar.app.wsp.features.mainmenu

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aar.app.wsp.R
import com.aar.app.wsp.features.gamehistory.GameHistoryActivityCompose
import com.aar.app.wsp.features.gamethemeselector.ThemeSelectorActivityCompose
import com.aar.app.wsp.features.settings.SettingsActivityCompose
import com.aar.app.wsp.features.gameplay.GamePlayActivityCompose
import com.aar.app.wsp.model.Difficulty
import com.aar.app.wsp.model.GameMode
import com.aar.app.wsp.model.GameTheme

@Suppress("DEPRECATION")
class MainMenuActivityCompose : ComponentActivity() {

    // Store game parameters when navigating to theme selector
    private var pendingGridSize: Int = 4
    private var pendingGameMode: GameMode = GameMode.Normal
    private var pendingDifficulty: Difficulty = Difficulty.Easy

    // Activity Result Launcher for theme selection
    private val themeSelectorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val themeId = result.data?.getIntExtra(
                ThemeSelectorActivityCompose.EXTRA_THEME_ID,
                GameTheme.NONE.id
            ) ?: GameTheme.NONE.id
            val themeName = result.data?.getStringExtra(
                ThemeSelectorActivityCompose.EXTRA_THEME_NAME
            ) ?: "Theme"

            // Start the game with selected theme
            startNewGame(themeId, themeName)
        }
    }

    private fun startNewGame(gameThemeId: Int, themeName: String) {
        val intent = Intent(this, GamePlayActivityCompose::class.java).apply {
            putExtra(GamePlayActivityCompose.EXTRA_GAME_DIFFICULTY, pendingDifficulty)
            putExtra(GamePlayActivityCompose.EXTRA_GAME_MODE, pendingGameMode)
            putExtra(GamePlayActivityCompose.EXTRA_GAME_THEME_ID, gameThemeId)
            putExtra(GamePlayActivityCompose.EXTRA_GAME_THEME_NAME, themeName)
            putExtra(GamePlayActivityCompose.EXTRA_ROW_COUNT, pendingGridSize)
            putExtra(GamePlayActivityCompose.EXTRA_COL_COUNT, pendingGridSize)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MainMenuScreen(
                onPlayClicked = { row, col, mode, difficulty ->
                    // Save parameters for when theme is selected
                    pendingGridSize = row
                    pendingGameMode = mode
                    pendingDifficulty = difficulty

                    // Launch theme selector and wait for result
                    val intent = Intent(this, ThemeSelectorActivityCompose::class.java).apply {
                        putExtra(ThemeSelectorActivityCompose.EXTRA_ROW_COUNT, row)
                        putExtra(ThemeSelectorActivityCompose.EXTRA_COL_COUNT, col)
                    }
                    themeSelectorLauncher.launch(intent)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                },
                onSettingClicked = { startActivity(Intent(this, SettingsActivityCompose::class.java)) },
                onHistoryClicked = { startActivity(Intent(this, GameHistoryActivityCompose::class.java)) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainMenuScreen(
    onPlayClicked: (row: Int, col: Int, mode: GameMode, difficulty: Difficulty) -> Unit,
    onSettingClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {}
) {

    // State lifting
    var selectedGridIndex by remember { mutableStateOf(0) } // default 4x4
    var selectedGameMode by remember { mutableStateOf(GameMode.Normal) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.Easy) }

    val gameRoundDimValues = listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
    val gridSizeDimension = gameRoundDimValues[selectedGridIndex]

    Box(modifier = Modifier.fillMaxSize()) {

        // Background
        Image(
            painter = painterResource(id = R.drawable.main_activity_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Image
            Image(
                painter = painterResource(id = R.drawable.word_search),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentScale = ContentScale.Crop
            )

            // Components
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GridSize(selectedIndex = selectedGridIndex) { newIndex ->
                    selectedGridIndex = newIndex
                }

                GameModeSelector(
                    selectedGameMode = selectedGameMode,
                    selectedDifficulty = selectedDifficulty
                ) { newMode, newDiff ->
                    selectedGameMode = newMode
                    selectedDifficulty = newDiff
                }

                PlayButton {
                    onPlayClicked(
                        gridSizeDimension,
                        gridSizeDimension,
                        selectedGameMode,
                        selectedDifficulty
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CircleMenuButton(icon = R.drawable.ic_history, onClick = onHistoryClicked)
                    CircleMenuButton(icon = R.drawable.ic_setting, onClick = onSettingClicked)
                }
            }
        }
    }
}

@Composable
fun GridSize(selectedIndex: Int, onIndexChange: (Int) -> Unit) {
    val kidsFont = FontFamily(Font(R.font.word_quest))
    val gameRoundDimValues = listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)  // Must match MainMenuScreen

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.grid_sizee),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.grid_size_txt),
                    contentDescription = "Grid Size",
                    modifier = Modifier.height(24.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f) {
                    if (selectedIndex > 0) onIndexChange(selectedIndex - 1)
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = "${gameRoundDimValues[selectedIndex]} x ${gameRoundDimValues[selectedIndex]}",
                        fontSize = 60.sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 12f
                    )
                }

                RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f) {
                    if (selectedIndex < gameRoundDimValues.size - 1) onIndexChange(selectedIndex + 1)
                }
            }
            
            // Show word count based on grid size (grid size - 1 = max words)
            val selectedSize = gameRoundDimValues[selectedIndex]
            val maxWords = selectedSize - 1  // 4x4 = 3 words, 5x5 = 4 words, etc.
            val minWords = 3  // Minimum is always 3 words
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedIndex == 0) {
                    // 4x4 shows only 3 words
                    Text(
                        text = "$maxWords words puzzle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = kidsFont,
                        color = Color.White
                    )
                } else {
                    // Larger grids show range: "3 - 4 words puzzle"
                    Text(
                        text = "$minWords - $maxWords words puzzle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = kidsFont,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun GameModeSelector(
    selectedGameMode: GameMode,
    selectedDifficulty: Difficulty,
    onChange: (GameMode, Difficulty) -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    // All available game modes and difficulties
    val gameModes = listOf(GameMode.Normal, GameMode.Hidden, GameMode.CountDown, GameMode.Marathon)
    val difficulties = listOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard)

    // Get display names
    val gameModeNames = mapOf(
        GameMode.Normal to "RELAX",
        GameMode.Hidden to "HIDDEN",
        GameMode.CountDown to "COUNTDOWN",
        GameMode.Marathon to "MARATHON"
    )
    val difficultyNames = mapOf(
        Difficulty.Easy to "EASY",
        Difficulty.Medium to "MEDIUM",
        Difficulty.Hard to "HARD"
    )

    // Current indices
    val currentModeIndex = gameModes.indexOf(selectedGameMode)
    val currentDiffIndex = difficulties.indexOf(selectedDifficulty)

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.game_mode),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.game_mode_txt),
                    contentDescription = "Game Mode",
                    modifier = Modifier.height(24.dp)
                )
            }

            // Game Mode Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f, boxSize = 30.dp) {
                    // Go to previous mode (wrap around)
                    val newIndex = if (currentModeIndex > 0) currentModeIndex - 1 else gameModes.size - 1
                    onChange(gameModes[newIndex], selectedDifficulty)
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = gameModeNames[selectedGameMode] ?: "RELAX",
                        fontSize = 15.sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 10f
                    )
                }
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f, boxSize = 30.dp) {
                    // Go to next mode (wrap around)
                    val newIndex = if (currentModeIndex < gameModes.size - 1) currentModeIndex + 1 else 0
                    onChange(gameModes[newIndex], selectedDifficulty)
                }
            }

            // Difficulty Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f, boxSize = 30.dp) {
                    // Go to previous difficulty (wrap around)
                    val newIndex = if (currentDiffIndex > 0) currentDiffIndex - 1 else difficulties.size - 1
                    onChange(selectedGameMode, difficulties[newIndex])
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = difficultyNames[selectedDifficulty] ?: "EASY",
                        fontSize = 15.sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 10f
                    )
                }
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f, boxSize = 30.dp) {
                    // Go to next difficulty (wrap around)
                    val newIndex = if (currentDiffIndex < difficulties.size - 1) currentDiffIndex + 1 else 0
                    onChange(selectedGameMode, difficulties[newIndex])
                }
            }
        }
    }
}

@Composable
fun PlayButton(onClick: () -> Unit) {
    // Use the play button image directly
    Image(
        painter = painterResource(id = R.drawable.play_btn),
        contentDescription = "Play",
        modifier = Modifier
            .width(150.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(49.dp))
            .clickable { onClick() }
    )
}

@Composable
fun CircleMenuButton(icon: Int, size: Dp = 55.dp, onClick: () -> Unit) {
    // Just display the button image directly (already has golden circle design)
    Image(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}

// Bubble text composable for rounded, 3D bubble-like text effect
@Composable
fun OutlinedText(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    fillColor: Color = Color.White,
    strokeColor: Color = Color(0xFFEC4899),
    strokeWidth: Float = 14f,
    letterSpacing: TextUnit = 3.sp
) {
    Box(modifier = modifier) {
        // Outer shadow/depth layer (darkest, largest)
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacing,
                color = Color(0xFFEC4899),
                drawStyle = Stroke(
                    width = strokeWidth + 15f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        )
        // Middle outline layer
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacing,
                color = strokeColor,
                drawStyle = Stroke(
                    width = strokeWidth + 6f,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        )
        // Inner highlight layer (gives 3D rounded look)
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacing,
                color = fillColor,
                drawStyle = Stroke(
                    width = strokeWidth,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        )
        // Fill layer
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = letterSpacing,
                color = fillColor
            )
        )
    }
}

@Composable
fun RoundIconButton(icon: Int, rotate: Float, boxSize: Dp = 70.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon), contentDescription = null, modifier = Modifier
                .size(boxSize * 0.9f)
                .rotate(rotate)
        )
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen(
        onPlayClicked = { row, col, mode, difficulty ->
            // Preview dummy action
        },
        onSettingClicked = {
            // Preview dummy action
        },
        onHistoryClicked = {
            // Preview dummy action
        }
    )
}
