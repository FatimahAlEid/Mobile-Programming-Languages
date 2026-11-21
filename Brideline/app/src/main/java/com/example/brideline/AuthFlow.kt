package com.example.brideline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.example.brideline.ui.CreateAccountScreen
import com.example.brideline.ui.CreateAccountUiState
import com.example.brideline.ui.theme.BridelineTheme

@Composable
fun AuthFlow(
    modifier: Modifier = Modifier,
    onLogin: (String, String) -> Unit = { _, _ -> },
    onCreateAccount: (CreateAccountUiState) -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    // false = Login
    // true = Create Account
    var showCreateAccount by rememberSaveable {
        mutableStateOf(false)
    }

    // Login data
    var loginEmail by rememberSaveable {
        mutableStateOf("")
    }

    var loginPassword by remember {
        mutableStateOf("")
    }

    // Registration data stays available when switching screens.
    var createAccountState by remember {
        mutableStateOf(CreateAccountUiState())
    }

    if (showCreateAccount) {
        CreateAccountScreen(
            modifier = modifier,

            state = createAccountState,

            onStateChange = {
                createAccountState = it
            },

            onCreateAccount = { submittedState ->
                onCreateAccount(submittedState)
            },

            // Android system back button
            onBack = {
                focusManager.clearFocus()
                showCreateAccount = false
            },

            // Already have an account? Log in
            onLoginClick = {
                focusManager.clearFocus()
                showCreateAccount = false
            }
        )
    } else {
        LoginScreen(
            modifier = modifier,

            emailValue = loginEmail,
            passwordValue = loginPassword,

            onEmailChange = {
                loginEmail = it
            },

            onPasswordChange = {
                loginPassword = it
            },

            onLoginClick = {
                focusManager.clearFocus()

                onLogin(
                    loginEmail,
                    loginPassword
                )
            },

            onForgotPasswordClick = onForgotPassword,

            // Don't have an account? Create an account
            onCreateAccountClick = {
                focusManager.clearFocus()
                showCreateAccount = true
            }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun AuthFlowPreview() {
    BridelineTheme {
        AuthFlow()
    }
}