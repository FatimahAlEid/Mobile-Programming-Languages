package com.example.brideline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
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
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --------------------------------------------------
// Colors and font
// --------------------------------------------------

private val SignupPrimary = Color(0xFFD2647D)
private val SignupAccent = Color(0xFFEDBEC8)
private val SignupBackground = Color(0xFFEDEEF3)
private val SignupTextColor = Color(0xFF514247)
private val SignupErrorBackground = Color(0xFFF8E2E7)

private val SignupFont = FontFamily(
    Font(
        resId = R.font.archivo_semibold,
        weight = FontWeight.SemiBold
    )
)

private val SignupServiceCategories = listOf(
    "Hair Stylists",
    "Makeup Experts",
    "Venues & Decorations",
    "Designers & Boutiques",
    "Flowers",
    "DJs & Music"
)

// --------------------------------------------------
// Roles and state
// --------------------------------------------------

enum class SignupRole(val label: String) {
    BRIDE("Bride"),
    GROOM("Groom"),
    SERVICE_PROVIDER("Service provider")
}

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
// Input rules
// --------------------------------------------------

private enum class SignupInputRule {
    ANY,
    LETTERS,
    ALPHANUMERIC,
    INTEGER,
    EMAIL
}

private fun isSignupLetter(character: Char): Boolean {
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
        SignupInputRule.ANY -> true

        SignupInputRule.LETTERS -> value.all {
            isSignupLetter(it) || it == ' '
        }

        SignupInputRule.ALPHANUMERIC -> value.all {
            isSignupLetter(it) || it.isDigit()
        }

        SignupInputRule.INTEGER -> value.all {
            it in '0'..'9'
        }

        SignupInputRule.EMAIL -> value.all {
            it.isLetterOrDigit() || it in "@._%+-"
        }
    }
}

