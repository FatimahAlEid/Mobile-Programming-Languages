package com.example.brideline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DetailBackground = Color(0xFFECEDF3)
private val DetailRose = Color(0xFFD2647D)
private val DetailBlush = Color(0xFFEDBEC8)
private val DetailInk = Color(0xFF45353A)
private val DetailMuted = Color(0xFF756D72)
private val DetailSelected = Color(0xFFEDD9DF)

private val DetailFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private data class BookingDetails(
    val provider: String,
    val category: String,
    val region: String,
    val status: String,
    val date: String,
    val time: String,
    val location: String,
    val service: String,
    val price: String,
    val paymentStatus: String,
    val reviewed: Boolean = false
)

@Composable
fun AppointmentDetailsScreen(
    weddingId: String,
    appointmentId: String,
    onBack: () -> Unit,
    onMessageProvider: (String) -> Unit,
    onLeaveReview: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onHome: (() -> Unit)? = null,
    onServices: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null
) {
    val isPreviewMode = weddingId == "preview-wedding"

    if (isPreviewMode) {
        var previewBooking by remember(appointmentId) {
            mutableStateOf(
                when (appointmentId) {
                    "2" -> BookingDetails(
                        provider = "Bloom Events",
                        category = "Flowers",
                        region = "Eastern Province",
                        status = "Pending",
                        date = "23 November 2026",
                        time = "03:44 AM",
                        location = "Khobar",
                        service = "Floral Arrangement",
                        price = "SAR 450",
                        paymentStatus = "Deposit Due"
                    )
                    "3" -> BookingDetails(
                        provider = "Pearl Hall",
                        category = "Venue",
                        region = "Eastern Province",
                        status = "Completed",
                        date = "20 November 2026",
                        time = "05:00 PM",
                        location = "Dammam",
                        service = "Hall Booking",
                        price = "SAR 1500",
                        paymentStatus = "Paid",
                        reviewed = false
                    )
                    else -> BookingDetails(
                        provider = "Rose Studio",
                        category = "Photography",
                        region = "Eastern Province",
                        status = "Confirmed",
                        date = "22 November 2026",
                        time = "03:44 AM",
                        location = "Dammam",
                        service = "Wedding Photography",
                        price = "SAR 800",
                        paymentStatus = "Paid"
                    )
                }
            )
        }

        DetailsContent(
            booking = previewBooking,
            onBack = onBack,
            onMessage = { onMessageProvider(appointmentId) },
            onReview = { onLeaveReview(appointmentId, previewBooking.provider) },
            onCancel = { previewBooking = previewBooking.copy(status = "Cancelled") },
            cancelling = false,
            cancelError = null,
            onHome = onHome,
            onServices = onServices,
            onProfile = onProfile,
            modifier = modifier
        )
        return
    }

    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var details by remember(weddingId, appointmentId) {
        mutableStateOf<BookingDetails?>(null)
    }
    var loading by remember(weddingId, appointmentId) { mutableStateOf(true) }
    var loadError by remember(weddingId, appointmentId) {
        mutableStateOf<String?>(null)
    }
    var retry by remember { mutableIntStateOf(0) }
    var cancelling by remember(appointmentId) { mutableStateOf(false) }
    var cancelError by remember(appointmentId) { mutableStateOf<String?>(null) }

    DisposableEffect(weddingId, appointmentId, retry) {
        loading = true
        loadError = null

        val listener = if (weddingId.isBlank() || appointmentId.isBlank()) {
            loading = false
            loadError = "Appointment not found."
            null
        } else {
            db.collection("bookings")
                .document(appointmentId)
                .addSnapshotListener { snapshot, failure ->
                    loading = false

                    if (failure != null) {
                        loadError = failure.localizedMessage ?: "Could not load appointment."
                    } else if (
                        snapshot == null ||
                        !snapshot.exists() ||
                        snapshot.get("weddingId") != weddingId
                    ) {
                        details = null
                        loadError = "Appointment not found."
                    } else {
                        runCatching { snapshot.toBookingDetails() }
                            .onSuccess {
                                details = it
                                loadError = null
                            }
                            .onFailure {
                                details = null
                                loadError = it.localizedMessage
                                    ?: "Could not read appointment details."
                            }
                    }
                }
        }

        onDispose { listener?.remove() }
    }

    val booking = details

    if (loading || loadError != null || booking == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DetailBackground)
                .safeDrawingPadding()
        ) {
            DetailsHeader(onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading) {
                    CircularProgressIndicator(color = DetailRose)
                } else {
                    Text(
                        text = loadError ?: "Appointment not found.",
                        color = DetailInk,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { retry++ }) {
                        Text("Retry", color = DetailRose)
                    }
                }
            }

            DetailsBottomBar(onHome, onServices, onProfile)
        }
        return
    }

    DetailsContent(
        booking = booking,
        onBack = onBack,
        onMessage = { onMessageProvider(appointmentId) },
        onReview = { onLeaveReview(appointmentId, booking.provider) },
        onCancel = {
            if (!cancelling) {
                cancelling = true
                cancelError = null

                scope.launch {
                    try {
                        val ref = db.collection("bookings").document(appointmentId)

                        db.runTransaction { transaction ->
                            val current = transaction.get(ref)

                            check(current.exists() && current.get("weddingId") == weddingId) {
                                "Appointment not found."
                            }

                            val status = current.getString("status")
                                .orEmpty()
                                .trim()
                                .lowercase(Locale.ROOT)

                            check(status !in setOf("completed", "cancelled", "canceled")) {
                                "This booking can no longer be cancelled."
                            }

                            transaction.update(ref, "status", "Cancelled")
                            Unit
                        }.await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        cancelError = e.localizedMessage ?: "Could not cancel booking."
                    } finally {
                        cancelling = false
                    }
                }
            }
        },
        cancelling = cancelling,
        cancelError = cancelError,
        onHome = onHome,
        onServices = onServices,
        onProfile = onProfile,
        modifier = modifier
    )
}

