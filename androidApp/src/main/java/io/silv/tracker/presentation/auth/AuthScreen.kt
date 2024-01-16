package io.silv.tracker.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.FormComponent
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import io.github.jan.supabase.compose.auth.ui.password.PasswordRule
import io.github.jan.supabase.compose.auth.ui.password.rememberPasswordRuleList
import io.github.jan.supabase.compose.auth.ui.phone.OutlinedPhoneField
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.Google
import io.silv.tracker.android.R
import io.silv.tracker.presentation.AuthScreenModel
import io.silv.tracker.presentation.AuthState
import io.silv.tracker.presentation.home.HomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class AuthActions(
    val signOut: () -> Unit,
    val signInWithGoogle: () -> Unit
)

class AuthScreen: Screen {

    @Composable
    override fun Content() {

        val auth = koinInject<ComposeAuth>()
        val screenModel = getScreenModel<AuthScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        fun showSnackBar(message: String) {
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }

        val signInWithGoogle = auth.rememberSignInWithGoogle(
            onResult = { result -> //optional error handling
                when (result) {
                    is NativeSignInResult.Success -> {
                        Toast.makeText(context, R.string.sign_in_success, Toast.LENGTH_SHORT).show()
                        if (navigator.canPop) {
                            navigator.pop()
                        } else {
                            navigator.replace(HomeScreen())
                        }
                    }
                    is NativeSignInResult.ClosedByUser -> Unit
                    is NativeSignInResult.Error -> showSnackBar(result.message)
                    is NativeSignInResult.NetworkError -> showSnackBar(result.message)
                }
            }
        )

        AuthScreenContent(
            state = state,
            snackbarHostState = snackbarHostState,
            actions = AuthActions(
                signOut = {},
                signInWithGoogle = signInWithGoogle::startFlow
            )
        )
    }
}

@OptIn(SupabaseExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenContent(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    actions: AuthActions
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {paddingValues ->
        AuthForm {
            var password by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf("") }
            var phone by rememberSaveable { mutableStateOf("") }

            val authState = LocalAuthState.current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(paddingValues)
            ) {
                OutlinedEmailField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    mandatory = email.isNotBlank() //once an email is entered, it is mandatory. (which enable validation)
                )
                OutlinedPhoneField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") }
                )
                OutlinedPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    rules = rememberPasswordRuleList(
                        PasswordRule.minLength(6),
                        PasswordRule.containsSpecialCharacter(),
                        PasswordRule.containsDigit(),
                        PasswordRule.containsLowercase(),
                        PasswordRule.containsUppercase()
                    )
                )
                FormComponent("accept_terms") { valid ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = valid.value,
                            onCheckedChange = { valid.value = it },
                        )
                        Text("Accept Terms", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                Button(
                    onClick = {}, //Login with email and password,
                    enabled = authState.validForm,
                ) {
                    Text("Login")
                }
                OutlinedButton(
                    onClick = {}, //Login with Google,
                    content = { ProviderButtonContent(Google) }
                )
            }
        }
    }
}