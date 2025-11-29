package com.aar.app.wsp.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.aar.app.wsp.R
import com.aar.app.wsp.custom.StreakView
import kotlin.math.sin
import kotlin.random.Random

class SettingsActivityCompose : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        setContent {
            SettingsScreen(
                sharedPrefs = sharedPrefs,
                onBackClicked = { finish() },
                onPrivacyPolicyClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://abdularis.com/app/wsp/privacy_policy.html"))
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    sharedPrefs: android.content.SharedPreferences,
    onBackClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit
) {
    val context = LocalContext.current
    val kidsFont = FontFamily(Font(R.font.word_quest))

    // Preference keys
    val keyEnableSound = context.getString(R.string.pref_enableSound)
    val keyEnableFullscreen = context.getString(R.string.pref_enableFullscreen)
    val keyReverseMatching = context.getString(R.string.pref_reverseMatching)
    val keyDeleteAfterFinish = context.getString(R.string.pref_deleteAfterFinish)
    val keyAutoScaleGrid = context.getString(R.string.pref_autoScaleGrid)
    val keyGrayscale = context.getString(R.string.pref_grayscale)
    val keyShowGridLine = context.getString(R.string.pref_showGridLine)
    val keySnapToGrid = context.getString(R.string.pref_snapToGrid)
    val defSnapToGrid = context.getString(R.string.snap_to_grid_def_val)

    // State for settings
    var enableSound by remember { mutableStateOf(sharedPrefs.getBoolean(keyEnableSound, true)) }
    var enableFullscreen by remember { mutableStateOf(sharedPrefs.getBoolean(keyEnableFullscreen, true)) }
    var reverseMatching by remember { mutableStateOf(sharedPrefs.getBoolean(keyReverseMatching, true)) }
    var deleteAfterFinish by remember { mutableStateOf(sharedPrefs.getBoolean(keyDeleteAfterFinish, true)) }
    var autoScaleGrid by remember { mutableStateOf(sharedPrefs.getBoolean(keyAutoScaleGrid, true)) }
    var grayscale by remember { mutableStateOf(sharedPrefs.getBoolean(keyGrayscale, false)) }
    var showGridLine by remember { mutableStateOf(sharedPrefs.getBoolean(keyShowGridLine, false)) }
    var snapToGrid by remember { 
        mutableStateOf(sharedPrefs.getString(keySnapToGrid, defSnapToGrid) != StreakView.SnapType.NONE.name) 
    }

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                    CircleBackButton(onClick = onBackClicked)
                }

