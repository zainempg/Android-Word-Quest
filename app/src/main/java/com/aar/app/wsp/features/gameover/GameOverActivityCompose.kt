package com.aar.app.wsp.features.gameover

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aar.app.wsp.R
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.commons.DurationFormatter
import com.aar.app.wsp.features.gameplay.GamePlayActivityCompose
import com.aar.app.wsp.features.mainmenu.MainMenuActivityCompose
import com.aar.app.wsp.features.settings.Preferences
import com.aar.app.wsp.model.GameData
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameOverActivityCompose : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var preferences: Preferences

    private lateinit var viewModel: GameOverViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        (application as WordSearchApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[GameOverViewModel::class.java]

        intent.extras?.getInt(EXTRA_GAME_ROUND_ID)?.let { gameId ->
            lifecycleScope.launch { viewModel.loadData(gameId) }
        }

        setContent {
            val gameData by viewModel.onGameDataLoaded.observeAsState()
            val gameDataReset by viewModel.onGameDataReset.observeAsState()

            // Handle game reset navigation
            LaunchedEffect(gameDataReset) {
                gameDataReset?.let { gameDataId ->
                    val intent = Intent(this@GameOverActivityCompose, GamePlayActivityCompose::class.java)
                    intent.putExtra(GamePlayActivityCompose.EXTRA_GAME_DATA_ID, gameDataId)
                    startActivity(intent)
                    finish()
                }
            }

            GameOverScreen(
                gameData = gameData,
                onReplayClicked = {
                    lifecycleScope.launch {
                        viewModel.resetCurrentGameData()
                    }
                },
                onMainMenuClicked = {
                    goToMainMenu()
                }
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        goToMainMenu()
    }

    private fun goToMainMenu() {
        if (preferences.deleteAfterFinish()) {
            lifecycleScope.launch { viewModel.deleteGameRound() }
        }
        val intent = Intent(this, MainMenuActivityCompose::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_GAME_ROUND_ID = "com.paperplanes.wsp.presentation.ui.activity.GameOverActivity"
    }
}

// Confetti particle data
data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val speed: Float,
    val amplitude: Float,
    val phase: Float
)

@Composable
fun GameOverScreen(
    gameData: GameData?,
    onReplayClicked: () -> Unit,
    onMainMenuClicked: () -> Unit
) {
    val knewaveFont = FontFamily(Font(R.font.knewave_regular))
    val bubblegumFont = FontFamily(Font(R.font.bubblegum_sans))
    val wordQuestFont = FontFamily(Font(R.font.word_quest))

    // Determine if it's a win or game over
    val isWin = gameData?.isGameOver != true

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Confetti animation
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    // Generate confetti particles
    val confettiParticles = remember {
        List(50) { index ->
            ConfettiParticle(
                id = index,
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                color = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFFFFE66D),
                    Color(0xFF95E1D3),
                    Color(0xFFF38181),
                    Color(0xFFAA96DA),
                    Color(0xFFFFD93D)
                ).random(),
                size = Random.nextFloat() * 12f + 6f,
                rotation = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 0.3f + 0.2f,
                amplitude = Random.nextFloat() * 40f + 20f,
                phase = Random.nextFloat() * 360f
            )
        }
    }

    // Background gradient based on win/lose
    val backgroundGradient = if (isWin) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA),
                Color(0xFF764BA2),
                Color(0xFFFF6B9D)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C3E50),
                Color(0xFF4A5568),
                Color(0xFF718096)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Confetti canvas (only for win)
        if (isWin && gameData != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiParticles.forEach { particle ->
                    val yProgress = ((confettiProgress * particle.speed + particle.y + 1f) % 1.5f)
                    val xOffset = sin((yProgress * 720 + particle.phase) * Math.PI / 180).toFloat() * particle.amplitude

                    val currentX = particle.x * size.width + xOffset
                    val currentY = yProgress * size.height * 1.2f

                    rotate(
                        degrees = particle.rotation + confettiProgress * 720f * particle.speed,
                        pivot = Offset(currentX, currentY)
                    ) {
                        drawRect(
                            color = particle.color,
                            topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                            size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.6f)
                        )
                    }
                }
            }
        }

        // Floating particles for game over
        if (!isWin && gameData != null) {
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "floatOffset"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(20) { i ->
                    val x = (i * 0.05f + floatOffset * 0.1f) % 1f
                    val y = sin((x * 4 + i * 0.3f) * Math.PI).toFloat() * 0.1f + 0.5f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = 20f + (i % 3) * 10f,
                        center = Offset(x * size.width, y * size.height)
                    )
                }
            }
        }

        // Loading state
        if (gameData == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy/Emoji section with animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) +
                            scaleIn(animationSpec = tween(600, easing = EaseOutBack))
                ) {
                    TrophySection(isWin = isWin)
                }

                // Title section
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 200)) { 50 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isWin) "CONGRATULATIONS!" else "GAME OVER",
                            fontFamily = knewaveFont,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = if (isWin) Color(0xFFFF3EA5) else Color.Black.copy(alpha = 0.5f),
                                    blurRadius = 20f,
                                    offset = Offset(0f, 4f)
                                )
                            )
                        )

                        if (isWin) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You did it! 🎉",
                                fontFamily = bubblegumFont,
                                fontSize = 20.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Stats card (only for win)
                if (isWin) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                                slideInVertically(animationSpec = tween(600, delayMillis = 400)) { 50 }
                    ) {
                        StatsCard(
                            gameData = gameData,
                            font = bubblegumFont
                        )
                    }
                } else {
                    // Encouraging message for game over
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 400))
                    ) {
                        Text(
                            text = "Don't give up!\nTry again! 💪",
                            fontFamily = bubblegumFont,
                            fontSize = 22.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    }
                }

                // Buttons section
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) +
                            slideInVertically(animationSpec = tween(600, delayMillis = 600)) { 100 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Replay button
                        GameOverButton(
                            text = "🔄  PLAY AGAIN",
                            font = wordQuestFont,
                            gradientColors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF45B649),
                                Color(0xFF2E7D32)
                            ),
                            onClick = onReplayClicked
                        )

                        // Main menu button
                        GameOverButton(
                            text = "🏠  MAIN MENU",
                            font = wordQuestFont,
                            gradientColors = listOf(
                                Color(0xFF3498DB),
                                Color(0xFF2980B9),
                                Color(0xFF1A5276)
                            ),
                            onClick = onMainMenuClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrophySection(isWin: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")

    // Pulsing glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Gentle bounce
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Rotation for game over
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.offset(y = bounceOffset.dp)
    ) {
        // Glow effect - clipped to circle before blur
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .blur(30.dp)
                .background(
                    Brush.radialGradient(
                        colors = if (isWin) listOf(
                            Color(0xFFFFD700).copy(alpha = 0.6f),
                            Color(0xFFFFD700).copy(alpha = 0.3f),
                            Color.Transparent
                        ) else listOf(
                            Color(0xFF718096).copy(alpha = 0.4f),
                            Color(0xFF718096).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Emoji
        Text(
            text = if (isWin) "🏆" else "😢",
            fontSize = 100.sp,
            modifier = Modifier.rotate(if (!isWin) rotation else 0f)
        )
    }
}

@Composable
fun StatsCard(
    gameData: GameData,
    font: FontFamily
) {
    val gridSize = "${gameData.grid?.rowCount ?: 0} × ${gameData.grid?.colCount ?: 0}"
    val wordsFound = gameData.usedWords.size.toString()
    val duration = DurationFormatter.fromInteger(gameData.duration)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF764BA2).copy(alpha = 0.3f),
                spotColor = Color(0xFF764BA2).copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 YOUR STATS",
                fontFamily = font,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    emoji = "🔲",
                    label = "Grid",
                    value = gridSize,
                    font = font
                )
                StatItem(
                    emoji = "📝",
                    label = "Words",
                    value = wordsFound,
                    font = font
                )
                StatItem(
                    emoji = "⏱️",
                    label = "Time",
                    value = duration,
                    font = font
                )
            }
        }
    }
}

@Composable
fun StatItem(
    emoji: String,
    label: String,
    value: String,
    font: FontFamily
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = font,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontFamily = font,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun GameOverButton(
    text: String,
    font: FontFamily,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .width(220.dp)
            .height(56.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = gradientColors[1].copy(alpha = 0.4f),
                spotColor = gradientColors[1].copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(colors = gradientColors)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = font,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.3f),
                    blurRadius = 4f,
                    offset = Offset(1f, 2f)
                )
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GameOverScreenWinPreview() {
    // Preview with mock win data
    GameOverScreen(
        gameData = null,
        onReplayClicked = {},
        onMainMenuClicked = {}
    )
}

