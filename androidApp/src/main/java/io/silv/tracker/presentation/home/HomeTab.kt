package io.silv.tracker.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.tracker.presentation.logs.LogsViewScreen

object HomeTab : Tab {

    override val options: TabOptions
    @Composable get() = TabOptions(
        index = 0u,
        title = "Home",
        icon = rememberVectorPainter(Icons.TwoTone.Home)
    )

    @Composable
    override fun Content() {
        Navigator(LogsViewScreen()) {
            FadeTransition(it)
        }
    }
}