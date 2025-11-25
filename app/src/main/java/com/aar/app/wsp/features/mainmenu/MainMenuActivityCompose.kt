package com.aar.app.wsp.features.mainmenu

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aar.app.wsp.R
import com.aar.app.wsp.features.gamehistory.GameHistoryActivity
import com.aar.app.wsp.features.gamethemeselector.ThemeSelectorActivityCompose
import com.aar.app.wsp.features.settings.SettingsActivity
import com.aar.app.wsp.features.gameplay.GamePlayActivity
import com.aar.app.wsp.model.Difficulty
import com.aar.app.wsp.model.GameMode
import com.aar.app.wsp.model.GameTheme

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

            // Start the game with selected theme
            startNewGame(themeId)
        }
    }

    private fun startNewGame(gameThemeId: Int) {
        val intent = Intent(this, GamePlayActivity::class.java).apply {
            putExtra(GamePlayActivity.EXTRA_GAME_DIFFICULTY, pendingDifficulty)
            putExtra(GamePlayActivity.EXTRA_GAME_MODE, pendingGameMode)
            putExtra(GamePlayActivity.EXTRA_GAME_THEME_ID, gameThemeId)
            putExtra(GamePlayActivity.EXTRA_ROW_COUNT, pendingGridSize)
            putExtra(GamePlayActivity.EXTRA_COL_COUNT, pendingGridSize)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
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
                onSettingClicked = { startActivity(Intent(this, SettingsActivity::class.java)) },
                onHistoryClicked = { startActivity(Intent(this, GameHistoryActivity::class.java)) }
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
    val context = LocalContext.current

    // State lifting
    var selectedGridIndex by remember { mutableStateOf(1) } // default 4x4
    var selectedGameMode by remember { mutableStateOf(GameMode.Normal) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.Easy) }

    val gameRoundDimValues = listOf(3, 4, 5, 6)
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

                GameMode(
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
                    CircleMenuButton(icon = R.drawable.ic_settings, onClick = onSettingClicked)
                    CircleMenuButton(icon = R.drawable.ic_settings, onClick = onHistoryClicked)
                }
            }
        }
    }
}

@Composable
fun GridSize(selectedIndex: Int, onIndexChange: (Int) -> Unit) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))
    val gameRoundDimValues = listOf(3, 4, 5, 6)

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.grid_sizee),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GRID SIZE",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kidsFont,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color(0xFFEC4899),
                        blurRadius = 8f,
                        offset = Offset(0f, 4f)
                    )
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f) {
                if (selectedIndex > 0) onIndexChange(selectedIndex - 1)
            }

            Text(
                text = "${gameRoundDimValues[selectedIndex]} x ${gameRoundDimValues[selectedIndex]}",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f) {
                if (selectedIndex < gameRoundDimValues.size - 1) onIndexChange(selectedIndex + 1)
            }
        }
    }
}

@Composable
fun GameMode(
    selectedGameMode: GameMode,
    selectedDifficulty: Difficulty,
    onChange: (GameMode, Difficulty) -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.game_mode),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GAME MODE",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = kidsFont,
                    color = Color.White,
                    shadow = Shadow(Color(0xFFEC4899), blurRadius = 8f, offset = Offset(0f, 4f))
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f, boxSize = 30.dp) {
                    onChange(GameMode.Hidden, selectedDifficulty)
                }
                Text(text = "RELAX", style = TextStyle(fontFamily = kidsFont, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f, boxSize = 30.dp) {
                    onChange(GameMode.CountDown, selectedDifficulty)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 0f, boxSize = 30.dp) {
                    onChange(selectedGameMode, Difficulty.Easy)
                }
                Text(text = "EASY", style = TextStyle(fontFamily = kidsFont, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                RoundIconButton(icon = R.drawable.back_arrow, rotate = 180f, boxSize = 30.dp) {
                    onChange(selectedGameMode, Difficulty.Medium)
                }
            }
        }
    }
}

@Composable
fun PlayButton(text: String = "PLAY", onClick: () -> Unit) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(10.dp)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(70.dp)
                .width(220.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFFD64A))
        )
        Box(
            modifier = Modifier
                .height(64.dp)
                .width(210.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFF3EA5))
        )
        Text(
            text = text,
            style = TextStyle(
                fontSize = 32.sp,
                fontFamily = kidsFont,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                shadow = Shadow(color = Color(0xFFCE2D88), blurRadius = 18f, offset = Offset(0f, 4f))
            )
        )
    }
}

@Composable
fun CircleMenuButton(icon: Int, size: Dp = 70.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(size).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(size).clip(CircleShape).background(Color(0xFFFFD64A)))
        Box(modifier = Modifier.size(size - 10.dp).clip(CircleShape).background(Color(0xFFFF3EA5)))
        Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(size * 0.45f))
    }
}

@Composable
fun RoundIconButton(icon: Int, rotate: Float, boxSize: Dp = 70.dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(boxSize).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(boxSize * 0.9f).rotate(rotate))
    }
}
