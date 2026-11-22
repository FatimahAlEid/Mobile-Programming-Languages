package com.example.brideline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pride.ui.components.BridelineBottomNavBar
import com.example.pride.ui.components.NavTab

// Self-contained Style Guide Colors
private val PrimaryPink = Color(0xFFD2647D)
private val AccentPink = Color(0xFFEDBEC8)
private val BackgroundSoft = Color(0xFFFAF6F7)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1F1F1F)
private val TextMuted = Color(0xFF757575)

@Composable
fun ForgotPasswordTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryPink,
            secondary = AccentPink,
            background = BackgroundSoft,
            surface = CardWhite,
            onPrimary = Color.White,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        content = content
    )
}

@Composable
fun ForgotPasswordScreen(
    onPasswordResetSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToServices: () -> Unit = {}
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    ForgotPasswordTheme {
        Scaffold(
            containerColor = BackgroundSoft,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Change Password",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(48.dp))
                }
            },
            bottomBar = {
                BridelineBottomNavBar(
                    selectedTab = NavTab.PROFILE,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToServices = onNavigateToServices,
                    onNavigateToProfile = {}
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock Header (Matching Edit Profile Avatar Header)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AccentPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Lock Icon",
                        tint = TextPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                // Form Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // New Password Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )

                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "Enter new password", color = TextMuted)
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Text(
                                    text = if (newPasswordVisible) "Hide" else "Show",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPink,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable { newPasswordVisible = !newPasswordVisible }
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AccentPink,
                                unfocusedContainerColor = AccentPink,
                                disabledContainerColor = AccentPink,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    // Confirm New Password Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Confirm New Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )

                        TextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "Re-enter new password", color = TextMuted)
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Text(
                                    text = if (confirmPasswordVisible) "Hide" else "Show",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPink,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AccentPink,
                                unfocusedContainerColor = AccentPink,
                                disabledContainerColor = AccentPink,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons (Save Changes & Cancel matching Edit Profile)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPasswordResetSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
                    ) {
                        Text(
                            text = "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, PrimaryPink),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardWhite,
                            contentColor = PrimaryPink
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen()
}
