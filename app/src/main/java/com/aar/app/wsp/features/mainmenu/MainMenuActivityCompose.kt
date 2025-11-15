package com.aar.app.wsp.features.mainmenu

import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aar.app.wsp.R.drawable.*
import com.aar.app.wsp.R


class MainMenuActivityCompose : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GridSize()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Box(
) {
    Image(
        painter = painterResource(id = main_activity_bg), // 👉 replace with your image
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize(),
        contentScale = ContentScale.Crop // or FillBounds if needed
    )
    androidx.compose.foundation.layout.Box(
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


        Row(
            modifier = Modifier
                .fillMaxWidth()

                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding() + 10.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
            horizontalArrangement = Arrangement.Absolute.Center,
            verticalAlignment = Alignment.CenterVertically

        )
        {
            Image(
                painter = painterResource(id = word_search), // 👉 replace with your image
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            ) // or FillBounds if needed
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridSize()
            GameMode()
            PlayButton()
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .padding(top = 30.dp),
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly
            ) {
                CircleMenuButton(icon = R.drawable.ic_settings)

                CircleMenuButton(icon = R.drawable.ic_settings)
            }

        }

    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun GridSize() {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Box(
        modifier = Modifier
            .fillMaxWidth()
    )
    {
        Image(
            painter = painterResource(id = grid_sizee), // 👉 replace with your image
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        ) // or FillBounds if needed

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically


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
                Arrangement.SpaceAround,
                Alignment.CenterVertically
            ) {

                RoundIconButton(
                    icon = back_arrow,
                    0f,
                    onClick = {}
                )
                Text(
                    text = "4 x 4",
                    style = TextStyle(
                        fontFamily = kidsFont,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 20f,

                            offset = Offset(0f, 0f)
                        )
                    )
                )
                RoundIconButton(icon = back_arrow,
                    180f,
                    onClick = {}
                    )
            }
        }


    }

}






@Composable
fun GameMode() {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Box(
        modifier = Modifier
            .fillMaxWidth()
    )
    {
        Image(
            painter = painterResource(id = game_mode), // 👉 replace with your image
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        ) // or FillBounds if needed

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically


            ) {
                Text(
                    text = "GAME MODE",
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
                    .fillMaxWidth(),
                Arrangement.SpaceAround,
                Alignment.CenterVertically
            ) {

                RoundIconButton(icon = back_arrow, 0f, 30.dp)
                Text(
                    text = "Relax",
                    style = TextStyle(
                        fontFamily = kidsFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 20f,

                            offset = Offset(0f, 0f)
                        )
                    )
                )
                RoundIconButton(icon = back_arrow, 180f, 30.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                Arrangement.SpaceAround,
                Alignment.CenterVertically
            ) {

                RoundIconButton(icon = back_arrow, 0f, 30.dp)
                Text(
                    text = "EASY",
                    style = TextStyle(
                        fontFamily = kidsFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black,
                            blurRadius = 20f,

                            offset = Offset(0f, 0f)
                        )
                    )
                )
                RoundIconButton(icon = back_arrow, 180f, 30.dp)
            }

        }


    }

}
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayButton(
    text: String = "PLAY",
    onClick: () -> Unit = {}
) {
    val kidsFont = FontFamily(Font(R.font.knewave_regular))

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(10.dp)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {

        // Outer Yellow Border Capsule
        Box(
            modifier = Modifier
                .height(70.dp)
                .width(220.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFFD64A))   // Yellow border
        )

        // Inner Pink Capsule
        Box(
            modifier = Modifier
                .height(64.dp)
                .width(210.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFFFF3EA5))   // Pink background
        )

        // Text
        Text(
            text = text,
            style = TextStyle(
                fontSize = 32.sp,
                fontFamily = kidsFont,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                shadow = Shadow(
                    color = Color(0xFFCE2D88),
                    blurRadius = 18f,
                    offset = Offset(0f, 4f)
                )
            )
        )
    }
}

@Composable
fun CircleMenuButton(
    icon: Int,
    size: Dp = 70.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Yellow outer border
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFFFD64A))
        )

        // Pink inner circle
        Box(
            modifier = Modifier
                .size(size - 10.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF3EA5))
        )

        // White icon in center
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}




@Composable
fun RoundIconButton(
    icon: Int,
    rotate: Float,
    boxSize: Dp=70.dp,
    onClick: () -> Unit = {}) {
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
            modifier = Modifier.size(boxSize * 0.9f)
                .rotate(rotate)

        )
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun GameHomeScreenPreview() {

    Box()
}


@Composable
fun RoundButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF7BE86A))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModeButton(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFF7BE86A) else Color(0xFFBEE7FF))
            .padding(vertical = 12.dp, horizontal = 20.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF8AA4B3),
            fontWeight = FontWeight.Bold
        )
    }
}

