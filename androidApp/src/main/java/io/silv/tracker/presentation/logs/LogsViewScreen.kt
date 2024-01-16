package io.silv.tracker.presentation.logs

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.core_ui.SearchBarInputField
import io.silv.core_ui.SearchLargeTopBar
import io.silv.tracker.android.R
import io.silv.tracker.presentation.LogsViewScreenModel
import io.silv.tracker.presentation.LogsViewState
import io.silv.tracker.presentation.auth.AuthScreen

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
                },
                updateSearch = screenModel::updateSearch
            )
        )
    }
}


data class LogsViewActions(
    val logout: () -> Unit,
    val login: () -> Unit,
    val updateSearch: (query: String) -> Unit
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewScreenContent(
    state: LogsViewState,
    actions: LogsViewActions
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LogsViewTopBar(
                status = state.status,
                query = state.searchQuery,
                actions = actions,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsViewTopBar(
    status: SessionStatus,
    query: String,
    actions: LogsViewActions,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
) {
    SearchLargeTopBar(
        scrollBehavior = scrollBehavior,
        title = { Text("Home") },
        extraContent = {
            Box(Modifier.fillMaxWidth()) {
                SearchBarInputField(
                    query = query,
                    modifier = Modifier.padding(horizontal = 18.dp),
                    onQueryChange = { actions.updateSearch(it) },
                    placeholder = { Text("Search logs") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    }
                )
            }
        },
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