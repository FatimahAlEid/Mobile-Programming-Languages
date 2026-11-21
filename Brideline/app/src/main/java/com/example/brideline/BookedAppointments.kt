package com.example.brideline

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.brideline.ui.BridelineServicesBottomBar
import com.example.brideline.ui.ServicesNavTab
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

private val ApptRose = Color(0xFFD2647D)
private val ApptBlush = Color(0xFFEDBEC8)
private val ApptInk = Color(0xFF45353A)
private val ApptMuted = Color(0xFF756D72)
private val ApptBackground = Color(0xFFECEDF3)
private val ApptPinkCard = Color(0xFFF8EEF0)

private val ApptFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private val pastStatuses = setOf("completed", "canceled", "cancelled")

private enum class AppointmentSubScreen {
    LIST,
    DETAILS,
    REVIEW,
    CHAT
}

private data class AppointmentRow(
    val id: String,
    val provider: String,
    val category: String,
    val time: Long,
    val status: String,
    val imageUrl: String
)

@Composable
fun BookedAppointmentsScreen(
    weddingId: String,
    onBack: () -> Unit,
    onCalendar: () -> Unit,
    onHome: () -> Unit,
    onServices: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubScreen by rememberSaveable(weddingId) {
        mutableStateOf(AppointmentSubScreen.LIST)
    }
    var selectedBookingId by rememberSaveable(weddingId) {
        mutableStateOf<String?>(null)
    }
    var reviewProviderName by rememberSaveable(weddingId) {
        mutableStateOf<String?>(null)
    }
    var past by rememberSaveable(weddingId) {
        mutableStateOf(false)
    }

    val listState = rememberLazyListState()

    val goBackStep: () -> Unit = {
        when (currentSubScreen) {
            AppointmentSubScreen.REVIEW,
            AppointmentSubScreen.CHAT -> {
                currentSubScreen = AppointmentSubScreen.DETAILS
            }
            AppointmentSubScreen.DETAILS -> {
                selectedBookingId = null
                reviewProviderName = null
                currentSubScreen = AppointmentSubScreen.LIST
            }
            AppointmentSubScreen.LIST -> {
                onBack()
            }
        }
    }

    BackHandler(enabled = currentSubScreen != AppointmentSubScreen.LIST) {
        goBackStep()
    }

    when (currentSubScreen) {
        AppointmentSubScreen.REVIEW -> {
            val bookingId = selectedBookingId
            val providerName = reviewProviderName.orEmpty().ifBlank { "Service Provider" }
            if (bookingId != null) {
                LeaveReviewScreen(
                    weddingId = weddingId,
                    appointmentId = bookingId,
                    providerName = providerName,
                    onBack = { currentSubScreen = AppointmentSubScreen.DETAILS },
                    onReviewSubmitted = { currentSubScreen = AppointmentSubScreen.DETAILS },
                    onHome = onHome,
                    onServices = onServices,
                    onProfile = onProfile,
                    modifier = modifier
                )
            } else {
                currentSubScreen = AppointmentSubScreen.LIST
            }
        }

        AppointmentSubScreen.CHAT -> {
            val bookingId = selectedBookingId
            if (bookingId != null) {
                ChatScreen(
                    appointmentId = bookingId,
                    onBack = { currentSubScreen = AppointmentSubScreen.DETAILS },
                    modifier = modifier,
                    onHome = onHome,
                    onServices = onServices,
                    onProfile = onProfile
                )
            } else {
                currentSubScreen = AppointmentSubScreen.LIST
            }
        }

        AppointmentSubScreen.DETAILS -> {
            val bookingId = selectedBookingId
            if (bookingId != null) {
                AppointmentDetailsScreen(
                    weddingId = weddingId,
                    appointmentId = bookingId,
                    onBack = {
                        selectedBookingId = null
                        reviewProviderName = null
                        currentSubScreen = AppointmentSubScreen.LIST
                    },
                    onMessageProvider = { id ->
                        selectedBookingId = id
                        currentSubScreen = AppointmentSubScreen.CHAT
                    },
                    onLeaveReview = { id, name ->
                        selectedBookingId = id
                        reviewProviderName = name
                        currentSubScreen = AppointmentSubScreen.REVIEW
                    },
                    modifier = modifier,
                    onHome = onHome,
                    onServices = onServices,
                    onProfile = onProfile
                )
            } else {
                currentSubScreen = AppointmentSubScreen.LIST
            }
        }

        AppointmentSubScreen.LIST -> {
            BookedAppointmentsList(
                weddingId = weddingId,
                past = past,
                onPastChange = { past = it },
                listState = listState,
                onBack = onBack,
                onCalendar = onCalendar,
                onAppointment = { id ->
                    selectedBookingId = id
                    currentSubScreen = AppointmentSubScreen.DETAILS
                },
                onHome = onHome,
                onServices = onServices,
                onProfile = onProfile,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BookedAppointmentsList(
    weddingId: String,
    past: Boolean,
    onPastChange: (Boolean) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBack: () -> Unit,
    onCalendar: () -> Unit,
    onAppointment: (String) -> Unit,
    onHome: () -> Unit,
    onServices: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rows by remember(weddingId) {
        mutableStateOf(emptyList<AppointmentRow>())
    }
    var loading by remember(weddingId) { mutableStateOf(true) }
    var error by remember(weddingId) { mutableStateOf<String?>(null) }
    var attempt by remember(weddingId) { mutableIntStateOf(0) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30.seconds)
        }
    }

    DisposableEffect(weddingId, attempt) {
        loading = true
        error = null

        val listener = if (weddingId.isBlank()) {
            loading = false
            error = "Your wedding could not be identified."
            null
        } else {
            FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("weddingId", weddingId)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, failure ->
                    if (failure != null) {
                        loading = false
                        error = failure.localizedMessage ?: "Could not load appointments."
                    } else if (snapshot != null) {
                        runCatching {
                            snapshot.documents.map { doc ->
                                val date = requireNotNull(doc.getTimestamp("scheduledDate")) {
                                    "Booking ${doc.id} is missing scheduledDate."
                                }

                                AppointmentRow(
                                    id = doc.id,
                                    provider = doc.getString("providerName")
                                        .orEmpty()
                                        .ifBlank { "Service Provider" },
                                    category = doc.getString("category").orEmpty(),
                                    time = date.toDate().time,
                                    status = doc.getString("status").orEmpty(),
                                    imageUrl = doc.getString("imageUrl").orEmpty()
                                )
                            }
                        }.onSuccess { bookings ->
                            rows = bookings
                            error = null
                            loading = bookings.isEmpty() && snapshot.metadata.isFromCache
                        }.onFailure { cause ->
                            loading = false
                            error = cause.localizedMessage ?: "Check the booking fields."
                        }
                    }
                }
        }

        onDispose { listener?.remove() }
    }

    BookedAppointmentsContent(
        rows = rows,
        past = past,
        now = now,
        loading = loading,
        error = error,
        listState = listState,
        onPastChange = onPastChange,
        onRetry = { attempt++ },
        onBack = onBack,
        onCalendar = onCalendar,
        onAppointment = onAppointment,
        onHome = onHome,
        onServices = onServices,
        onProfile = onProfile,
        modifier = modifier
    )
}

@Composable
private fun BookedAppointmentsContent(
    rows: List<AppointmentRow>,
    past: Boolean,
    now: Long,
    loading: Boolean,
    error: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onPastChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onCalendar: () -> Unit,
    onAppointment: (String) -> Unit,
    onHome: () -> Unit,
    onServices: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible = remember(rows, past, now) {
        val matching = rows.filter { row ->
            val normalizedStatus = row.status.trim().lowercase(Locale.ROOT)
            val isPastItem = normalizedStatus in pastStatuses || row.time < now
            isPastItem == past
        }

        if (past) matching.sortedByDescending { it.time }
        else matching.sortedBy { it.time }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ApptBackground)
            .safeDrawingPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ApptInk)
            }

            ApptText(
                "Booked Appointments",
                22,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = onCalendar,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Outlined.Event, "Calendar", tint = ApptRose)
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApptBlush)
                    .padding(2.dp)
            ) {
                listOf("Upcoming", "Past").forEachIndexed { index, title ->
                    val isPast = index == 1
                    val selected = past == isPast

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) ApptRose else Color.Transparent)
                            .clickable { onPastChange(isPast) },
                        contentAlignment = Alignment.Center
                    ) {
                        ApptText(title, 16, if (selected) Color.White else ApptInk)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ApptText(
                    if (past) "Past Appointments" else "Your Appointments",
                    18,
                    modifier = Modifier.weight(1f)
                )

                if (!loading && error == null) {
                    val label = if (visible.size == 1) "Booking" else "Bookings"
                    ApptText("${visible.size} $label", 12, ApptMuted)
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                error != null -> Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ApptText("Couldn’t load appointments", 18)
                    Text(error, color = ApptMuted, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = ApptRose)
                    }
                }

                loading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ApptRose)
                }

                visible.isEmpty() -> EmptyAppointments(past)

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(visible, key = { _, row -> row.id }) { index, row ->
                        AppointmentCard(
                            row = row,
                            pink = index % 2 == 0,
                            onClick = { onAppointment(row.id) }
                        )
                    }
                }
            }
        }

        BridelineServicesBottomBar(
            selectedTab = ServicesNavTab.Services,
            onTabSelected = { tab ->
                when (tab) {
                    ServicesNavTab.Home -> onHome()
                    ServicesNavTab.Services -> onServices()
                    ServicesNavTab.Profile -> onProfile()
                }
            }
        )
    }
}

