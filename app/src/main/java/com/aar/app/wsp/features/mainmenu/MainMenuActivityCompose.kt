package com.aar.app.wsp.features.mainmenu

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aar.app.wsp.R.drawable.*


class MainMenuActivityCompose : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
        }
    }

}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Box(
    modifier: Modifier = Modifier
        .fillMaxSize()

    ){
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
    ){


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
                contentScale = ContentScale.Crop) // or FillBounds if needed
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GridSize()
            GameMode()
        }

    }
}



@Composable
fun GridSize(){

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
            contentScale = ContentScale.Crop) // or FillBounds if needed

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically


                ) {
                Text(
                    text = "Grid Size",
                    fontSize = 20.sp,

                    )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                Arrangement.SpaceAround,
                Alignment.CenterVertically
            ) {

                RoundIconButton(icon = back_arrow)
                Text("4 x 4",
                    fontSize = 20.sp,)
                RoundIconButton(icon = back_arrow)
            }
        }


    }

}
@Composable
fun GameMode(){

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
            contentScale = ContentScale.Crop) // or FillBounds if needed

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically


                ) {
                Text(
                    text = "Game Mode",
                    fontSize = 20.sp,

                    )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                Arrangement.SpaceAround,
                Alignment.CenterVertically
            ) {

                RoundIconButton(icon = back_arrow)
                Text("4 x 4",
                    fontSize = 20.sp,)
                RoundIconButton(icon = back_arrow)
            }
        }


    }

}


@Preview(showBackground = true)
@Composable
fun GridSizePreview() {

    GridSize()
}

@Preview(showBackground = true)
@Composable
fun GameModePreview() {

    GameMode()
}



@Composable
fun RoundIconButton(icon: Int, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}



@Preview(showBackground = true)
@Composable
fun GameHomeScreenPreview() {

    Box()
}

