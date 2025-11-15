package com.aar.app.wsp.features.gamethemeselector

// Jetpack Compose Core
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Material / Material3
import androidx.compose.material3.Text

// Compose Runtime
import androidx.compose.runtime.Composable
// UI & Graphics
import androidx.compose.ui.graphics.Color
// Alignment, Sizes, Padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Text Font / FontFamily
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

// Icons (optional)
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import com.aar.app.wsp.R

// Preview
import androidx.compose.ui.tooling.preview.Preview
import com.aar.app.wsp.R.drawable.main_activity_bg


class ThemeSelectorActivityCompose : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContent {
    }
}
}



@Composable
fun SelectWordThemeScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Image(
            painter = painterResource(id = main_activity_bg), // 👉 replace with your image
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop // or FillBounds if needed
        )
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
                item{
                    TitleText("SELECT WORD THEME")

                }
                item{
                    AllThemesButton()

                }
                items(themesList){theme ->
                    CategoryItem(title = theme.title, subtitle = theme.subtitle, bgColor = theme.color)
                }
                item{
                    UpdateWordsButton()

                }
            }
            Spacer(modifier = Modifier.height(30.dp))


        }
    }
}
@Composable
fun TitleText(text: String) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = kidsFont,
        color = Color.White,
        modifier = Modifier.padding(8.dp),
        style = TextStyle(
            shadow = Shadow(
                color = Color(0xFFDB0F7D),
                blurRadius = 16f,
                offset = Offset(0f, 4f)
            )
        )
    )

}
@Composable
fun AllThemesButton() {

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFFEC84D))
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
                Text(
                    text = "All Themes",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(main_activity_bg),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
@Composable
fun CategoryItem(title: String, subtitle: String, bgColor: Color) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .height(75.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFFEC84D))
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }

                Image(
                    painter = painterResource(main_activity_bg),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
@Composable
fun UpdateWordsButton() {

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFFEC84D))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFF3EA5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Update words",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SelectWordTheme() {

    SelectWordThemeScreen()
}

data class ThemeItem(
    val title: String,
    val subtitle: String,
    val color: Color
)

val themesList = listOf(
    ThemeItem("Animals", "3 words", Color(0xFF9CE850)),
    ThemeItem("Body Parts", "31 words", Color(0xFF15DBD3)),
    ThemeItem("Foods", "19 words", Color(0xFFFFD352)),
    ThemeItem("City (Indonesia)", "19 words", Color(0xFFD2B7FF)),
    ThemeItem("Foods", "19 words", Color(0xFFE6FF53))

)
