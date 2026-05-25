package com.aar.app.wsp.features.gamethemeselector

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aar.app.wsp.commons.PreviewDevices

private fun previewSampleThemes(): List<GameThemeItem> = listOf(
    GameThemeItem().apply { id = 1; name = "Animals"; wordsCount = 45 },
    GameThemeItem().apply { id = 2; name = "Body Parts"; wordsCount = 31 },
    GameThemeItem().apply { id = 3; name = "Foods"; wordsCount = 19 },
    GameThemeItem().apply { id = 4; name = "Countries"; wordsCount = 52 },
    GameThemeItem().apply { id = 5; name = "Fruits"; wordsCount = 86 }
)

@Preview(
    showBackground = true,
    widthDp = PreviewDevices.PHONE_WIDTH,
    heightDp = PreviewDevices.PHONE_HEIGHT,
    name = "Theme list - Phone"
)
@Preview(
    showBackground = true,
    widthDp = PreviewDevices.TABLET_WIDTH,
    heightDp = PreviewDevices.TABLET_HEIGHT,
    name = "Theme list - Tablet"
)
@Composable
fun SelectWordThemePreview() {
    MaterialTheme {
        SelectWordThemeScreen(
            themes = previewSampleThemes(),
            isLoading = false,
            revisionNumber = 5
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = PreviewDevices.PHONE_WIDTH,
    heightDp = PreviewDevices.PHONE_HEIGHT,
    name = "Theme loading"
)
@Composable
fun SelectWordThemeLoadingPreview() {
    MaterialTheme {
        SelectWordThemeScreen(
            themes = emptyList(),
            isLoading = true
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = PreviewDevices.PHONE_WIDTH,
    heightDp = 400,
    name = "Grid dialog - Phone"
)
@Preview(
    showBackground = true,
    widthDp = PreviewDevices.TABLET_WIDTH,
    heightDp = 500,
    name = "Grid dialog - Tablet"
)
@Composable
fun GridSizeSelectorDialogPreview() {
    MaterialTheme {
        GridSizeSelectorDialog(
            themeName = "Insects",
            availableGridSizes = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
            currentGridSize = 10,
            onGridSizeSelected = {},
            onDismiss = {}
        )
    }
}
