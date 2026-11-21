package com.example.brideline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.R
import com.example.brideline.ui.theme.BridelineTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// --------------------------------------------------
// Colors
// --------------------------------------------------

private val SignupPrimary = Color(0xFFD2647D)
private val SignupAccent = Color(0xFFEDBEC8)
private val SignupBackground = Color(0xFFEDEEF3)
private val SignupTextColor = Color(0xFF514247)
private val SignupErrorBackground = Color(0xFFF8E2E7)

// --------------------------------------------------
// Font
// --------------------------------------------------

private val SignupFont = FontFamily(
    Font(
        resId = R.font.archivo_semibold,
        weight = FontWeight.SemiBold
    )
)

// --------------------------------------------------
// Service categories
// --------------------------------------------------

private val SignupServiceCategories = listOf(
    "Hair Stylists",
    "Makeup Experts",
    "Venues & Decorations",
    "Designers & Boutiques",
    "Flowers",
    "DJs & Music"
)

// --------------------------------------------------
// Roles
// --------------------------------------------------

enum class SignupRole(val label: String) {
    BRIDE("Bride"),
    GROOM("Groom"),
    SERVICE_PROVIDER("Service provider")
}

// --------------------------------------------------
// Form state
// --------------------------------------------------

data class CreateAccountUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: SignupRole = SignupRole.BRIDE,
    val showRoleDetails: Boolean = false,

    // Bride
    val weddingDate: String = "",
    val partnerName: String = "",
    val weddingLocation: String = "",
    val expectedGuests: String = "",

    // Groom
    val weddingCode: String = "",
    val budget: String = "",

    // Service provider
    val businessName: String = "",
    val businessLocation: String = "",
    val verificationNumber: String = "",
    val serviceCategory: String = "",
    val startingPrice: String = "",
    val contactNumber: String = ""
)

// --------------------------------------------------
// Main screen
// --------------------------------------------------

