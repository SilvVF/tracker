package io.silv.tracker.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.FadeTransition
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.tracker.android.R
import io.silv.tracker.presentation.LogsViewScreenModel
import io.silv.tracker.presentation.LogsViewState
import io.silv.tracker.presentation.auth.AuthScreen

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

data class LogsViewActions(
    val logout: () -> Unit,
    val login: () -> Unit
)

class LogsViewScreen: Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<LogsViewScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        LogsViewScreenContent(
            state = state,
            actions = LogsViewActions(
                logout = screenModel::logout,
                login = {
                    navigator.push(AuthScreen())
                }
            )
        )
    }
}

@Composable
fun LogsViewScreenContent(
    state: LogsViewState,
    actions: LogsViewActions
) {
    Scaffold(
        topBar = {
            LogsViewTopBar(
                status = state.status,
                actions = actions
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewTopBar(
    status: SessionStatus,
    actions: LogsViewActions,
) {
    TopAppBar(
        title = { Text("Home") },
        actions = {
            AnimatedContent(
                targetState = status is SessionStatus.Authenticated,
                label = ""
            ) { authenticated ->
                val (icon, action, resId) = if (authenticated) {
                    Triple(Icons.Filled.Logout, actions.logout, R.string.sign_out)
                } else {
                    Triple(Icons.Filled.Login, actions.login, R.string.sign_in)
                }
                IconButton(onClick = { action() }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(id = resId)
                    )
                }
            }
        }
    )
}