private fun normalizeSignupDigits(value: String): String {
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
// Date helpers
// --------------------------------------------------

private fun signupUtcDate(
    year: Int,
    month: Int,
    day: Int
): Long {
    return Calendar.getInstance(
        TimeZone.getTimeZone("UTC"),
        Locale.ENGLISH
    ).apply {
        clear()
        set(year, month, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
- Gets today's local calendar date, represented as a UTC midnight
- timestamp because Material 3 date pickers use UTC dates.
 */
private fun signupTodayUtcDate(): Long {
    val localToday = Calendar.getInstance(Locale.ENGLISH)

    return signupUtcDate(
        year = localToday.get(Calendar.YEAR),
        month = localToday.get(Calendar.MONTH),
        day = localToday.get(Calendar.DAY_OF_MONTH)
    )
}

private fun signupParseDate(value: String): Long? {
    if (value.isBlank()) return null

    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.ENGLISH
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }

    val position = ParsePosition(0)
    val parsed = formatter.parse(value, position) ?: return null

    return parsed.time.takeIf {
        position.index == value.length
    }
}

private fun signupIsPastDate(
    value: String,
    todayUtc: Long = signupTodayUtcDate()
): Boolean {
    return signupParseDate(value)?.let {
        it < todayUtc
    } == true
}

/**
- Refreshes the current day at the device's next local midnight.
 */
@Composable
private fun rememberSignupTodayUtc(): Long {
    var today by remember {
        mutableStateOf(signupTodayUtcDate())
    }

    LaunchedEffect(Unit) {
        while (true) {
            today = signupTodayUtcDate()

            val now = System.currentTimeMillis()

            val nextMidnight =
                Calendar.getInstance(Locale.ENGLISH).apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

            delay(
                (nextMidnight - now).coerceAtLeast(1_000L)
            )
        }
    }

    return today
}

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

    // Retained for compatibility with existing AuthFlow calls.
    onCreateGroupClick: () -> Unit = {},
    onJoinGroupClick: () -> Unit = {},

    serviceCategories: List<String> = SignupServiceCategories,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val todayUtc = rememberSignupTodayUtc()

    val hasPastWeddingDate =
        state.role == SignupRole.BRIDE &&
                signupIsPastDate(
                    value = state.weddingDate,
                    todayUtc = todayUtc
                )

    val passwordsMatch =
        state.password.isNotEmpty() &&
                state.password == state.confirmPassword

    val confirmationError =
        if (
            state.confirmPassword.isNotEmpty() &&
            !passwordsMatch
        ) {
            "Passwords do not match."
        } else {
            null
        }

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
                                        modifier =
                                            Modifier.padding(16.dp)
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
                                enabled = !isLoading,
                                confirmationError =
                                    confirmationError
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
                                                    enabled =
                                                        !isLoading
                                                )
                                            }

                                            SignupRole.GROOM -> {
                                                GroomDetailsFields(
                                                    state = state,
                                                    onStateChange =
                                                        onStateChange,
                                                    enabled =
                                                        !isLoading
                                                )
                                            }

                                            SignupRole.SERVICE_PROVIDER -> {
                                                ProviderDetailsFields(
                                                    state = state,
                                                    onStateChange =
                                                        onStateChange,
                                                    serviceCategories =
                                                        serviceCategories,
                                                    enabled =
                                                        !isLoading
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

                                    // Recheck the actual day at submission.
                                    val dateAllowed =
                                        state.role != SignupRole.BRIDE ||
                                                !signupIsPastDate(
                                                    state.weddingDate
                                                )

                                    if (
                                        passwordsMatch &&
                                        dateAllowed
                                    ) {
                                        onCreateAccount(state)
                                    }
                                },
                                enabled =
                                    state.showRoleDetails &&
                                            !isLoading &&
                                            passwordsMatch &&
                                            !hasPastWeddingDate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            SignupPrimary,
                                        contentColor =
                                            Color.White,
                                        disabledContainerColor =
                                            SignupPrimary.copy(
                                                alpha = 0.6f
                                            ),
                                        disabledContentColor =
                                            Color.White
                                    )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier.size(24.dp),
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
                                    end = 2.dp,
                                    top = 24.dp,
                                    bottom = 24.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// Scrollbar
// --------------------------------------------------

@Composable
private fun SignupVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val maximumScroll = scrollState.maxValue
    val viewport = scrollState.viewportSize.toFloat()

    if (
        maximumScroll <= 0 ||
        maximumScroll == Int.MAX_VALUE ||
        viewport <= 0f
    ) {
        return
    }

    val rawFraction =
        (scrollState.value.toFloat() / maximumScroll)
            .coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(
            durationMillis = 80,
            easing = FastOutSlowInEasing
        ),
        label = "Scrollbar thumb fraction"
    )

    var trackHeightPx by remember {
        mutableFloatStateOf(0f)
    }

    val density = LocalDensity.current
    val minimumThumbPx = remember(density) {
        with(density) { 44.dp.toPx() }
    }
    val visualWidthPx = remember(density) {
        with(density) { 8.dp.toPx() }
    }

    fun scrollToTrackY(trackY: Float) {
        if (trackHeightPx <= 0f) return

        val contentHeight = viewport + maximumScroll.toFloat()
        val thumbHeight =
            (trackHeightPx * viewport / contentHeight)
                .coerceIn(minimumThumbPx, trackHeightPx)

        val travelRange = (trackHeightPx - thumbHeight).coerceAtLeast(1f)
        val targetFraction =
            ((trackY - thumbHeight / 2f) / travelRange)
                .coerceIn(0f, 1f)

        val targetScroll = (targetFraction * maximumScroll).roundToInt()

        coroutineScope.launch {
            scrollState.scrollTo(targetScroll)
        }
    }

    Canvas(
        modifier = modifier
            .width(24.dp)
            .fillMaxHeight()
            .onSizeChanged { size ->
                trackHeightPx = size.height.toFloat()
            }
            .pointerInput(maximumScroll, viewport, trackHeightPx) {
                detectTapGestures { offset ->
                    scrollToTrackY(offset.y)
                }
            }
            .pointerInput(maximumScroll, viewport, trackHeightPx) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    scrollToTrackY(change.position.y)
                }
            }
    ) {
        if (size.height <= 0f) return@Canvas

        val contentHeight =
            viewport + maximumScroll.toFloat()

        val minimumThumb =
            minimumThumbPx.coerceAtMost(size.height)

        val thumbHeight =
            (size.height * viewport / contentHeight)
                .coerceIn(minimumThumb, size.height)

        val barWidth = visualWidthPx.coerceAtMost(size.width)
        val barLeft = (size.width - barWidth) / 2f

        val thumbTop =
            (size.height - thumbHeight) * animatedFraction

        drawRoundRect(
            color = SignupPrimary.copy(alpha = 0.14f),
            topLeft = Offset(barLeft, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = CornerRadius(
                barWidth / 2f,
                barWidth / 2f
            )
        )

        drawRoundRect(
            color = SignupPrimary.copy(alpha = 0.85f),
            topLeft = Offset(barLeft, thumbTop),
            size = Size(barWidth, thumbHeight),
            cornerRadius = CornerRadius(
                barWidth / 2f,
                barWidth / 2f
            )
        )
    }
}