@Composable
private fun EmptyAppointments(past: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(56.dp)
                .background(ApptBlush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.CalendarToday, null, tint = ApptRose)
        }

        Spacer(Modifier.height(12.dp))
        ApptText(
            if (past) "No past appointments yet." else "No upcoming appointments yet.",
            18
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (past) "Completed appointments will appear here."
            else "Your upcoming appointments will appear here.",
            color = ApptMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppointmentCard(
    row: AppointmentRow,
    pink: Boolean,
    onClick: () -> Unit
) {
    val dateText = remember(row.time) {
        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH)
            .format(Date(row.time))
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (pink) ApptPinkCard else Color.White,
        border = BorderStroke(1.dp, Color(0xFFE6DDE1)),
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(width = 76.dp, height = 92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ApptBlush.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Photo, null, tint = ApptMuted)

                if (row.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = row.imageUrl,
                        contentDescription = row.provider,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                ApptText(row.provider, 16)

                if (row.category.isNotBlank()) {
                    Text(row.category, color = ApptMuted, fontSize = 14.sp)
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        null,
                        Modifier.size(13.dp),
                        tint = ApptMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(dateText, color = ApptInk, fontSize = 12.sp)
                }

                if (row.status.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    BookingStatusBadge(row.status)
                }
            }

            Icon(
                Icons.Outlined.ChevronRight,
                null,
                Modifier.size(18.dp),
                tint = ApptMuted
            )
        }
    }
}

