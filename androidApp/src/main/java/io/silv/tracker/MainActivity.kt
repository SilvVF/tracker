package io.silv.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.core_ui.TrackerTheme
import io.silv.tracker.presentation.auth.AuthScreen
import io.silv.tracker.presentation.home.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TrackerTheme {
                Surface {
                    Navigator(
                        screen = HomeScreen()
                    ) {
                        FadeTransition(it)
                    }
                }
            }
        }
    }
}