// --------------------------------------------------
// Header
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
// Common account fields
// --------------------------------------------------

@Composable
private fun AccountDetailsFields(
    state: CreateAccountUiState,
    onStateChange: (CreateAccountUiState) -> Unit,
    onRoleSelected: (SignupRole) -> Unit,
    enabled: Boolean,
    confirmationError: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SignupField(
            label = "Full Name",
            placeholder = "Enter your full name",
            value = state.fullName,
            onValueChange = {
                onStateChange(state.copy(fullName = it))
            },
            inputRule = SignupInputRule.LETTERS,
            enabled = enabled
        )

        SignupField(
            label = "Email Address",
            placeholder = "Enter your email address",
            value = state.email,
            onValueChange = {
                onStateChange(state.copy(email = it))
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
                onStateChange(state.copy(password = it))
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
            enabled = enabled,
            error = confirmationError
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
                onRoleSelected(
                    SignupRole.values().first {
                        it.label == selectedLabel
                    }
                )
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
            onStateChange(state.copy(weddingDate = it))
        },
        enabled = enabled
    )

    SignupField(
        label = "Partner’s Name",
        placeholder = "Enter your partner’s name",
        value = state.partnerName,
        onValueChange = {
            onStateChange(state.copy(partnerName = it))
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
            onStateChange(state.copy(weddingCode = it))
        },
        inputRule = SignupInputRule.ALPHANUMERIC,
        enabled = enabled
    )

    SignupField(
        label = "Budget",
        placeholder = "Enter your budget in SAR",
        value = state.budget,
        onValueChange = {
            onStateChange(state.copy(budget = it))
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
    enabled: Boolean = true,
    error: String? = null
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
        SignupText(label)

        TextField(
            value = value,

            onValueChange = { proposedValue ->
                val candidate =
                    if (
                        inputRule == SignupInputRule.INTEGER ||
                        isPassword
                    ) {
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
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),

            supportingText = if (error != null) {
                {
                    SignupText(
                        text = error,
                        color = SignupPrimary,
                        fontSize = 12.sp
                    )
                }
            } else {
                null
            },

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
                errorContainerColor = SignupAccent,
                errorTextColor = SignupTextColor,
                errorIndicatorColor = SignupPrimary,
                errorCursorColor = SignupPrimary,
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
    val todayUtc = rememberSignupTodayUtc()

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
        SignupText(label)

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
                        if (showPicker) "" else placeholder
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

    if (signupIsPastDate(value, todayUtc)) {
        SignupText(
            text = "Choose today or a future date.",
            color = SignupPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    if (showPicker) {
        val initialDate = remember(value, todayUtc) {
            signupParseDate(value)?.takeIf {
                it >= todayUtc
            }
        }

        val selectableDates = remember {
            object : SelectableDates {

                override fun isSelectableDate(
                    utcTimeMillis: Long
                ): Boolean {
                    return utcTimeMillis >=
                            signupTodayUtcDate()
                }

                override fun isSelectableYear(
                    year: Int
                ): Boolean {
                    return year >= Calendar
                        .getInstance(Locale.ENGLISH)
                        .get(Calendar.YEAR)
                }
            }
        }

        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate,
            selectableDates = selectableDates
        )

        DatePickerDialog(
            modifier = Modifier.border(
                width = 1.dp,
                color = Color(0xFFF2E4E9),
                shape = RoundedCornerShape(24.dp)
            ),
            shape = RoundedCornerShape(24.dp),
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            ),

            onDismissRequest = {
                showPicker = false
            },

            confirmButton = {
                TextButton(
                    enabled =
                        pickerState.selectedDateMillis?.let {
                            it >= todayUtc
                        } == true,
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            // Recheck the actual day when confirming.
                            if (it >= signupTodayUtcDate()) {
                                onValueChange(
                                    formatter.format(Date(it))
                                )

                                showPicker = false
                            }
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
            SignupStyledCalendar(
                state = pickerState,
                today = todayUtc
            )
        }
    }
}

// --------------------------------------------------
// Styled calendar
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupStyledCalendar(
    state: DatePickerState,
    today: Long
) {
    val displayed = state.displayedMonthMillis

    val monthCalendar = remember(displayed) {
        Calendar.getInstance(
            TimeZone.getTimeZone("UTC"),
            Locale.ENGLISH
        ).apply {
            timeInMillis = displayed
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val year = monthCalendar.get(Calendar.YEAR)
    val month = monthCalendar.get(Calendar.MONTH)

    val daysInMonth =
        monthCalendar.getActualMaximum(
            Calendar.DAY_OF_MONTH
        )

    val firstDayOffset =
        (monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val rows =
        (firstDayOffset + daysInMonth + 6) / 7

    val monthLabel = remember(displayed) {
        SimpleDateFormat(
            "MMMM yyyy",
            Locale.ENGLISH
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(displayed))
    }

    var showYears by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IconButton(
                enabled =
                    year > state.yearRange.first ||
                            month > Calendar.JANUARY,
                onClick = {
                    val previous =
                        monthCalendar.clone() as Calendar

                    previous.add(Calendar.MONTH, -1)

                    state.displayedMonthMillis =
                        previous.timeInMillis
                },
                modifier = Modifier.semantics {
                    contentDescription = "Previous month"
                }
            ) {
                SignupText(
                    text = "‹",
                    fontSize = 26.sp,
                    color = SignupPrimary
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showYears = true
                    }
                ) {
                    SignupText(
                        text = monthLabel,
                        fontSize = 16.sp
                    )
                }

                DropdownMenu(
                    expanded = showYears,
                    onDismissRequest = {
                        showYears = false
                    },
                    modifier = Modifier.heightIn(
                        max = 280.dp
                    ),
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    state.yearRange.forEach { candidateYear ->
                        DropdownMenuItem(
                            text = {
                                SignupText(
                                    text =
                                        candidateYear.toString(),
                                    color =
                                        if (candidateYear == year) {
                                            SignupPrimary
                                        } else {
                                            SignupTextColor
                                        }
                                )
                            },

                            enabled =
                                state.selectableDates
                                    .isSelectableYear(
                                        candidateYear
                                    ),

                            onClick = {
                                state.displayedMonthMillis =
                                    signupUtcDate(
                                        year = candidateYear,
                                        month = month,
                                        day = 1
                                    )

                                showYears = false
                            }
                        )
                    }
                }
            }

            IconButton(
                enabled =
                    year < state.yearRange.last ||
                            month < Calendar.DECEMBER,
                onClick = {
                    val next =
                        monthCalendar.clone() as Calendar

                    next.add(Calendar.MONTH, 1)

                    state.displayedMonthMillis =
                        next.timeInMillis
                },
                modifier = Modifier.semantics {
                    contentDescription = "Next month"
                }
            ) {
                SignupText(
                    text = "›",
                    fontSize = 26.sp,
                    color = SignupPrimary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "MON",
                "TUE",
                "WED",
                "THU",
                "FRI",
                "SAT",
                "SUN"
            ).forEach { weekday ->
                SignupText(
                    text = weekday,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF8F898D),
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(
            color = Color(0xFFF2E8EC)
        )

        Spacer(Modifier.height(6.dp))

        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(7) { column ->
                    val day =
                        row * 7 + column - firstDayOffset + 1

                    if (day !in 1..daysInMonth) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )
                    } else {
                        val dayMillis = signupUtcDate(
                            year = year,
                            month = month,
                            day = day
                        )

                        val selectable =
                            state.selectableDates
                                .isSelectableYear(year) &&
                                    state.selectableDates
                                        .isSelectableDate(
                                            dayMillis
                                        )

                        SignupCalendarDay(
                            day = day,
                            dayMillis = dayMillis,
                            isSelected =
                                state.selectedDateMillis ==
                                        dayMillis &&
                                        selectable,
                            isToday = today == dayMillis,
                            enabled = selectable,
                            onClick = {
                                // Recheck the clock at the moment of tapping.
                                val stillSelectable =
                                    state.selectableDates
                                        .isSelectableYear(year) &&
                                            state.selectableDates
                                                .isSelectableDate(
                                                    dayMillis
                                                )

                                if (stillSelectable) {
                                    state.selectedDateMillis =
                                        dayMillis
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// Calendar day
// --------------------------------------------------

@Composable
private fun SignupCalendarDay(
    day: Int,
    dayMillis: Long,
    isSelected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // No special weekend colors.
    val circleColor = when {
        isSelected -> SignupPrimary
        !enabled -> Color(0xFFEDEEF3)
        else -> Color(0xFFF8EDF0)
    }

    val textColor = when {
        isSelected -> Color.White
        !enabled -> Color(0xFFB2B1B9)
        else -> Color(0xFF8C727A)
    }

    val description = remember(dayMillis) {
        SimpleDateFormat(
            "EEEE, d MMMM yyyy",
            Locale.ENGLISH
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(dayMillis))
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                selected = isSelected
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = circleColor,
            border = if (isToday && !isSelected) {
                BorderStroke(
                    width = 1.dp,
                    color = SignupPrimary
                )
            } else {
                null
            }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                SignupText(
                    text = day.toString(),
                    fontSize = 14.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --------------------------------------------------
// Dropdown
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
        targetValue = if (expanded) 180f else 0f,
        label = "Dropdown arrow"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SignupText(label)

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = {
                    focusManager.clearFocus()
                    expanded = true
                },
                enabled =
                    enabled && options.isNotEmpty(),
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
                            if (expanded) "" else placeholder
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
                            size.width / 2f,
                            size.height
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
                            end = Offset(size.width, 0f),
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
                    color =
                        SignupPrimary.copy(alpha = 0.18f)
                )
            ) {
                options.forEach { option ->
                    val selected =
                        option == selectedValue

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
                                    SignupPrimary.copy(
                                        alpha = 0.1f
                                    )
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
// Dropdown checkmark
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
            size.width * 0.4f,
            size.height * 0.75f
        )

        drawLine(
            color = SignupPrimary,
            start = Offset(
                size.width * 0.15f,
                size.height * 0.5f
            ),
            end = middle,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = SignupPrimary,
            start = middle,
            end = Offset(
                size.width * 0.85f,
                size.height * 0.25f
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// --------------------------------------------------
// Footer and typography
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
// Previews
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