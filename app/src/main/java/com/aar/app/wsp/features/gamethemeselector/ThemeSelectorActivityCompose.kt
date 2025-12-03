package com.aar.app.wsp.features.gamethemeselector

// Jetpack Compose Core
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

// Material / Material3
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text

// Compose Runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
// UI & Graphics
import androidx.compose.ui.graphics.Color
// Alignment, Sizes, Padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Text Font / FontFamily
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// Icons (optional)
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import com.aar.app.wsp.R
import com.aar.app.wsp.WordSearchApp
import com.aar.app.wsp.model.GameTheme

// Preview
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ThemeSelectorActivityCompose : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ThemeSelectorViewModel
    private var updateDisposable: Disposable? = null

    private val gridRowCount: Int by lazy {
        intent.extras?.getInt(EXTRA_ROW_COUNT) ?: 0
    }

    private val gridColCount: Int by lazy {
        intent.extras?.getInt(EXTRA_COL_COUNT) ?: 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordSearchApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)[ThemeSelectorViewModel::class.java]
        viewModel.loadThemes()

        setContent {
            val themes by viewModel.onGameThemeLoaded.observeAsState(initial = emptyList())
            var isLoading by remember { mutableStateOf(themes.isEmpty()) }
            var isUpdating by remember { mutableStateOf(false) }
            
            // State for grid size selector dialog
            var showGridSizeDialog by remember { mutableStateOf(false) }
            var pendingThemeId by remember { mutableStateOf(0) }
            var pendingThemeName by remember { mutableStateOf("") }
            var availableGridSizes by remember { mutableStateOf<List<Int>>(emptyList()) }

            // Update loading state when themes change
            if (themes.isNotEmpty()) {
                isLoading = false
            }

            SelectWordThemeScreen(
                themes = themes,
                isLoading = isLoading,
                isUpdating = isUpdating,
                revisionNumber = viewModel.lastDataRevision,
                onAllThemesClick = { 
                    onThemeSelected(GameTheme.NONE.id, "All Themes") { sizes ->
                        pendingThemeId = GameTheme.NONE.id
                        pendingThemeName = "All Themes"
                        availableGridSizes = sizes
                        showGridSizeDialog = true
                    }
                },
                onThemeClick = { themeItem -> 
                    onThemeSelected(themeItem.id, themeItem.name.ifEmpty { "Theme" }) { sizes ->
                        pendingThemeId = themeItem.id
                        pendingThemeName = themeItem.name.ifEmpty { "Theme" }
                        availableGridSizes = sizes
                        showGridSizeDialog = true
                    }
                },
                onUpdateClick = {
                    isUpdating = true
                    updateDisposable = viewModel.updateData()
                        .subscribe({ responseType ->
                            isUpdating = false
                            val message = if (responseType == ThemeSelectorViewModel.ResponseType.NoUpdate) {
                                getString(R.string.up_to_date)
                            } else {
                                getString(R.string.update_success)
                            }
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        }, {
                            isUpdating = false
                            Toast.makeText(this, R.string.err_no_connect, Toast.LENGTH_LONG).show()
                        })
                }
            )
            
            // Grid Size Selector Dialog
            if (showGridSizeDialog) {
                GridSizeSelectorDialog(
                    themeName = pendingThemeName,
                    availableGridSizes = availableGridSizes,
                    currentGridSize = gridRowCount,
                    onGridSizeSelected = { selectedSize ->
                        showGridSizeDialog = false
                        returnWithNewGridSize(pendingThemeId, pendingThemeName, selectedSize)
                    },
                    onDismiss = { showGridSizeDialog = false }
                )
            }
        }
    }

    private fun onThemeSelected(themeId: Int, themeName: String, onNoWords: (List<Int>) -> Unit) {
        viewModel.checkWordAvailability(themeId, gridRowCount, gridColCount)
            .subscribe { available ->
                if (available) {
                    val intent = Intent()
                    intent.putExtra(EXTRA_THEME_ID, themeId)
                    intent.putExtra(EXTRA_THEME_NAME, themeName)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    // Get available grid sizes and show dialog
                    viewModel.getAvailableGridSizes(themeId)
                        .subscribe { sizes ->
                            if (sizes.isNotEmpty()) {
                                onNoWords(sizes)
                            } else {
                                Toast.makeText(
                                    this,
                                    "No words available for this theme",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
    }
    
    private fun returnWithNewGridSize(themeId: Int, themeName: String, gridSize: Int) {
        val intent = Intent()
        intent.putExtra(EXTRA_THEME_ID, themeId)
        intent.putExtra(EXTRA_THEME_NAME, themeName)
        intent.putExtra(EXTRA_SELECTED_GRID_SIZE, gridSize)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        updateDisposable?.dispose()
    }

    companion object {
        const val EXTRA_THEME_ID = "game_theme_id"
        const val EXTRA_THEME_NAME = "game_theme_name"
        const val EXTRA_ROW_COUNT = "row_count"
        const val EXTRA_COL_COUNT = "col_count"
        const val EXTRA_SELECTED_GRID_SIZE = "selected_grid_size"
    }
}



@Composable
fun SelectWordThemeScreen(
    themes: List<GameThemeItem>,
    isLoading: Boolean = false,
    isUpdating: Boolean = false,
    revisionNumber: Int = 0,
    onAllThemesClick: () -> Unit = {},
    onThemeClick: (GameThemeItem) -> Unit = {},
    onUpdateClick: () -> Unit = {}
) {
    // Predefined colors for theme items
    val themeColors = listOf(
        Color(0xFF9CE850),  // Green
        Color(0xFF15DBD3),  // Cyan
        Color(0xFFFFD352),  // Yellow
        Color(0xFFD2B7FF),  // Light Purple
        Color(0xFFE6FF53),  // Lime
        Color(0xFFFF8A65),  // Orange
        Color(0xFF81D4FA),  // Light Blue
        Color(0xFFF48FB1),  // Pink
        Color(0xFFCE93D8),  // Purple
        Color(0xFFA5D6A7)   // Light Green
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.main_activity_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isLoading) {
            // Loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF3EA5))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    item {
                        Image(
                            painter = painterResource(id = R.drawable.select_word_theme),
                            contentDescription = "Select Word Theme",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp, vertical = 8.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                    item {
                        AllThemesButton(onClick = onAllThemesClick)
                    }
                    items(themes) { theme ->
                        val colorIndex = (theme.id - 1) % themeColors.size
                        CategoryItem(
                            title = theme.name,
                            subtitle = "${theme.wordsCount} words",
                            bgColor = themeColors[colorIndex],
                            onClick = { onThemeClick(theme) }
                        )
                    }
                    item {
                        UpdateWordsButton(
                            isUpdating = isUpdating,
                            onClick = onUpdateClick
                        )
                    }
                    item {
                        // Revision number
                        Text(
                            text = "Rev: ${if (revisionNumber > 0) revisionNumber else "-"}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Updating overlay
        if (isUpdating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF3EA5))
            }
        }
    }
}
@Composable
fun TitleText(text: String) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    Box(
        modifier = Modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Stroke text (behind)
        Text(
            text = text,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = kidsFont,
            color = Color(0xFFDB0F7D),
            style = TextStyle(
                drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 12f
                )
            )
        )
        // Fill text (on top)
        Text(
            text = text,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = kidsFont,
            color = Color.White
        )
    }

}
@Composable
fun AllThemesButton(onClick: () -> Unit = {}) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFFEC84D))
            .clickable { onClick() }
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF8F6CD9)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "All Themes",
                        fontSize = 24.sp,
                        color = Color(0xFF5A3D8A),
                        fontFamily = kidsFont,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                    )
                    Text(
                        text = "All Themes",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontFamily = kidsFont,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "🎲",
                    fontSize = 24.sp
                )
            }
        }
    }
}
@Composable
fun CategoryItem(
    title: String,
    subtitle: String,
    bgColor: Color,
    onClick: () -> Unit = {}
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .height(75.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFF3BC41))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(bgColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontFamily = kidsFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                            )
                        )
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontFamily = kidsFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "▶",
                    fontSize = 24.sp,
                    color = Color.Black.copy(alpha = 0.4f)
                )
            }
        }
    }
}
@Composable
fun UpdateWordsButton(
    isUpdating: Boolean = false,
    onClick: () -> Unit = {}
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFFEC84D))
            .clickable(enabled = !isUpdating) { onClick() }
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(40.dp))
                .background(if (isUpdating) Color(0xFFCC3285) else Color(0xFFFF3EA5)),
            contentAlignment = Alignment.Center
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Update Words",
                        fontSize = 24.sp,
                        fontFamily = kidsFont,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAA2070),
                        style = TextStyle(
                            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                    )
                    Text(
                        text = "Update Words",
                        fontSize = 24.sp,
                        fontFamily = kidsFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun GridSizeSelectorDialog(
    themeName: String,
    availableGridSizes: List<Int>,
    currentGridSize: Int,
    onGridSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))
    
    // Grid colors matching theme selector
    val gridColors = listOf(
        Color(0xFF9CE850),  // Green
        Color(0xFF15DBD3),  // Cyan
        Color(0xFFFFD352),  // Yellow
        Color(0xFFD2B7FF),  // Light Purple
        Color(0xFFE6FF53),  // Lime
        Color(0xFFFF8A65),  // Orange
        Color(0xFF81D4FA),  // Light Blue
        Color(0xFFF48FB1),  // Pink
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFF3BC41))  // Golden border
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFFF6EB5))  // Pink background like theme selector
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title with stroke effect
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Select Grid Size",
                            fontSize = 26.sp,
                            fontFamily = kidsFont,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAA2070),
                            style = TextStyle(
                                drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f)
                            )
                        )
                        Text(
                            text = "Select Grid Size",
                            fontSize = 26.sp,
                            fontFamily = kidsFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Message in a bubble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Grid ${currentGridSize}x${currentGridSize} needs different words!\nPick a grid size for \"$themeName\":",
                            fontSize = 14.sp,
                            fontFamily = kidsFont,
                            color = Color(0xFF6B4E71),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Grid size buttons in scrollable row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(availableGridSizes) { size ->
                            val colorIndex = (size - 3) % gridColors.size
                            GridSizeButton(
                                size = size,
                                bgColor = gridColors[colorIndex],
                                onClick = { onGridSizeSelected(size) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Cancel button - styled like other buttons
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFFF3BC41))  // Golden border
                            .padding(4.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF8F6CD9))  // Purple
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Cancel",
                                fontSize = 18.sp,
                                fontFamily = kidsFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5A3D8A),
                                style = TextStyle(
                                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                                )
                            )
                            Text(
                                text = "Cancel",
                                fontSize = 18.sp,
                                fontFamily = kidsFont,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridSizeButton(
    size: Int,
    bgColor: Color,
    onClick: () -> Unit
) {
    val kidsFont = FontFamily(Font(R.font.word_quest))
    
    Box(
        modifier = Modifier
            .size(65.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3BC41))  // Golden border
            .padding(4.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Grid size text with stroke
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "${size}x${size}",
                    fontSize = 18.sp,
                    fontFamily = kidsFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                )
                Text(
                    text = "${size}x${size}",
                    fontSize = 18.sp,
                    fontFamily = kidsFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectWordThemePreview() {
    // Preview with sample data
    val sampleThemes = listOf(
        GameThemeItem().apply { id = 1; name = "Animals"; wordsCount = 45 },
        GameThemeItem().apply { id = 2; name = "Body Parts"; wordsCount = 31 },
        GameThemeItem().apply { id = 3; name = "Foods"; wordsCount = 19 },
        GameThemeItem().apply { id = 4; name = "Countries"; wordsCount = 52 },
        GameThemeItem().apply { id = 5; name = "Fruits"; wordsCount = 86 }
    )

    SelectWordThemeScreen(
        themes = sampleThemes,
        isLoading = false,
        revisionNumber = 5
    )
}

@Preview(showBackground = true)
@Composable
fun GridSizeSelectorDialogPreview() {
    GridSizeSelectorDialog(
        themeName = "Insects",
        availableGridSizes = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        currentGridSize = 10,
        onGridSizeSelected = {},
        onDismiss = {}
    )
}