@Suppress("UNUSED_PARAMETER")
@Composable
fun CreateAccountScreen(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    onCreateAccount: (CreateAccountUiState) -> Unit,
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,

    // Retained for compatibility with previous AuthFlow calls.
    onCreateGroupClick: () -> Unit = {},
    onJoinGroupClick: () -> Unit = {},

    serviceCategories: List<String> = SignupServiceCategories,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Android system back button.
    BackHandler(onBack = onBack)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SignupBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                color = SignupBackground,
                border = BorderStroke(
                    width = 1.5.dp,
                    color = SignupPrimary.copy(alpha = 0.7f)
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewportHeight = maxHeight

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .heightIn(min = viewportHeight)
                            .padding(
                                horizontal = 24.dp,
                                vertical = 16.dp
                            ),
                        verticalArrangement =
                            Arrangement.SpaceBetween,
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            SignupHeader()

                            Spacer(Modifier.height(24.dp))

                            if (!errorMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = SignupErrorBackground
                                ) {
                                    SignupText(
                                        text = errorMessage,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            AccountDetailsFields(
                                state = state,
                                onStateChange = onStateChange,
                                onRoleSelected = { selectedRole ->
                                    focusManager.clearFocus()

                                    onStateChange(
                                        state.copy(
                                            role = selectedRole,
                                            showRoleDetails = true
                                        )
                                    )
                                },
                                enabled = !isLoading
                            )

                            if (state.showRoleDetails) {
                                Spacer(Modifier.height(24.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement =
                                        Arrangement.spacedBy(16.dp)
                                ) {
                                    key(state.role) {
                                        when (state.role) {
                                            SignupRole.BRIDE -> {
                                                BrideDetailsFields(
                                                    state = state,
                                                    onStateChange =
                                                        onStateChange,
                                                    enabled = !isLoading
                                                )
                                            }

                                            SignupRole.GROOM -> {
                                                GroomDetailsFields(
                                                    state = state,
                                                    onStateChange =
                                                        onStateChange,
                                                    enabled = !isLoading
                                                )
                                            }

                                            SignupRole.SERVICE_PROVIDER -> {
                                                ProviderDetailsFields(
                                                    state = state,
                                                    onStateChange =
                                                        onStateChange,
                                                    serviceCategories =
                                                        serviceCategories,
                                                    enabled = !isLoading
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onCreateAccount(state)
                                },
                                enabled =
                                    state.showRoleDetails && !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SignupPrimary,
                                    contentColor = Color.White,
                                    disabledContainerColor =
                                        SignupPrimary.copy(alpha = 0.6f),
                                    disabledContentColor = Color.White
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    SignupText(
                                        text = "Create Account",
                                        color = Color.White
                                    )
                                }
                            }

                            SignupLoginFooter(
                                onLoginClick = onLoginClick
                            )
                        }
                    }

                    if (state.showRoleDetails) {
                        SignupVerticalScrollbar(
                            scrollState = scrollState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    end = 6.dp,
                                    top = 24.dp,
                                    bottom = 24.dp
                                )
                                .width(4.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// Vertical scrollbar
// --------------------------------------------------

@Composable
private fun SignupVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val maximumScroll = scrollState.maxValue
        val viewport = scrollState.viewportSize.toFloat()

        if (
            maximumScroll <= 0 ||
            maximumScroll == Int.MAX_VALUE ||
            viewport <= 0f ||
            size.height <= 0f
        ) {
            return@Canvas
        }

        val contentHeight =
            viewport + maximumScroll.toFloat()

        val minimumThumb =
            36.dp.toPx().coerceAtMost(size.height)

        val thumbHeight =
            (size.height * viewport / contentHeight)
                .coerceIn(
                    minimumValue = minimumThumb,
                    maximumValue = size.height
                )

        val scrollFraction =
            (scrollState.value.toFloat() / maximumScroll)
                .coerceIn(0f, 1f)

        val thumbTop =
            (size.height - thumbHeight) * scrollFraction

        drawRoundRect(
            color = SignupPrimary.copy(alpha = 0.12f),
            cornerRadius = CornerRadius(
                x = size.width / 2f,
                y = size.width / 2f
            )
        )

        drawRoundRect(
            color = SignupPrimary.copy(alpha = 0.8f),
            topLeft = Offset(
                x = 0f,
                y = thumbTop
            ),
            size = Size(
                width = size.width,
                height = thumbHeight
            ),
            cornerRadius = CornerRadius(
                x = size.width / 2f,
                y = size.width / 2f
            )
        )
    }
}

// --------------------------------------------------
// Header without a back button
// --------------------------------------------------

@Composable
private fun SignupHeader() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(
                id = R.drawable.bride_line_logo
            ),
            contentDescription = "Bride Line Logo",
            modifier = Modifier
                .width(240.dp)
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
    }

    Spacer(Modifier.height(8.dp))

    SignupText(
        text = "Create Account",
        fontSize = 22.sp,
        textAlign = TextAlign.Center
    )
}

// --------------------------------------------------
// Input rules
// --------------------------------------------------

private enum class SignupInputRule {
    ANY,
    LETTERS,
    ALPHANUMERIC,
    INTEGER,
    EMAIL
}

private fun isSignupLetter(
    character: Char
): Boolean {
    val type = Character.getType(character)

    return character.isLetter() ||
            type == Character.NON_SPACING_MARK.toInt() ||
            type == Character.COMBINING_SPACING_MARK.toInt()
}

private fun isSignupInputAllowed(
    value: String,
    rule: SignupInputRule
): Boolean {
    return when (rule) {
        SignupInputRule.ANY -> {
            true
        }

        SignupInputRule.LETTERS -> {
            value.all {
                isSignupLetter(it) || it == ' '
            }
        }

        SignupInputRule.ALPHANUMERIC -> {
            value.all {
                isSignupLetter(it) || it.isDigit()
            }
        }

        SignupInputRule.INTEGER -> {
            value.all {
                it in '0'..'9'
            }
        }

        SignupInputRule.EMAIL -> {
            value.all {
                it.isLetterOrDigit() || it in "@._%+-"
            }
        }
    }
}

private fun normalizeSignupDigits(
    value: String
): String {
    return buildString {
        value.forEach { character ->
            if (character.isDigit()) {
                append(
                    ('0'.code + character.digitToInt()).toChar()
                )
            } else {
                append(character)
            }
        }
    }
}

// --------------------------------------------------
// Common account fields
// --------------------------------------------------

@Composable
private fun AccountDetailsFields(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    onRoleSelected: (SignupRole) -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SignupField(
            label = "Full Name",
            placeholder = "Enter your full name",
            value = state.fullName,
            onValueChange = {
                onStateChange(
                    state.copy(fullName = it)
                )
            },
            inputRule = SignupInputRule.LETTERS,
            enabled = enabled
        )

        SignupField(
            label = "Email Address",
            placeholder = "Enter your email address",
            value = state.email,
            onValueChange = {
                onStateChange(
                    state.copy(email = it)
                )
            },
            keyboardType = KeyboardType.Email,
            inputRule = SignupInputRule.EMAIL,
            enabled = enabled
        )

        SignupField(
            label = "Password",
            placeholder = "Create password",
            value = state.password,
            onValueChange = {
                onStateChange(
                    state.copy(password = it)
                )
            },
            isPassword = true,
            keyboardType = KeyboardType.NumberPassword,
            inputRule = SignupInputRule.INTEGER,
            enabled = enabled
        )

        SignupField(
            label = "Confirm Password",
            placeholder = "Re-enter your password",
            value = state.confirmPassword,
            onValueChange = {
                onStateChange(
                    state.copy(confirmPassword = it)
                )
            },
            isPassword = true,
            keyboardType = KeyboardType.Password,
            inputRule = SignupInputRule.ALPHANUMERIC,
            imeAction = ImeAction.Done,
            enabled = enabled
        )

        SignupDropdown(
            label = "I Am Signing Up As",
            selectedValue = if (state.showRoleDetails) {
                state.role.label
            } else {
                ""
            },
            placeholder = "Select your role",
            options = SignupRole.values().map {
                it.label
            },
            enabled = enabled,
            onSelected = { selectedLabel ->
                val selectedRole = SignupRole.values().first {
                    it.label == selectedLabel
                }

                onRoleSelected(selectedRole)
            }
        )
    }
}

// --------------------------------------------------
// Bride fields
// --------------------------------------------------

@Composable
private fun BrideDetailsFields(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    enabled: Boolean
) {
    SignupDateField(
        label = "Wedding Date",
        placeholder = "Enter your wedding date",
        value = state.weddingDate,
        onValueChange = {
            onStateChange(
                state.copy(weddingDate = it)
            )
        },
        enabled = enabled
    )

    SignupField(
        label = "Partner’s Name",
        placeholder = "Enter your partner’s name",
        value = state.partnerName,
        onValueChange = {
            onStateChange(
                state.copy(partnerName = it)
            )
        },
        inputRule = SignupInputRule.LETTERS,
        enabled = enabled
    )

    SignupField(
        label = "Wedding Location",
        placeholder = "Enter the wedding location",
        value = state.weddingLocation,
        onValueChange = {
            onStateChange(
                state.copy(weddingLocation = it)
            )
        },
        inputRule = SignupInputRule.LETTERS,
        enabled = enabled
    )

    SignupField(
        label = "Expected Number of Guests",
        placeholder = "Enter estimated guests",
        value = state.expectedGuests,
        onValueChange = {
            onStateChange(
                state.copy(expectedGuests = it)
            )
        },
        keyboardType = KeyboardType.Number,
        inputRule = SignupInputRule.INTEGER,
        imeAction = ImeAction.Done,
        enabled = enabled
    )
}

// --------------------------------------------------
// Groom fields
// --------------------------------------------------

@Composable
private fun GroomDetailsFields(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    enabled: Boolean
) {
    SignupField(
        label = "Wedding Code",
        placeholder = "Enter your wedding code",
        value = state.weddingCode,
        onValueChange = {
            onStateChange(
                state.copy(weddingCode = it)
            )
        },
        inputRule = SignupInputRule.ALPHANUMERIC,
        enabled = enabled
    )

    SignupField(
        label = "Budget",
        placeholder = "Enter your budget in SAR",
        value = state.budget,
        onValueChange = {
            onStateChange(
                state.copy(budget = it)
            )
        },
        keyboardType = KeyboardType.Number,
        inputRule = SignupInputRule.INTEGER,
        imeAction = ImeAction.Done,
        enabled = enabled
    )
}

// --------------------------------------------------
// Service provider fields
// --------------------------------------------------

@Composable
private fun ProviderDetailsFields(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    serviceCategories: List<String>,
    enabled: Boolean
) {
    SignupField(
        label = "Business Name",
        placeholder = "Enter your business name",
        value = state.businessName,
        onValueChange = {
            onStateChange(
                state.copy(businessName = it)
            )
        },
        inputRule = SignupInputRule.LETTERS,
        enabled = enabled
    )

    SignupField(
        label = "Business Location",
        placeholder = "Enter your business location",
        value = state.businessLocation,
        onValueChange = {
            onStateChange(
                state.copy(businessLocation = it)
            )
        },
        inputRule = SignupInputRule.LETTERS,
        enabled = enabled
    )

    SignupField(
        label = "Enter Your Authentication Number",
        placeholder = "XXXXXXXXXX",
        value = state.verificationNumber,
        onValueChange = {
            onStateChange(
                state.copy(verificationNumber = it)
            )
        },
        keyboardType = KeyboardType.Number,
        inputRule = SignupInputRule.INTEGER,
        enabled = enabled
    )

    SignupDropdown(
        label = "Service Category",
        selectedValue = state.serviceCategory,
        placeholder = "Select service category",
        options = serviceCategories,
        onSelected = {
            onStateChange(
                state.copy(serviceCategory = it)
            )
        },
        enabled = enabled
    )

    SignupField(
        label = "Starting Price",
        placeholder = "Enter your starting price",
        value = state.startingPrice,
        onValueChange = {
            onStateChange(
                state.copy(startingPrice = it)
            )
        },
        keyboardType = KeyboardType.Number,
        inputRule = SignupInputRule.INTEGER,
        enabled = enabled
    )

    SignupField(
        label = "Contact Number",
        placeholder = "Enter your phone contact number",
        value = state.contactNumber,
        onValueChange = {
            onStateChange(
                state.copy(contactNumber = it)
            )
        },
        keyboardType = KeyboardType.Number,
        inputRule = SignupInputRule.INTEGER,
        imeAction = ImeAction.Done,
        enabled = enabled
    )
}

// --------------------------------------------------
// Shared text field
// --------------------------------------------------

@Composable
private fun SignupField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    inputRule: SignupInputRule = SignupInputRule.ANY,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true
) {
    var passwordVisible by rememberSaveable(label) {
        mutableStateOf(false)
    }

    var focused by remember {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SignupText(
            text = label
        )

        TextField(
            value = value,

            onValueChange = { proposedValue ->
                val candidate =
                    if (inputRule == SignupInputRule.INTEGER) {
                        normalizeSignupDigits(proposedValue)
                    } else {
                        proposedValue
                    }

                if (
                    isSignupInputAllowed(
                        value = candidate,
                        rule = inputRule
                    )
                ) {
                    onValueChange(candidate)
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    focused = it.isFocused
                },

            enabled = enabled,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),

            textStyle = TextStyle(
                fontFamily = SignupFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = SignupTextColor
            ),

            placeholder = {
                if (!focused) {
                    SignupText(
                        text = placeholder,
                        color = Color.White
                    )
                }
            },

            visualTransformation =
                if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },

            trailingIcon = if (isPassword) {
                {
                    TextButton(
                        enabled = enabled,
                        onClick = {
                            passwordVisible = !passwordVisible
                        },
                        contentPadding = PaddingValues(
                            horizontal = 12.dp
                        )
                    ) {
                        SignupText(
                            text = if (passwordVisible) {
                                "Hide"
                            } else {
                                "Show"
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                null
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),

            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Down
                    )
                },
                onDone = {
                    focusManager.clearFocus()
                }
            ),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = SignupAccent,
                unfocusedContainerColor = SignupAccent,
                disabledContainerColor = SignupAccent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = SignupTextColor,
                unfocusedTextColor = SignupTextColor,
                disabledTextColor = SignupTextColor,
                cursorColor = SignupPrimary
            )
        )
    }
}

// --------------------------------------------------
// Date field
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupDateField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    var showPicker by rememberSaveable {
        mutableStateOf(false)
    }

    val focusManager = LocalFocusManager.current

    val formatter = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.ENGLISH
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SignupText(
            text = label
        )

        Surface(
            onClick = {
                focusManager.clearFocus()
                showPicker = true
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        if (value.isBlank()) {
                            label
                        } else {
                            "$label: $value"
                        }
                },
            shape = RoundedCornerShape(8.dp),
            color = SignupAccent
        ) {
            Box(
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                SignupText(
                    text = if (value.isNotBlank()) {
                        value
                    } else {
                        if (showPicker) {
                            ""
                        } else {
                            placeholder
                        }
                    },
                    color = if (value.isBlank()) {
                        Color.White
                    } else {
                        SignupTextColor
                    }
                )
            }
        }
    }

    if (showPicker) {
        val initialDate = remember(value) {
            runCatching {
                formatter.parse(value)?.time
            }.getOrNull()
        }

        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate
        )

        DatePickerDialog(
            onDismissRequest = {
                showPicker = false
            },

            confirmButton = {
                TextButton(
                    enabled =
                        pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onValueChange(
                                formatter.format(Date(it))
                            )

                            showPicker = false
                        }
                    }
                ) {
                    SignupText(
                        text = "Confirm",
                        color = SignupPrimary
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showPicker = false
                    }
                ) {
                    SignupText(
                        text = "Cancel",
                        color = SignupTextColor
                    )
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = SignupBackground,
                    selectedDayContainerColor = SignupPrimary,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = SignupPrimary
                )
            )
        }
    }
}

