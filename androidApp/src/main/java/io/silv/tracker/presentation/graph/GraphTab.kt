package io.silv.tracker.presentation.graph

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AutoGraph
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object GraphTab: Tab {
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 1u,
            title = "Graph",
            icon = rememberVectorPainter(Icons.TwoTone.AutoGraph)
        )

    @Composable
    override fun Content() {

    }
}