package com.aar.app.wsp.features.mainmenu

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview



class MainMenuActivityCompose : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
        }
    }

}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Box(){
    Column(){
        Text(text = "Hello")
    }
}


@Preview(showBackground = true)
@Composable
fun GameHomeScreenPreview() {

    Box()
}

