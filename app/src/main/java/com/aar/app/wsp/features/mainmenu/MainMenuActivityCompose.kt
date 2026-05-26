package com.aar.app.wsp.features.mainmenu

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.aar.app.wsp.commons.PreviewDevices
import com.aar.app.wsp.features.gamehistory.GameHistoryActivityCompose
import com.aar.app.wsp.features.gameplay.GamePlayActivityCompose
import com.aar.app.wsp.features.gamethemeselector.ThemeSelectorActivityCompose
import com.aar.app.wsp.features.settings.SettingsActivityCompose
import com.aar.app.wsp.model.Difficulty
import com.aar.app.wsp.model.GameMode
import com.aar.app.wsp.model.GameTheme

@Suppress("DEPRECATION")
class MainMenuActivityCompose : ComponentActivity() {

    private var pendingGridSize: Int = 4
    private var pendingGameMode: GameMode = GameMode.Normal
    private var pendingDifficulty: Difficulty = Difficulty.Easy

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

            val selectedGridSize = result.data?.getIntExtra(
                ThemeSelectorActivityCompose.EXTRA_SELECTED_GRID_SIZE,
                -1
            ) ?: -1

            val gridSize =
                if (selectedGridSize > 0) selectedGridSize else pendingGridSize

            startNewGame(themeId, themeName, gridSize)
        }
    }

    private fun startNewGame(
        gameThemeId: Int,
        themeName: String,
        gridSize: Int = pendingGridSize
    ) {

        val intent = Intent(this, GamePlayActivityCompose::class.java).apply {

            putExtra(
                GamePlayActivityCompose.EXTRA_GAME_DIFFICULTY,
                pendingDifficulty
            )

            putExtra(
                GamePlayActivityCompose.EXTRA_GAME_MODE,
                pendingGameMode
            )

            putExtra(
                GamePlayActivityCompose.EXTRA_GAME_THEME_ID,
                gameThemeId
            )

            putExtra(
                GamePlayActivityCompose.EXTRA_GAME_THEME_NAME,
                themeName
            )

            putExtra(
                GamePlayActivityCompose.EXTRA_ROW_COUNT,
                gridSize
            )

            putExtra(
                GamePlayActivityCompose.EXTRA_COL_COUNT,
                gridSize
            )
        }

        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {

            MainMenuScreen(

                onPlayClicked = { row, col, mode, difficulty ->

                    pendingGridSize = row
                    pendingGameMode = mode
                    pendingDifficulty = difficulty

                    val intent =
                        Intent(this, ThemeSelectorActivityCompose::class.java).apply {

                            putExtra(
                                ThemeSelectorActivityCompose.EXTRA_ROW_COUNT,
                                row
                            )

                            putExtra(
                                ThemeSelectorActivityCompose.EXTRA_COL_COUNT,
                                col
                            )
                        }

                    themeSelectorLauncher.launch(intent)

                    overridePendingTransition(
                        R.anim.slide_in,
                        R.anim.slide_out
                    )
                },

                onSettingClicked = {
                    startActivity(
                        Intent(
                            this,
                            SettingsActivityCompose::class.java
                        )
                    )
                },

                onHistoryClicked = {
                    startActivity(
                        Intent(
                            this,
                            GameHistoryActivityCompose::class.java
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun MainMenuScreen(
    onPlayClicked: (row: Int, col: Int, mode: GameMode, difficulty: Difficulty) -> Unit,
    onSettingClicked: () -> Unit = {},
    onHistoryClicked: () -> Unit = {}
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val isTablet = screenWidth >= 840
    val isFold = screenWidth >= 600 && screenWidth < 840

    val scale = when {
        isTablet -> 1.12f
        isFold -> 1.06f
        else -> 1f
    }

    val textScale = when {
        isTablet -> 1.03f
        isFold -> 1.01f
        else -> 1f
    }

    val buttonScale = when {
        isTablet -> 1.08f
        isFold -> 1.03f
        else -> 1f
    }

    val contentWidthFraction = when {
        isTablet -> 0.62f
        isFold -> 0.82f
        else -> 1f
    }

    var selectedGridIndex by remember { mutableStateOf(0) }
    var selectedGameMode by remember { mutableStateOf(GameMode.Normal) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.Easy) }

    val gameRoundDimValues =
        listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    val gridSizeDimension = gameRoundDimValues[selectedGridIndex]

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

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
                .padding(bottom = (20 * scale).dp),

            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.word_search),
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth(
                        if (isTablet) 0.55f else 0.9f
                    )
                    .padding(horizontal = (20 * scale).dp),

                contentScale = ContentScale.FillWidth
            )

            Column(
                modifier = Modifier.fillMaxWidth(contentWidthFraction),

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.spacedBy(
                    (12 * scale).dp
                )
            ) {

                GridSize(
                    selectedIndex = selectedGridIndex,
                    scale = scale,
                    textScale = textScale
                ) {
                    selectedGridIndex = it
                }

                GameModeSelector(
                    selectedGameMode = selectedGameMode,
                    selectedDifficulty = selectedDifficulty,
                    scale = scale,
                    textScale = textScale
                ) { newMode, newDiff ->

                    selectedGameMode = newMode
                    selectedDifficulty = newDiff
                }

                PlayButton(scale = buttonScale) {

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
                        .padding(
                            horizontal = (30 * scale).dp,
                            vertical = (20 * scale).dp
                        ),

                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    CircleMenuButton(
                        icon = R.drawable.ic_history,
                        size = (50 * buttonScale).dp,
                        onClick = onHistoryClicked
                    )

                    CircleMenuButton(
                        icon = R.drawable.ic_setting,
                        size = (50 * buttonScale).dp,
                        onClick = onSettingClicked
                    )
                }
            }
        }
    }
}

@Composable
fun GridSize(
    selectedIndex: Int,
    scale: Float,
    textScale: Float,
    onIndexChange: (Int) -> Unit
) {

    val kidsFont = FontFamily(Font(R.font.word_quest))

    val gameRoundDimValues =
        listOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = (10 * scale).dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.grid_sizee),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),

            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.grid_size_txt),
                    contentDescription = null,
                    modifier = Modifier.height((35 * scale).dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (16 * scale).dp),

                verticalAlignment = Alignment.CenterVertically
            ) {

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 0f,
                    boxSize = (62 * buttonScale(scale)).dp
                ) {

                    if (selectedIndex > 0) {
                        onIndexChange(selectedIndex - 1)
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    OutlinedText(
                        text = "${gameRoundDimValues[selectedIndex]} x ${gameRoundDimValues[selectedIndex]}",
                        fontSize = (60 * textScale).sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 12f
                    )
                }

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 180f,
                    boxSize = (62 * buttonScale(scale)).dp
                ) {

                    if (selectedIndex < gameRoundDimValues.size - 1) {
                        onIndexChange(selectedIndex + 1)
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeSelector(
    selectedGameMode: GameMode,
    selectedDifficulty: Difficulty,
    scale: Float,
    textScale: Float,
    onChange: (GameMode, Difficulty) -> Unit
) {

    val kidsFont = FontFamily(Font(R.font.word_quest))

    val gameModes = listOf(
        GameMode.Normal,
        GameMode.Hidden,
        GameMode.CountDown,
        GameMode.Marathon
    )

    val difficulties = listOf(
        Difficulty.Easy,
        Difficulty.Medium,
        Difficulty.Hard
    )

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

    val currentModeIndex = gameModes.indexOf(selectedGameMode)
    val currentDiffIndex = difficulties.indexOf(selectedDifficulty)

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Image(
            painter = painterResource(id = R.drawable.game_mode),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),

            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (20 * scale).dp),

                horizontalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.game_mode_txt),
                    contentDescription = null,
                    modifier = Modifier.height((24 * scale).dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (40 * scale).dp),

                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 0f,
                    boxSize = (45 * buttonScale(scale)).dp
                ) {

                    val newIndex =
                        if (currentModeIndex > 0)
                            currentModeIndex - 1
                        else
                            gameModes.size - 1

                    onChange(
                        gameModes[newIndex],
                        selectedDifficulty
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    OutlinedText(
                        text = gameModeNames[selectedGameMode] ?: "RELAX",
                        fontSize = (20 * textScale).sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 10f
                    )
                }

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 180f,
                    boxSize = (45 * buttonScale(scale)).dp
                ) {

                    val newIndex =
                        if (currentModeIndex < gameModes.size - 1)
                            currentModeIndex + 1
                        else
                            0

                    onChange(
                        gameModes[newIndex],
                        selectedDifficulty
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (80 * scale).dp)
                    .padding(bottom = (10 * scale).dp),

                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 0f,
                    boxSize = (40 * buttonScale(scale)).dp
                ) {

                    val newIndex =
                        if (currentDiffIndex > 0)
                            currentDiffIndex - 1
                        else
                            difficulties.size - 1

                    onChange(
                        selectedGameMode,
                        difficulties[newIndex]
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    OutlinedText(
                        text = difficultyNames[selectedDifficulty] ?: "EASY",
                        fontSize = (20 * textScale).sp,
                        fontFamily = kidsFont,
                        fillColor = Color.White,
                        strokeColor = Color(0xFFEC4899),
                        strokeWidth = 10f
                    )
                }

                RoundIconButton(
                    icon = R.drawable.back_arrow,
                    rotate = 180f,
                    boxSize = (40 * buttonScale(scale)).dp
                ) {

                    val newIndex =
                        if (currentDiffIndex < difficulties.size - 1)
                            currentDiffIndex + 1
                        else
                            0

                    onChange(
                        selectedGameMode,
                        difficulties[newIndex]
                    )
                }
            }
        }
    }
}

fun buttonScale(scale: Float): Float {
    return when {
        scale > 1.1f -> 1.08f
        scale > 1f -> 1.03f
        else -> 1f
    }
}

@Composable
fun PlayButton(
    scale: Float,
    onClick: () -> Unit
) {

    Image(
        painter = painterResource(id = R.drawable.play_btn),
        contentDescription = "Play",

        modifier = Modifier
            .width((145 * scale).dp)
            .height((56 * scale).dp)
            .clip(RoundedCornerShape(49.dp))
            .clickable { onClick() }
    )
}

@Composable
fun CircleMenuButton(
    icon: Int,
    size: Dp = 55.dp,
    onClick: () -> Unit
) {

    Image(
        painter = painterResource(id = icon),
        contentDescription = null,

        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() }
    )
}

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
fun RoundIconButton(
    icon: Int,
    rotate: Float,
    boxSize: Dp = 70.dp,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() },

        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = icon),
            contentDescription = null,

            modifier = Modifier
                .size(boxSize * 0.9f)
                .rotate(rotate)
        )
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
fun MainMenuScreenPreview() {

    MainMenuScreen(
        onPlayClicked = { _, _, _, _ -> },
        onSettingClicked = {},
        onHistoryClicked = {}
    )
}