private fun DocumentSnapshot.toBookingDetails(): BookingDetails {
    fun textField(name: String): String =
        (get(name) as? String).orEmpty().trim()

    val scheduledDate = requireNotNull(getTimestamp("scheduledDate")) {
        "Appointment date is missing."
    }.toDate()

    val priceText = get("price")?.toString()?.trim().orEmpty()
    val displayPrice = when {
        priceText.isBlank() -> "Not specified"
        priceText.toBigDecimalOrNull() != null -> {
            val amount = priceText.toBigDecimal().stripTrailingZeros().toPlainString()
            "SAR $amount"
        }
        else -> priceText
    }

    return BookingDetails(
        provider = textField("providerName").ifBlank { "Service Provider" },
        category = textField("category"),
        region = textField("region"),
        status = textField("status").ifBlank { "Unknown" },
        date = formatBookingDate(scheduledDate, "d MMMM yyyy"),
        time = formatBookingDate(scheduledDate, "h:mm a"),
        location = textField("location").ifBlank { "Not specified" },
        service = textField("serviceName")
            .ifBlank { textField("service") }
            .ifBlank { "Not specified" },
        price = displayPrice,
        paymentStatus = textField("paymentStatus").ifBlank { "Not specified" },
        reviewed = get("reviewed") == true
    )
}

private fun formatBookingDate(date: Date, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.ENGLISH).format(date)
}

@Composable
private fun DetailsContent(
    booking: BookingDetails,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onReview: () -> Unit,
    onCancel: () -> Unit,
    cancelling: Boolean,
    cancelError: String?,
    onHome: (() -> Unit)?,
    onServices: (() -> Unit)?,
    onProfile: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    val status = booking.status.trim().lowercase(Locale.ROOT)
    val completed = status == "completed"
    val cancelled = status == "cancelled" || status == "canceled"

    if (showCancelDialog && !completed && !cancelled) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking", fontFamily = DetailFont) },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancel()
                    }
                ) {
                    Text("Yes, Cancel", color = DetailRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Booking", color = DetailInk)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBackground)
            .safeDrawingPadding()
    ) {
        DetailsHeader(onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            ProviderCard(booking)

            Spacer(Modifier.height(18.dp))
            SectionTitle("Appointment Information")
            Spacer(Modifier.height(6.dp))

            InfoCard {
                DetailRow(Icons.Outlined.CalendarToday, "Date", booking.date)
                DetailRow(Icons.Outlined.Schedule, "Time", booking.time)
                DetailRow(Icons.Outlined.LocationOn, "Location", booking.location)
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("Booking Summary")
            Spacer(Modifier.height(6.dp))

            InfoCard {
                DetailRow(Icons.Outlined.ShoppingBag, "Service", booking.service)
                DetailRow(Icons.Outlined.CreditCard, "Price", booking.price)
                DetailRow(
                    Icons.Outlined.AccountBalanceWallet,
                    "Payment Status",
                    booking.paymentStatus
                )
            }

            if (cancelError != null) {
                Spacer(Modifier.height(10.dp))
                Text(cancelError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))
        }

        if (!cancelled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (completed) {
                    ActionButton(
                        text = if (booking.reviewed) "Review submitted" else "Leave a Review",
                        filled = true,
                        enabled = !booking.reviewed,
                        modifier = Modifier.weight(1f),
                        onClick = onReview
                    )
                    ActionButton(
                        text = "Message Provider",
                        filled = false,
                        modifier = Modifier.weight(1f),
                        onClick = onMessage
                    )
                } else {
                    ActionButton(
                        text = "Message Provider",
                        filled = true,
                        modifier = Modifier.weight(1f),
                        onClick = onMessage
                    )
                    ActionButton(
                        text = if (cancelling) "Cancelling…" else "Cancel Booking",
                        filled = false,
                        enabled = !cancelling,
                        modifier = Modifier.weight(1f),
                        onClick = { showCancelDialog = true }
                    )
                }
            }
        }

        DetailsBottomBar(onHome, onServices, onProfile)
    }
}

