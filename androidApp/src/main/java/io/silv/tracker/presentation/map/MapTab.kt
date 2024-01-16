package io.silv.tracker.presentation.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AutoGraph
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material.icons.twotone.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object MapTab: Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 2u,
            title = "Map",
            icon = rememberVectorPainter(Icons.TwoTone.LocationOn)
        )

    @Composable
    override fun Content() {

    }
}