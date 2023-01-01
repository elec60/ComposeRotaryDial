package com.hashem.mousavi.composerotarydial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashem.mousavi.composerotarydial.ui.theme.ComposeRotaryDialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeRotaryDialTheme {
                var number by remember {
                    mutableStateOf("")
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        20.dp,
                        alignment = Alignment.CenterVertically
                    )
                ) {
                    Text(text = number, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    RotaryDial(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onNumberDialed = {
                            number += it.toString()
                        }
                    )
                }
            }
        }
    }
}