// --------------------------------------------------
// Shared dropdown
// --------------------------------------------------

@Composable
private fun SignupDropdown(
    label: String,
    selectedValue: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var anchorWidth by remember {
        mutableStateOf(0)
    }

    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) {
            180f
        } else {
            0f
        },
        label = "Dropdown arrow"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SignupText(
            text = label
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = {
                    focusManager.clearFocus()
                    expanded = true
                },
                enabled = enabled && options.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        anchorWidth = it.width
                    }
                    .semantics {
                        contentDescription = label
                    },
                shape = RoundedCornerShape(8.dp),
                color = SignupAccent,
                border = if (expanded) {
                    BorderStroke(
                        width = 1.dp,
                        color = SignupPrimary
                    )
                } else {
                    null
                }
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    SignupText(
                        text = selectedValue.ifBlank {
                            if (expanded) {
                                ""
                            } else {
                                placeholder
                            }
                        },
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(12.dp))

                    Canvas(
                        modifier = Modifier
                            .size(
                                width = 18.dp,
                                height = 10.dp
                            )
                            .rotate(arrowRotation)
                    ) {
                        val middle = Offset(
                            x = size.width / 2f,
                            y = size.height
                        )

                        drawLine(
                            color = SignupTextColor,
                            start = Offset.Zero,
                            end = middle,
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = SignupTextColor,
                            start = middle,
                            end = Offset(
                                x = size.width,
                                y = 0f
                            ),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier
                    .width(
                        with(density) {
                            anchorWidth.toDp()
                        }
                    )
                    .heightIn(max = 320.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = SignupPrimary.copy(alpha = 0.18f)
                )
            ) {
                options.forEach { option ->
                    val selected = option == selectedValue

                    DropdownMenuItem(
                        modifier = Modifier
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            )
                            .clip(
                                RoundedCornerShape(10.dp)
                            )
                            .background(
                                color = if (selected) {
                                    SignupPrimary.copy(alpha = 0.1f)
                                } else {
                                    Color.Transparent
                                }
                            ),

                        text = {
                            SignupText(
                                text = option,
                                color = if (selected) {
                                    SignupPrimary
                                } else {
                                    SignupTextColor
                                }
                            )
                        },

                        trailingIcon = if (selected) {
                            {
                                SignupCheckmark()
                            }
                        } else {
                            null
                        },

                        onClick = {
                            expanded = false
                            onSelected(option)
                        },

                        contentPadding = PaddingValues(
                            horizontal = 12.dp
                        )
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// Dropdown selected checkmark
// --------------------------------------------------

@Composable
private fun SignupCheckmark() {
    Canvas(
        modifier = Modifier
            .size(20.dp)
            .semantics {
                contentDescription = "Selected"
            }
    ) {
        val middle = Offset(
            x = size.width * 0.4f,
            y = size.height * 0.75f
        )

        drawLine(
            color = SignupPrimary,
            start = Offset(
                x = size.width * 0.15f,
                y = size.height * 0.5f
            ),
            end = middle,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = SignupPrimary,
            start = middle,
            end = Offset(
                x = size.width * 0.85f,
                y = size.height * 0.25f
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// --------------------------------------------------
// Login footer
// --------------------------------------------------

@Composable
private fun SignupLoginFooter(
    onLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SignupText(
            text = "Already have an account?",
            fontSize = 12.sp
        )

        TextButton(
            onClick = onLoginClick,
            contentPadding = PaddingValues(
                horizontal = 4.dp,
                vertical = 0.dp
            )
        ) {
            SignupText(
                text = "Log in",
                color = SignupPrimary,
                fontSize = 12.sp
            )
        }
    }
}

// --------------------------------------------------
// Shared typography
// --------------------------------------------------

@Composable
private fun SignupText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = SignupTextColor,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = SignupFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        color = color,
        textAlign = textAlign
    )
}

// --------------------------------------------------
// Preview state holder
// --------------------------------------------------

@Composable
private fun SignupPreviewContent(
    initialState: CreateAccountUiState
) {
    var state by remember {
        mutableStateOf(initialState)
    }

    BridelineTheme {
        CreateAccountScreen(
            state = state,
            onStateChange = {
                state = it
            },
            onCreateAccount = {},
            onBack = {},
            onLoginClick = {},
            serviceCategories = SignupServiceCategories
        )
    }
}

// --------------------------------------------------
// Previews
// --------------------------------------------------

@Preview(
    name = "Create Account",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun CreateAccountScreenPreview() {
    SignupPreviewContent(
        initialState = CreateAccountUiState()
    )
}

@Preview(
    name = "Bride Form",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun BrideSignupPreview() {
    SignupPreviewContent(
        initialState = CreateAccountUiState(
            role = SignupRole.BRIDE,
            showRoleDetails = true
        )
    )
}

@Preview(
    name = "Groom Form",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun GroomSignupPreview() {
    SignupPreviewContent(
        initialState = CreateAccountUiState(
            role = SignupRole.GROOM,
            showRoleDetails = true
        )
    )
}

@Preview(
    name = "Service Provider Form",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProviderSignupPreview() {
    SignupPreviewContent(
        initialState = CreateAccountUiState(
            role = SignupRole.SERVICE_PROVIDER,
            showRoleDetails = true,
            serviceCategory = "Hair Stylists"
        )
    )
}