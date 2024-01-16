package io.silv.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.core_ui.TrackerTheme
import io.silv.tracker.presentation.auth.AuthScreen
import io.silv.tracker.presentation.home.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrackerTheme {
                Surface {
                    Navigator(
                        screen = AuthScreen()
                    ) {
                        FadeTransition(it)
                    }
                }
            }
        }
    }
}
