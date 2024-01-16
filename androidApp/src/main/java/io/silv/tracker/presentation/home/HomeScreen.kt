package io.silv.tracker.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.tracker.android.R
import io.silv.tracker.presentation.HomeScreenModel
import io.silv.tracker.presentation.HomeState
import io.silv.tracker.presentation.auth.AuthScreen

class HomeScreen: Screen {

    @Composable
    override fun Content() {

        val screenModel = getScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        HomeScreenContent(
            state = state,
            actions = HomeActions(
                logout = screenModel::logout,
                login = { navigator.push(AuthScreen()) }
            )
        )
    }
}

data class HomeActions(
    val logout: () -> Unit,
    val login: () -> Unit
)

@Composable
fun HomeScreenContent(
    state: HomeState,
    actions: HomeActions
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                status = state.status,
                actions = actions
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    status: SessionStatus,
    actions: HomeActions,
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