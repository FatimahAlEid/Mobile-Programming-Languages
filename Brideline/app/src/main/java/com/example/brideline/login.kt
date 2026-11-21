package com.example.brideline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.ui.theme.BridelineTheme

private val PrimaryRoseColor = Color(0xFFD2647D)
private val AccentFieldColor = Color(0xFFEDBEC8)
private val ScreenBackgroundColor = Color(0xFFF2F4F8)
private val ScreenBorderColor = Color(0xFFEDBEC8)
private val DarkTextColor = Color(0xFF2D2326)
private val MutedTextColor = Color(0xFF78666E)
private val DividerLineColor = Color(0xFFD6B9C0)
private val ErrorBackgroundColor = Color(0xFFF8E2E7)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    emailValue: String = "",
    passwordValue: String = "",
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    logoPainter: Painter? = null
) {
    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    // Loads your uploaded bride_line_logo.png.
    val effectiveLogoPainter = logoPainter
        ?: painterResource(id = R.drawable.bride_line_logo)

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = AccentFieldColor,
        unfocusedContainerColor = AccentFieldColor,
        disabledContainerColor = AccentFieldColor,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = DarkTextColor,
        unfocusedTextColor = DarkTextColor,
        cursorColor = PrimaryRoseColor
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ScreenBackgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = ScreenBackgroundColor,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = ScreenBorderColor
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 0.dp,
                            bottom = 24.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = effectiveLogoPainter,
                        contentDescription = "Bride Line Logo",
                        modifier = Modifier
                            .size(330.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorBackgroundColor
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                color = PrimaryRoseColor,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Email Address",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextColor
                        )

                        TextField(
                            value = emailValue,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter your email address",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = fieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Password",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextColor
                        )

                        TextField(
                            value = passwordValue,
                            onValueChange = onPasswordChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation =
                                if (isPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                            trailingIcon = {
                                Text(
                                    text = if (isPasswordVisible) {
                                        "Hide"
                                    } else {
                                        "Show"
                                    },
                                    color = DarkTextColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clickable {
                                            isPasswordVisible =
                                                !isPasswordVisible
                                        }
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!isLoading) {
                                        onLoginClick()
                                    }
                                }
                            ),
                            colors = fieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Forgot password?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextColor,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(
                                onClick = onForgotPasswordClick
                            )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onLoginClick,
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRoseColor,
                            contentColor = Color.White,
                            disabledContainerColor =
                                PrimaryRoseColor.copy(alpha = 0.6f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Log in",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = DividerLineColor,
                            thickness = 1.dp
                        )

                        Text(
                            text = "or",
                            fontSize = 13.sp,
                            color = MutedTextColor,
                            modifier = Modifier.padding(
                                horizontal = 14.dp
                            )
                        )

                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = DividerLineColor,
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            fontSize = 14.sp,
                            color = DarkTextColor
                        )

                        Text(
                            text = "Create an account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRoseColor,
                            modifier = Modifier.clickable(
                                onClick = onCreateAccountClick
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    BridelineTheme {
        LoginScreen(
            emailValue = email,
            passwordValue = password,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onLoginClick = {},
            onForgotPasswordClick = {},
            onCreateAccountClick = {}
        )
    }
}