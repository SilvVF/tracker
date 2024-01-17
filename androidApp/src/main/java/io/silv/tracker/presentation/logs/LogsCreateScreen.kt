package io.silv.tracker.presentation.logs

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LogsCreateScreen: Screen {

    @Composable
    override fun Content() {

        val screenModel = getScreenModel<LogsCreateScreenModel>()

        LogsScreenContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreenContent(
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    val datePickerState = rememberDatePickerState()
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            DatePickerCalendar(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            DefaultImagePicker(
                onListReceived = {},
                onError = {
                    scope.launch {
                        snackBarHostState.showSnackbar(it)
                    }
                }
            )
        }
    }
}

@Composable
fun DefaultImagePicker(
    onListReceived: (List<Uri>) -> Unit,
    onError: (reason: String) -> Unit
) {
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            onListReceived(uriList)
        }


    Button(
        onClick = {
            try {
                galleryLauncher.launch("select images")
            } catch (e: ActivityNotFoundException) {
                onError("No Activity found to handle Intent")
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "")
            }
        }
    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerCalendar(
    state: DatePickerState,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        if (state.selectedDateMillis == null) {
            state.setSelection(Clock.System.now().toEpochMilliseconds())
        }
    }

    DatePicker(
        state = state,
        modifier = modifier,
        title = { Text("log time") },
        headline = {
            val time by remember(state) {
                derivedStateOf {
                    val date = Instant.fromEpochMilliseconds(state.selectedDateMillis
                        ?: return@derivedStateOf "Select a date")
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                    "${date.monthNumber}, ${date.dayOfMonth}, ${date.year}"
                }
            }
            Text(time)
        }
    )
}