package io.silv.tracker.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.FadeTransition
import io.silv.core_ui.ActionTab
import io.silv.tracker.presentation.logs.LogsCreateScreen
import io.silv.tracker.presentation.logs.LogsViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object HomeTab : ActionTab {

    private val addClickedChannel = Channel<Unit>(UNLIMITED)

    override fun onReselect() {
    }

    override fun onAddBottomBarClick() {
        addClickedChannel.trySend(Unit)
    }

    override val options: TabOptions
    @Composable get() = TabOptions(
        index = 0u,
        title = "Home",
        icon = rememberVectorPainter(Icons.TwoTone.Home)
    )

    @Composable
    override fun Content() {

        val lifecycle = LocalLifecycleOwner.current

        Navigator(LogsViewScreen()) { navigator ->

            FadeTransition(navigator)

            LaunchedEffect(addClickedChannel, lifecycle.lifecycle) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    withContext(Dispatchers.Main.immediate) {
                        addClickedChannel.receiveAsFlow().collect {
                            val screen = LogsCreateScreen()
                            when {
                                navigator.lastItem is LogsCreateScreen -> Unit
                                !navigator.items.contains(screen) -> navigator.push(screen)
                                else -> {
                                    val otherScreens = navigator.items.filterNot { it is LogsCreateScreen }
                                    navigator.replaceAll(otherScreens + screen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}