                // Title in center
                SettingsTitle(text = "Settings", kidsFont = kidsFont)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enable Sound Setting
            SettingsToggleRow(
                icon = R.drawable.ic_sound,
                title = "Enable Sound",
                isChecked = enableSound,
                kidsFont = kidsFont,
                onCheckedChange = { checked ->
                    enableSound = checked
                    sharedPrefs.edit().putBoolean(keyEnableSound, checked).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fullscreen Setting
            SettingsToggleRow(
                icon = R.drawable.ic_fullscreen,
                title = "Fullscreen",
                isChecked = enableFullscreen,
                kidsFont = kidsFont,
                onCheckedChange = { checked ->
                    enableFullscreen = checked
                    sharedPrefs.edit().putBoolean(keyEnableFullscreen, checked).apply()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Game Play Section Header
            SectionHeader(title = "Game Play", kidsFont = kidsFont)

            Spacer(modifier = Modifier.height(16.dp))

            // Reverse matching
            SettingsToggleRow(
                icon = R.drawable.ic_reverse_match,
                title = "Reverse matching",
                isChecked = reverseMatching,
                kidsFont = kidsFont,
                onCheckedChange = { checked ->
                    reverseMatching = checked
                    sharedPrefs.edit().putBoolean(keyReverseMatching, checked).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Finish
            SettingsOnOffRow(
                icon = R.drawable.ic_delete_after_finish,
                title = "Delete Finish",
                isOn = deleteAfterFinish,
                kidsFont = kidsFont,
                onToggle = { isOn ->
                    deleteAfterFinish = isOn
                    sharedPrefs.edit().putBoolean(keyDeleteAfterFinish, isOn).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Auto Scale Grid
            SettingsOnOffRow(
                icon = R.drawable.ic_auto_scale,
                title = "Auto Scale Grid",
                isOn = autoScaleGrid,
                kidsFont = kidsFont,
                onToggle = { isOn ->
                    autoScaleGrid = isOn
                    sharedPrefs.edit().putBoolean(keyAutoScaleGrid, isOn).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grayscale
            SettingsOnOffRow(
                icon = R.drawable.ic_grayscale,
                title = "Grayscale",
                isOn = grayscale,
                kidsFont = kidsFont,
                onToggle = { isOn ->
                    grayscale = isOn
                    sharedPrefs.edit().putBoolean(keyGrayscale, isOn).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show Grid Line
            SettingsOnOffRow(
                icon = R.drawable.ic_grid,
                title = "Show Grid Line",
                isOn = showGridLine,
                kidsFont = kidsFont,
                onToggle = { isOn ->
                    showGridLine = isOn
                    sharedPrefs.edit().putBoolean(keyShowGridLine, isOn).apply()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Snap To Grid
            SettingsOnOffRow(
                icon = R.drawable.ic_snap_grid,
                title = "Snap To Grid",
                isOn = snapToGrid,
                kidsFont = kidsFont,
                onToggle = { isOn ->
                    snapToGrid = isOn
                    val snapValue = if (isOn) StreakView.SnapType.ALWAYS_SNAP.name else StreakView.SnapType.NONE.name
                    sharedPrefs.edit().putString(keySnapToGrid, snapValue).apply()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // About Section Header
            SectionHeader(title = "About", kidsFont = kidsFont)

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy
            AboutRow(
                title = "Privacy Policy",
                kidsFont = kidsFont,
                onClick = onPrivacyPolicyClicked
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Version
            AboutRow(
                title = "Version",
                subtitle = "1.0",
                kidsFont = kidsFont,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CircleBackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF4FC3F7))
            .border(4.dp, Color(0xFFFEC84D), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Arrow icon
        Canvas(modifier = Modifier.size(24.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.2f)
                lineTo(size.width * 0.3f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.8f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun SettingsTitle(text: String, kidsFont: FontFamily) {
    Box {
        // Black outline/shadow
        Text(
            text = text,
            fontFamily = kidsFont,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            style = TextStyle(
                drawStyle = Stroke(width = 12f)
            )
        )
        // White fill
        Text(
            text = text,
            fontFamily = kidsFont,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: Int,
    title: String,
    isChecked: Boolean,
    kidsFont: FontFamily,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFF7DD3F4))
            .border(4.dp, Color(0xFFFEC84D), RoundedCornerShape(30.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon in circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B9D)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontFamily = kidsFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            // Custom Toggle Switch
            CustomToggleSwitch(
                isChecked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }

        // Highlight shine effect
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 8.dp)
                .size(width = 60.dp, height = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun CustomToggleSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isChecked) 24.dp else 0.dp,
        animationSpec = tween(200),
        label = "thumbOffset"
    )

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isChecked) Color(0xFF7AC943) else Color(0xFFCCCCCC))
            .clickable { onCheckedChange(!isChecked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun SettingsOnOffRow(
    icon: Int,
    title: String,
    isOn: Boolean,
    kidsFont: FontFamily,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFF7DD3F4))
            .border(4.dp, Color(0xFFFEC84D), RoundedCornerShape(30.dp))
            .clickable { onToggle(!isOn) }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon in circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF26C6B0)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontFamily = kidsFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            // ON/OFF pill button
            OnOffButton(isOn = isOn, onClick = { onToggle(!isOn) })
        }

        // Highlight shine effect
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 8.dp)
                .size(width = 60.dp, height = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun OnOffButton(isOn: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isOn) Color(0xFFB8E986) else Color(0xFFE0E0E0))
            .border(2.dp, Color(0xFF999999), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isOn) "ON" else "OFF",
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun SectionHeader(title: String, kidsFont: FontFamily) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        // Green scalloped/wavy background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            val scallopsCount = 20
            val scallowWidth = size.width / scallopsCount
            val scallowHeight = 12f

            // Top edge with scallops
            path.moveTo(0f, scallowHeight)
            for (i in 0 until scallopsCount) {
                val startX = i * scallowWidth
                val midX = startX + scallowWidth / 2
                val endX = startX + scallowWidth
                path.quadraticBezierTo(midX, 0f, endX, scallowHeight)
            }

            // Right edge
            path.lineTo(size.width, size.height - scallowHeight)

            // Bottom edge with scallops
            for (i in scallopsCount - 1 downTo 0) {
                val startX = (i + 1) * scallowWidth
                val midX = startX - scallowWidth / 2
                val endX = i * scallowWidth
                path.quadraticBezierTo(midX, size.height, endX, size.height - scallowHeight)
            }

            // Left edge
            path.lineTo(0f, scallowHeight)
            path.close()

            // Draw green fill
            drawPath(path, Color(0xFF9ACD32))

            // Draw yellow border
            drawPath(
                path,
                Color(0xFFFEC84D),
                style = Stroke(width = 4f)
            )
        }

        Text(
            text = title,
            fontFamily = kidsFont,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFF5D7A1E),
                    offset = Offset(2f, 2f),
                    blurRadius = 0f
                )
            )
        )
    }
}

@Composable
fun AboutRow(
    title: String,
    subtitle: String? = null,
    kidsFont: FontFamily,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFB19CD9))
            .border(4.dp, Color(0xFFFEC84D), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (subtitle != null) {
            Column {
                Text(
                    text = title,
                    fontFamily = kidsFont,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = subtitle,
                    fontFamily = kidsFont,
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
            }
        } else {
            Text(
                text = title,
                fontFamily = kidsFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        // Highlight shine effect
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = 8.dp)
                .size(width = 80.dp, height = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun FloatingBubbles() {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    // Create multiple bubbles with different animations
    val bubbles = remember {
        List(8) {
            BubbleData(
                startX = Random.nextFloat(),
                startY = Random.nextFloat() * 0.3f + 0.7f,
                size = Random.nextFloat() * 40f + 30f,
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
                // Outer bubble
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = bubble.size,
                    center = Offset(x, y)
                )
                // Inner highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
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
            Offset(0.85f, 0.15f),
            Offset(0.9f, 0.35f),
            Offset(0.1f, 0.25f),
            Offset(0.15f, 0.55f),
            Offset(0.88f, 0.65f),
            Offset(0.12f, 0.85f)
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

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000 + index * 500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sparkle_rotation_$index"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = position.x * size.width
            val y = position.y * size.height

            // Draw 4-pointed star
            drawStar(
                center = Offset(x, y),
                size = 12f,
                color = Color.White.copy(alpha = alpha),
                rotation = rotation
            )
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    val path = Path().apply {
        val angleStep = 90f
        for (i in 0 until 4) {
            val angle = Math.toRadians((rotation + i * angleStep).toDouble())
            val outerX = center.x + size * kotlin.math.cos(angle).toFloat()
            val outerY = center.y + size * kotlin.math.sin(angle).toFloat()

            if (i == 0) {
                moveTo(outerX, outerY)
            } else {
                lineTo(outerX, outerY)
            }

            val innerAngle = Math.toRadians((rotation + i * angleStep + 45).toDouble())
            val innerX = center.x + size * 0.3f * kotlin.math.cos(innerAngle).toFloat()
            val innerY = center.y + size * 0.3f * kotlin.math.sin(innerAngle).toFloat()
            lineTo(innerX, innerY)
        }
        close()
    }
    drawPath(path, color)
}

// ==================== PREVIEW ====================

@Composable
fun SettingsScreenPreviewContent() {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    // Preview state
    var enableSound by remember { mutableStateOf(true) }
    var enableFullscreen by remember { mutableStateOf(true) }
    var reverseMatching by remember { mutableStateOf(true) }
    var deleteAfterFinish by remember { mutableStateOf(true) }
    var autoScaleGrid by remember { mutableStateOf(true) }
    var grayscale by remember { mutableStateOf(false) }
    var showGridLine by remember { mutableStateOf(false) }
    var snapToGrid by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with back button and title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.align(Alignment.CenterStart)) {
                    CircleBackButton(onClick = {})
                }
                SettingsTitle(text = "Settings", kidsFont = kidsFont)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enable Sound
            SettingsToggleRow(
                icon = R.drawable.ic_sound,
                title = "Enable Sound",
                isChecked = enableSound,
                kidsFont = kidsFont,
                onCheckedChange = { enableSound = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fullscreen
            SettingsToggleRow(
                icon = R.drawable.ic_fullscreen,
                title = "Fullscreen",
                isChecked = enableFullscreen,
                kidsFont = kidsFont,
                onCheckedChange = { enableFullscreen = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Game Play", kidsFont = kidsFont)
            Spacer(modifier = Modifier.height(16.dp))

            // Reverse matching
            SettingsToggleRow(
                icon = R.drawable.ic_reverse_match,
                title = "Reverse matching",
                isChecked = reverseMatching,
                kidsFont = kidsFont,
                onCheckedChange = { reverseMatching = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Finish
            SettingsOnOffRow(
                icon = R.drawable.ic_delete_after_finish,
                title = "Delete Finish",
                isOn = deleteAfterFinish,
                kidsFont = kidsFont,
                onToggle = { deleteAfterFinish = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Auto Scale Grid
            SettingsOnOffRow(
                icon = R.drawable.ic_auto_scale,
                title = "Auto Scale Grid",
                isOn = autoScaleGrid,
                kidsFont = kidsFont,
                onToggle = { autoScaleGrid = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grayscale
            SettingsOnOffRow(
                icon = R.drawable.ic_grayscale,
                title = "Grayscale",
                isOn = grayscale,
                kidsFont = kidsFont,
                onToggle = { grayscale = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show Grid Line
            SettingsOnOffRow(
                icon = R.drawable.ic_grid,
                title = "Show Grid Line",
                isOn = showGridLine,
                kidsFont = kidsFont,
                onToggle = { showGridLine = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Snap To Grid
            SettingsOnOffRow(
                icon = R.drawable.ic_snap_grid,
                title = "Snap To Grid",
                isOn = snapToGrid,
                kidsFont = kidsFont,
                onToggle = { snapToGrid = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "About", kidsFont = kidsFont)
            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Policy
            AboutRow(
                title = "Privacy Policy",
                kidsFont = kidsFont,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Version
            AboutRow(
                title = "Version",
                subtitle = "1.0",
                kidsFont = kidsFont,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreenPreviewContent()
}

