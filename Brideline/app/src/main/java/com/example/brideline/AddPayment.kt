package com.example.brideline

import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val paymentPage = Color(0xFFECEDF3)
private val paymentPink = Color(0xFFEDBEC8)
private val paymentRose = Color(0xFFC95772)
private val paymentInk = Color(0xFF45353A)
private val paymentMuted = Color(0xFF756D72)
private val paymentGreen = Color(0xFF42634E)
private val paymentFieldShape = RoundedCornerShape(10.dp)

private val paymentMethods = listOf(
    "Apple Pay" to "Apple Pay",
    "Bank Transfer" to "Bank Transfer",
    "Cash" to "Cash"
)

@Composable
fun AddPaymentScreen(
    bookingId: String,
    roleLabel: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onBudget: () -> Unit,
    onProfile: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    unpaidPayments: List<Pair<String, String>> = emptyList(),
    onPaymentPaid: (paymentId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var selectedId by rememberSaveable(bookingId) { mutableStateOf<String?>(null) }
    var amount by rememberSaveable(bookingId) { mutableStateOf("") }
    var method by rememberSaveable(bookingId) { mutableStateOf("") }
    var proofName by remember(bookingId) { mutableStateOf<String?>(null) }
    var feedback by rememberSaveable(bookingId) { mutableStateOf<String?>(null) }

    val proofPicker = if (isPreview) {
        null
    } else {
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                proofName = runCatching {
                    context.contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Proof attached"
                feedback = null
            }
        }
    }

    if (!isPreview) {
        BackHandler(onBack = onBack)
    }

    val selectedPayment = unpaidPayments.firstOrNull { (id, _) -> id == selectedId }
    val paidAmount = amount.trim().replace(',', '.').toBigDecimalOrNull()
    val validAmount = paidAmount != null &&
            paidAmount.signum() > 0 &&
            paidAmount.stripTrailingZeros().scale() <= 2

    val canSave = selectedPayment != null &&
            validAmount &&
            method.isNotBlank() &&
            proofName != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = paymentPage,
        bottomBar = {
            BridelineBottomBar(
                selectedTab = BridelineTab.Budget,
                onTabSelected = { tab ->
                    when (tab) {
                        BridelineTab.Home -> onHome()
                        BridelineTab.Budget -> onBudget()
                        BridelineTab.Profile -> onProfile()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = paymentInk
                    )
                }

                Text(
                    text = "Budget & Payments",
                    modifier = Modifier.padding(horizontal = 48.dp),
                    color = paymentInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = roleLabel,
                    color = paymentRose,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Add Payment",
                    color = paymentInk,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        PaymentLabel("Select unpaid payment")

                        LocalPaymentDropdown(
                            label = selectedPayment?.second ?: "Select payment",
                            options = unpaidPayments,
                            onSelect = {
                                selectedId = it
                                feedback = null
                            }
                        )

                        if (unpaidPayments.isEmpty()) {
                            Text(
                                text = "No unpaid payments left.",
                                color = paymentMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        PaymentLabel("Amount Paid")

                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it
                                feedback = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter amount",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            singleLine = true,
                            isError = amount.isNotBlank() && !validAmount,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            shape = paymentFieldShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = paymentPink,
                                unfocusedContainerColor = paymentPink,
                                errorContainerColor = paymentPink,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = paymentInk,
                                unfocusedTextColor = paymentInk,
                                focusedPlaceholderColor = Color.White,
                                unfocusedPlaceholderColor = Color.White
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        PaymentLabel("Select payment method")

                        LocalPaymentDropdown(
                            label = method.ifBlank { "Select method" },
                            options = paymentMethods,
                            onSelect = {
                                method = it
                                feedback = null
                            }
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    PaymentLabel("Payment proof")

                    PaymentProofBox(
                        fileName = proofName,
                        onClick = {
                            if (isPreview) {
                                proofName = "sample-receipt.pdf"
                                feedback = null
                            } else {
                                proofPicker?.launch(
                                    arrayOf("image/jpeg", "image/png", "application/pdf")
                                )
                            }
                        }
                    )

                    feedback?.let { message ->
                        Text(message, color = paymentGreen, fontSize = 13.sp)
                    }
                }
            }

            Button(
                enabled = canSave,
                onClick = {
                    if (canSave && selectedPayment != null) {
                        val (id, title) = selectedPayment
                        selectedId = null
                        amount = ""
                        method = ""
                        proofName = null
                        feedback = "$title marked as paid."
                        onPaymentPaid(id)
                        onSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 8.dp)
                    .heightIn(min = 52.dp),
                shape = paymentFieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = paymentRose,
                    contentColor = Color.White,
                    disabledContainerColor = paymentRose,
                    disabledContentColor = Color.White.copy(alpha = 0.85f)
                )
            ) {
                Text(
                    text = "Save Payment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PaymentLabel(text: String) {
    Text(
        text = text,
        color = paymentInk,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun LocalPaymentDropdown(
    label: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(paymentFieldShape)
                .background(paymentPink)
                .clickable(
                    enabled = options.isNotEmpty(),
                    role = Role.Button,
                    onClick = { expanded = true }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Canvas(Modifier.size(20.dp)) {
                val chevron = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.4f)
                    lineTo(size.width * 0.5f, size.height * 0.65f)
                    lineTo(size.width * 0.8f, size.height * 0.4f)
                }

                drawPath(
                    path = chevron,
                    color = paymentInk,
                    style = Stroke(
                        width = 1.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, title) ->
                DropdownMenuItem(
                    text = { Text(title, color = paymentInk) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentProofBox(
    fileName: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 174.dp)
            .clip(paymentFieldShape)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()

                drawRoundRect(
                    color = paymentPink,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                        )
                    )
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(paymentPink.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PaymentCloudIcon()
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = fileName ?: "Upload receipt or payment proof",
            color = paymentInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = if (fileName == null) "JPG, PNG or PDF" else "Tap to change file",
            color = paymentMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PaymentCloudIcon() {
    Canvas(Modifier.size(36.dp)) {
        val unit = size.minDimension / 24f

        val icon = Path().apply {
            moveTo(5f * unit, 17f * unit)
            cubicTo(2f * unit, 15f * unit, 2f * unit, 11f * unit, 4f * unit, 8f * unit)
            cubicTo(7f * unit, 4f * unit, 14f * unit, 4f * unit, 16f * unit, 9f * unit)
            cubicTo(21f * unit, 9f * unit, 23f * unit, 15f * unit, 19f * unit, 17f * unit)

            moveTo(12f * unit, 21f * unit)
            lineTo(12f * unit, 12f * unit)
            moveTo(8f * unit, 16f * unit)
            lineTo(12f * unit, 12f * unit)
            lineTo(16f * unit, 16f * unit)
        }

        drawPath(
            path = icon,
            color = paymentInk,
            style = Stroke(
                width = 2f * unit,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Preview(
    name = "Add Payment",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 402,
    heightDp = 874
)
@Composable
private fun AddPaymentPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        var finalPaymentPaid by rememberSaveable { mutableStateOf(false) }
        val frameShape = RoundedCornerShape(56.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(10.dp)
                .border(
                    width = 2.dp,
                    color = paymentRose.copy(alpha = 0.7f),
                    shape = frameShape
                )
                .clip(frameShape)
        ) {
            AddPaymentScreen(
                bookingId = "preview-booking",
                roleLabel = "Bride Line.Groom",
                onBack = {},
                onHome = {},
                onBudget = {},
                onProfile = {},
                onSaved = {},
                unpaidPayments = if (finalPaymentPaid) {
                    emptyList()
                } else {
                    listOf("final" to "Final Payment")
                },
                onPaymentPaid = { paymentId ->
                    if (paymentId == "final") {
                        finalPaymentPaid = true
                    }
                }
            )
        }
    }
}