@Composable
private fun DetailsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = DetailInk,
                modifier = Modifier.size(23.dp)
            )
        }

        Text(
            text = "Appointment Details",
            modifier = Modifier.align(Alignment.Center),
            fontFamily = DetailFont,
            fontWeight = FontWeight.SemiBold,
            color = DetailInk,
            fontSize = 23.sp
        )
    }
}

@Composable
private fun ProviderCard(booking: BookingDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DetailBlush, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = booking.provider,
                modifier = Modifier.weight(1f),
                fontFamily = DetailFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = DetailInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (booking.category.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = booking.category,
                    fontSize = 14.sp,
                    color = DetailInk
                )
            }
        }

        if (booking.region.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(booking.region, fontSize = 13.sp, color = DetailInk)
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status:", fontSize = 14.sp, color = DetailInk)
            Spacer(Modifier.width(6.dp))
            DetailsStatus(booking.status)
        }
    }
}

@Composable
private fun DetailsStatus(rawStatus: String) {
    val status = rawStatus.trim().lowercase(Locale.ROOT)

    val (background, foreground) = when (status) {
        "confirmed", "completed" -> Color(0xFFEAF4ED) to Color(0xFF476A50)
        "cancelled", "canceled" -> Color(0xFFF8E2E7) to Color(0xFF99475D)
        "pending" -> Color(0xFFF7EFE0) to Color(0xFF755B22)
        else -> Color(0xFFECE9EA) to DetailMuted
    }

    Row(
        modifier = Modifier
            .background(background, CircleShape)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(4.dp).background(foreground, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            text = status.replaceFirstChar { it.uppercaseChar() },
            color = foreground,
            fontSize = 13.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = DetailFont,
        fontWeight = FontWeight.SemiBold,
        color = DetailInk,
        fontSize = 17.sp
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(DetailBlush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DetailInk,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                color = DetailMuted,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
            Text(
                text = value,
                color = DetailInk,
                fontFamily = DetailFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(13.dp),
        color = if (filled) DetailRose else Color.White,
        contentColor = if (filled) Color.White else DetailRose,
        border = if (filled) null else BorderStroke(1.dp, DetailRose)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontFamily = DetailFont,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailsBottomBar(
    onHome: (() -> Unit)?,
    onServices: (() -> Unit)?,
    onProfile: (() -> Unit)?
) {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = DetailRose.copy(alpha = 0.55f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("Home", Icons.Outlined.Home, false, onHome)
        BottomItem("Services", Icons.AutoMirrored.Outlined.Assignment, true, onServices)
        BottomItem("Profile", Icons.Outlined.PersonOutline, false, onProfile)
    }
}

@Composable
private fun BottomItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: (() -> Unit)?
) {
    val clickModifier = if (onClick == null) Modifier
    else Modifier.clickable(onClick = onClick)

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) DetailSelected else Color.Transparent)
            .then(clickModifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DetailInk,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = title,
            color = DetailInk,
            fontFamily = DetailFont,
            fontSize = 12.sp,
            lineHeight = 15.sp
        )
    }
}

private val confirmedPreview = BookingDetails(
    provider = "Zahra Alsaffar",
    category = "Florist",
    region = "Eastern Province",
    status = "Confirmed",
    date = "11 March 2027",
    time = "10:00 AM",
    location = "Dammam",
    service = "Hair Stylists",
    price = "SAR 300",
    paymentStatus = "Deposit Due"
)

private val completedPreview = BookingDetails(
    provider = "Rose Land",
    category = "Florist",
    region = "Eastern Province",
    status = "Completed",
    date = "11 March 2027",
    time = "5:00 PM",
    location = "Dammam",
    service = "Bridal Flowers",
    price = "SAR 200",
    paymentStatus = "Paid"
)

@Composable
private fun DetailsPreview(booking: BookingDetails) {
    var example by remember(booking) { mutableStateOf(booking) }

    MaterialTheme {
        DetailsContent(
            booking = example,
            onBack = {},
            onMessage = {},
            onReview = {},
            onCancel = { example = example.copy(status = "Cancelled") },
            cancelling = false,
            cancelError = null,
            onHome = null,
            onServices = null,
            onProfile = null
        )
    }
}

@Preview(
    name = "Appointment Details - Confirmed",
    showBackground = true,
    widthDp = 402,
    heightDp = 874
)
@Composable
private fun ConfirmedDetailsPreview() {
    DetailsPreview(confirmedPreview)
}

@Preview(
    name = "Appointment Details - Completed",
    showBackground = true,
    widthDp = 402,
    heightDp = 874
)
@Composable
private fun CompletedDetailsPreview() {
    DetailsPreview(completedPreview)
}