@Composable
internal fun BookingStatusBadge(raw: String) {
    val status = raw.trim().lowercase(Locale.ROOT).ifBlank { "unknown" }

    val (background, ink) = when (status) {
        "confirmed", "completed" -> Color(0xFFEAF3EC) to Color(0xFF406747)
        "pending" -> Color(0xFFF7EFE0) to Color(0xFF755B22)
        "cancelled", "canceled" -> Color(0xFFF8E2E7) to Color(0xFF99475D)
        else -> Color(0xFFECE9EA) to ApptMuted
    }

    Row(
        Modifier
            .background(background, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(4.dp)
                .background(ink, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        ApptText(status.replaceFirstChar { it.uppercaseChar() }, 11, ink)
    }
}

@Composable
private fun ApptText(
    text: String,
    size: Int,
    color: Color = ApptInk,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = ApptFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun AppointmentsPreviewContent(empty: Boolean) {
    var past by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var subScreen by rememberSaveable { mutableStateOf("LIST") }
    var reviewProvider by rememberSaveable { mutableStateOf("Pearl Hall") }
    val listState = rememberLazyListState()
    val now = remember { System.currentTimeMillis() }
    val day = 86_400_000L

    val rows = remember(empty, now) {
        if (empty) emptyList() else listOf(
            AppointmentRow("1", "Rose Studio", "Photography", now + day, "Confirmed", ""),
            AppointmentRow("2", "Bloom Events", "Flowers", now + 2 * day, "Pending", ""),
            AppointmentRow("3", "Pearl Hall", "Venue", now - day, "Completed", "")
        )
    }

    MaterialTheme {
        when (subScreen) {
            "CHAT" -> ChatScreen(
                appointmentId = selectedId ?: "1",
                onBack = { subScreen = "DETAILS" }
            )
            "REVIEW" -> LeaveReviewScreen(
                weddingId = "preview-wedding",
                appointmentId = selectedId ?: "3",
                providerName = reviewProvider,
                onBack = { subScreen = "DETAILS" },
                onReviewSubmitted = { subScreen = "DETAILS" }
            )
            "DETAILS" -> AppointmentDetailsScreen(
                weddingId = "preview-wedding",
                appointmentId = selectedId ?: "1",
                onBack = {
                    selectedId = null
                    subScreen = "LIST"
                },
                onMessageProvider = { subScreen = "CHAT" },
                onLeaveReview = { _, name ->
                    reviewProvider = name
                    subScreen = "REVIEW"
                }
            )
            else -> BookedAppointmentsContent(
                rows = rows,
                past = past,
                now = now,
                loading = false,
                error = null,
                listState = listState,
                onPastChange = { past = it },
                onRetry = {},
                onBack = {},
                onCalendar = {},
                onAppointment = { id ->
                    selectedId = id
                    subScreen = "DETAILS"
                },
                onHome = {},
                onServices = {},
                onProfile = {}
            )
        }
    }
}

@Preview(name = "With bookings", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun BookedAppointmentsPreview() {
    AppointmentsPreviewContent(empty = false)
}

@Preview(name = "Empty bookings", showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun EmptyBookedAppointmentsPreview() {
    AppointmentsPreviewContent(empty = true)
}