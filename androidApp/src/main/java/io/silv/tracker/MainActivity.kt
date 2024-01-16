package io.silv.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.twotone.AutoGraph
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.core_ui.TrackerTheme
import io.silv.tracker.android.R
import io.silv.tracker.presentation.graph.GraphTab
import io.silv.tracker.presentation.home.HomeTab
import io.silv.tracker.presentation.map.MapTab

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TrackerTheme {
                Surface {
                    TabNavigator(tab = HomeTab) {
                        Scaffold(
                            bottomBar = { DefaultBottomAppBar() }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .consumeWindowInsets(paddingValues)
                            ) {

                                CrossfadeTransition(navigator = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrossfadeTransition(
    navigator: TabNavigator
) {
    navigator.saveableState("currentTab") {
        Crossfade(
            targetState = navigator.current,
            label = "tab-crossfade"
        ) { tab ->
            tab.Content()
        }
    }
}

@Composable
fun DefaultBottomAppBar() {

    val tabNavigator = LocalTabNavigator.current
    val current = tabNavigator.current

    val tabs = remember { listOf(HomeTab, GraphTab, MapTab) }

    BottomAppBar(
        actions = {
            tabs.fastForEach {
                NavigationBarItem(
                    selected = current == it,
                    onClick = { tabNavigator.current = it },
                    icon = {
                        it.options.icon?.let { painter ->
                            Icon(
                                painter = painter,
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(it.options.title) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = RoundedCornerShape(12),
                onClick = {  }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    stringResource(id = R.string.log_poop)
                )
            }
        }
    )
}
