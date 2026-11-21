package com.example.brideline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val detailsPage = Color(0xFFECEDF3)
private val detailsPink = Color(0xFFEDBEC8)
private val detailsRose = Color(0xFFD2647D)
private val detailsInk = Color(0xFF45353A)
private val detailsMuted = Color(0xFF756D72)
private val detailsGreen = Color(0xFF42634E)
private val detailsPaleGreen = Color(0xFFEAF3EC)

private val detailsFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private data class CategoryBooking(
    val id: String,
    val provider: String,
    val status: String,
    val scheduledDate: Date?,
    val imageUrl: String
)

private data class BookingPayment(
    val id: String,
    val title: String,
    val amount: Double?,
    val status: String,
    val dueDate: Date?,
    val paidDate: Date?
)

private data class BookingInvoice(
    val id: String,
    val title: String,
    val amount: Double?,
    val date: Date?
)

private data class DetailsResult<T>(
    val rows: List<T> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

private fun categoryKey(value: String): String =
    when (val key = value.trim().lowercase(Locale.ROOT)) {
        "hair", "hair stylist", "hair stylists" -> "hair"
        "makeup", "makeup artist", "makeup artists" -> "makeup"
        "venue", "venues" -> "venues"
        "photography", "photo" -> "photo"
        else -> key
    }

private fun categoryTitle(categoryId: String): String =
    when (categoryKey(categoryId)) {
        "hair" -> "Hair Stylist"
        "makeup" -> "Makeup Artist"
        "venues" -> "Venue"
        "buffet" -> "Buffet"
        "dj" -> "DJ"
        "photo" -> "Photography"
        else -> categoryId
    }

private fun displayDate(date: Date): String =
    SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(date)

private fun displayAmount(amount: Double?): String {
    if (amount == null) return "—"

    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    return "SAR ${formatter.format(amount)}"
}

private fun displayStatus(raw: String): String =
    raw.trim().lowercase(Locale.ROOT).replaceFirstChar { it.uppercaseChar() }

@Composable
private fun <T> observeDetails(
    query: Query,
    readRows: (List<DocumentSnapshot>) -> List<T>
): DetailsResult<T> {
    var result by remember(query) { mutableStateOf(DetailsResult<T>()) }
    val latestReadRows by rememberUpdatedState(readRows)

    DisposableEffect(query) {
        val listener = query.addSnapshotListener { snapshot, failure ->
            if (failure != null) {
                result = DetailsResult(
                    loading = false,
                    error = failure.localizedMessage ?: "Could not load booking details."
                )
            } else if (snapshot != null) {
                result = try {
                    DetailsResult(
                        rows = latestReadRows(snapshot.documents),
                        loading = false
                    )
                } catch (error: RuntimeException) {
                    DetailsResult(
                        loading = false,
                        error = error.localizedMessage ?: "Check the document fields."
                    )
                }
            }
        }

        onDispose { listener.remove() }
    }

    return result
}

@Composable
fun BudgetCategoryDetailsScreen(
    weddingId: String,
    categoryId: String,
    roleLabel: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onBudget: () -> Unit,
    onProfile: () -> Unit,
    onAddPayment: (bookingId: String) -> Unit,
    onViewInvoice: (bookingId: String, invoiceId: String) -> Unit,
    modifier: Modifier = Modifier,
    openPaymentLocally: Boolean = true
) {
    var selectedBookingId by rememberSaveable(weddingId, categoryId) {
        mutableStateOf<String?>(null)
    }
    var paymentBookingId by rememberSaveable(weddingId, categoryId) {
        mutableStateOf<String?>(null)
    }

    val bookingId = paymentBookingId

    if (bookingId != null) {
        AddPaymentScreen(
            bookingId = bookingId,
            roleLabel = roleLabel,
            onBack = { paymentBookingId = null },
            onHome = {
                paymentBookingId = null
                onHome()
            },
            onBudget = {
                paymentBookingId = null
                onBudget()
            },
            onProfile = {
                paymentBookingId = null
                onProfile()
            },
            onSaved = { paymentBookingId = null },
            modifier = modifier
        )
        return
    }

    val title = categoryTitle(categoryId)

    DetailsLayout(
        title = title,
        roleLabel = roleLabel,
        onBack = onBack,
        onHome = onHome,
        onBudget = onBudget,
        onProfile = onProfile,
        modifier = modifier
    ) {
        if (weddingId.isBlank()) {
            DetailsMessage("Your wedding could not be identified.")
        } else {
            key(weddingId, categoryId) {
                val db = remember { FirebaseFirestore.getInstance() }
                val bookingsQuery = remember(db, weddingId) {
                    db.collection("bookings").whereEqualTo("weddingId", weddingId)
                }
                val bookings = observeDetails(bookingsQuery) { documents ->
                    documents.filter { document ->
                        val category = document.getString("categoryId")
                            .orEmpty()
                            .ifBlank { document.getString("category").orEmpty() }
                        categoryKey(category) == categoryKey(categoryId)
                    }.map { document ->
                        CategoryBooking(
                            id = document.id,
                            provider = document.getString("providerName")
                                .orEmpty()
                                .ifBlank { "Service provider" },
                            status = document.getString("status").orEmpty(),
                            scheduledDate = document.getTimestamp("scheduledDate")?.toDate(),
                            imageUrl = document.getString("imageUrl").orEmpty()
                        )
                    }.sortedWith(
                        compareBy<CategoryBooking> {
                            it.scheduledDate?.time ?: Long.MAX_VALUE
                        }.thenBy { it.id }
                    )
                }

                when {
                    bookings.loading -> DetailsLoading()
                    bookings.error != null -> DetailsMessage(bookings.error)
                    bookings.rows.isEmpty() -> {
                        DetailsMessage("No bookings in this category yet.")
                    }

                    else -> {
                        val selected = bookings.rows.firstOrNull {
                            it.id == selectedBookingId
                        } ?: bookings.rows.first()

                        key(selected.id) {
                            val paymentsQuery = remember(db, selected.id) {
                                db.collection("payments")
                                    .whereEqualTo("bookingId", selected.id)
                            }
                            val invoicesQuery = remember(db, selected.id) {
                                db.collection("invoices")
                                    .whereEqualTo("bookingId", selected.id)
                            }
                            val payments = observeDetails(paymentsQuery) { documents ->
                                documents.map { document ->
                                    BookingPayment(
                                        id = document.id,
                                        title = document.getString("title")
                                            .orEmpty()
                                            .ifBlank { "Payment" },
                                        amount = (document.get("amount") as? Number)?.toDouble(),
                                        status = document.getString("status").orEmpty(),
                                        dueDate = document.getTimestamp("dueDate")?.toDate(),
                                        paidDate = document.getTimestamp("paidDate")?.toDate()
                                    )
                                }.sortedBy {
                                    (it.dueDate ?: it.paidDate)?.time ?: Long.MAX_VALUE
                                }
                            }
                            val invoices = observeDetails(invoicesQuery) { documents ->
                                documents.map { document ->
                                    BookingInvoice(
                                        id = document.id,
                                        title = document.getString("title")
                                            .orEmpty()
                                            .ifBlank { "Invoice" },
                                        amount = (document.get("amount") as? Number)?.toDouble(),
                                        date = document.getTimestamp("invoiceDate")?.toDate()
                                    )
                                }.sortedByDescending { it.date?.time }
                            }

                            BookingDetailsList(
                                title = title,
                                bookings = bookings.rows,
                                selectedId = selected.id,
                                payments = payments,
                                invoices = invoices,
                                onSelect = { selectedBookingId = it },
                                onAddPayment = {
                                    if (openPaymentLocally) {
                                        paymentBookingId = selected.id
                                    } else {
                                        onAddPayment(selected.id)
                                    }
                                },
                                onViewInvoice = { invoiceId ->
                                    onViewInvoice(selected.id, invoiceId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsLayout(
    title: String,
    roleLabel: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onBudget: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(detailsPage)
            .safeDrawingPadding()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = detailsInk)
            }

            DetailsText(
                text = "Budget & Payments",
                size = 22,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        Column(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        ) {
            DetailsText(roleLabel, 13, detailsRose)
            DetailsText("$title Details", 26)
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            content()
        }

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
}

@Composable
private fun ProviderLogo(booking: CategoryBooking) {
    val imageUrl = booking.imageUrl.trim()
    val photoPainter = rememberVectorPainter(Icons.Outlined.Photo)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isEmpty()) {
            Icon(
                painter = photoPainter,
                contentDescription = "No provider photo",
                tint = detailsMuted,
                modifier = Modifier.size(28.dp)
            )
        } else {
            var imageLoaded by remember(imageUrl) { mutableStateOf(false) }

            AsyncImage(
                model = imageUrl,
                contentDescription = booking.provider,
                modifier = if (imageLoaded) Modifier.fillMaxSize() else Modifier.size(28.dp),
                contentScale = if (imageLoaded) ContentScale.Crop else ContentScale.Fit,
                placeholder = photoPainter,
                error = photoPainter,
                colorFilter = if (imageLoaded) null else ColorFilter.tint(detailsMuted),
                onLoading = { imageLoaded = false },
                onSuccess = { imageLoaded = true },
                onError = { imageLoaded = false }
            )
        }
    }
}

@Composable
private fun BookingDetailsList(
    title: String,
    bookings: List<CategoryBooking>,
    selectedId: String,
    payments: DetailsResult<BookingPayment>,
    invoices: DetailsResult<BookingInvoice>,
    onSelect: (String) -> Unit,
    onAddPayment: () -> Unit,
    onViewInvoice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bookings, key = { "booking-${it.id}" }) { booking ->
            val selected = booking.id == selectedId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) detailsPink else Color.White)
                    .clickable(enabled = bookings.size > 1) { onSelect(booking.id) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderLogo(booking)

                Column(Modifier.weight(1f)) {
                    DetailsText(booking.provider, 18)
                    DetailsText(title, 13, detailsMuted)

                    booking.scheduledDate?.let { date ->
                        Spacer(Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = detailsMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            DetailsText(displayDate(date), 12, detailsMuted)
                        }
                    }
                }

                if (booking.status.isNotBlank()) {
                    DetailsStatus(booking.status)
                }

                if (bookings.size > 1) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = detailsRose,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailsText("Payments", 17, modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onAddPayment,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    DetailsText(
                        text = "+ Add Payment",
                        size = 13,
                        modifier = Modifier
                            .background(detailsPink, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }

        when {
            payments.loading -> item { DetailsLoading() }
            payments.error != null -> item { DetailsMessage(payments.error) }
            payments.rows.isEmpty() -> item { DetailsMessage("No payments yet.") }

            else -> items(payments.rows, key = { "payment-${it.id}" }) { payment ->
                val paid = payment.status.trim().equals("paid", ignoreCase = true)
                val dateLine = if (paid) {
                    payment.paidDate?.let { "Paid ${displayDate(it)}" }
                } else {
                    payment.dueDate?.let { "Due ${displayDate(it)}" }
                }
                val icon = if (payment.title.trim().equals("Final Payment", ignoreCase = true)) {
                    R.drawable.ic_final_payment
                } else {
                    R.drawable.ic_booking_payment
                }

                DetailsCard(
                    title = payment.title,
                    subtitle = dateLine.orEmpty(),
                    amount = payment.amount,
                    iconResource = icon
                ) {
                    if (payment.status.isNotBlank()) {
                        DetailsStatus(payment.status)
                    }
                }
            }
        }

        item {
            DetailsText(
                text = "Invoice",
                size = 17,
                modifier = Modifier.padding(start = 10.dp, top = 14.dp, bottom = 4.dp)
            )
        }

        when {
            invoices.loading -> item { DetailsLoading() }
            invoices.error != null -> item { DetailsMessage(invoices.error) }
            invoices.rows.isEmpty() -> item { DetailsMessage("No invoices yet.") }

            else -> items(invoices.rows, key = { "invoice-${it.id}" }) { invoice ->
                DetailsCard(
                    title = invoice.title,
                    subtitle = invoice.date?.let(::displayDate).orEmpty(),
                    amount = invoice.amount,
                    iconResource = R.drawable.ic_booking_invoice
                ) {
                    DetailsText(
                        text = "View Invoice",
                        size = 12,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(detailsPink)
                            .clickable { onViewInvoice(invoice.id) }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsCard(
    title: String,
    subtitle: String,
    amount: Double?,
    iconResource: Int,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(detailsPink, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResource),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(Modifier.weight(1f)) {
            DetailsText(title, 15)

            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                DetailsText(subtitle, 12, detailsMuted)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            DetailsText(displayAmount(amount), 15)
            Spacer(Modifier.height(4.dp))
            action()
        }
    }
}

@Composable
private fun DetailsStatus(raw: String) {
    val positive = raw.trim().lowercase(Locale.ROOT) in
            setOf("paid", "confirmed", "completed")

    DetailsText(
        text = "• ${displayStatus(raw)}",
        size = 12,
        color = if (positive) detailsGreen else detailsInk,
        modifier = Modifier
            .background(if (positive) detailsPaleGreen else detailsPink, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun DetailsText(
    text: String,
    size: Int,
    color: Color = detailsInk,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = detailsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp,
        lineHeight = (size + 4).sp
    )
}

@Composable
private fun DetailsMessage(message: String) {
    DetailsText(
        text = message,
        size = 14,
        color = detailsMuted,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )
}

@Composable
private fun DetailsLoading() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = detailsRose,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(
    name = "Hair Stylist Details",
    showBackground = true,
    widthDp = 402,
    heightDp = 760
)
@Composable
private fun BudgetCategoryDetailsPreview() {
    val frameShape = RoundedCornerShape(56.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(10.dp)
            .border(
                width = 2.dp,
                color = Color(0xFFC95772).copy(alpha = 0.7f),
                shape = frameShape
            )
            .clip(frameShape)
    ) {
        SampleBudgetCategoryDetails(categoryId = "hair", onBack = {})
    }
}

@Composable
internal fun SampleBudgetCategoryDetails(
    categoryId: String,
    onBack: () -> Unit
) {
    val roleLabel = "Bride Line.Groom"
    val bookingId = "sample-booking"
    val title = categoryTitle(categoryId)
    val dates = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    var showAddPayment by rememberSaveable(categoryId) { mutableStateOf(false) }
    var finalPaymentPaidAt by rememberSaveable(bookingId) {
        mutableStateOf<Long?>(null)
    }

    val paidAt = finalPaymentPaidAt
    val payments = listOf(
        BookingPayment(
            id = "deposit",
            title = "Booking Deposit",
            amount = 160.0,
            status = "Paid",
            dueDate = null,
            paidDate = dates.parse("2026-05-05")
        ),
        BookingPayment(
            id = "second",
            title = "Second Payment",
            amount = 400.0,
            status = "Paid",
            dueDate = null,
            paidDate = dates.parse("2026-08-13")
        ),
        BookingPayment(
            id = "final",
            title = "Final Payment",
            amount = 400.0,
            status = if (paidAt == null) "Upcoming" else "Paid",
            dueDate = dates.parse("2027-02-15"),
            paidDate = paidAt?.let { Date(it) }
        )
    )

    if (showAddPayment) {
        AddPaymentScreen(
            bookingId = bookingId,
            roleLabel = roleLabel,
            onBack = { showAddPayment = false },
            onHome = {},
            onBudget = {
                showAddPayment = false
                onBack()
            },
            onProfile = {},
            onSaved = { showAddPayment = false },
            unpaidPayments = payments
                .filter { !it.status.equals("Paid", ignoreCase = true) }
                .map { it.id to it.title },
            onPaymentPaid = { paymentId ->
                if (paymentId == "final" && finalPaymentPaidAt == null) {
                    finalPaymentPaidAt = System.currentTimeMillis()
                }
            }
        )
        return
    }

    DetailsLayout(
        title = title,
        roleLabel = roleLabel,
        onBack = onBack,
        onHome = {},
        onBudget = onBack,
        onProfile = {}
    ) {
        if (categoryKey(categoryId) == "hair") {
            BookingDetailsList(
                title = title,
                bookings = listOf(
                    CategoryBooking(
                        id = bookingId,
                        provider = "Zahra Alsaffar",
                        status = "Confirmed",
                        scheduledDate = dates.parse("2027-03-11"),
                        imageUrl = ""
                    )
                ),
                selectedId = bookingId,
                payments = DetailsResult(rows = payments, loading = false),
                invoices = DetailsResult(
                    rows = listOf(
                        BookingInvoice(
                            id = "invoice",
                            title = "Bride Hair Styling Package",
                            amount = 700.0,
                            date = dates.parse("2026-10-05")
                        )
                    ),
                    loading = false
                ),
                onSelect = {},
                onAddPayment = { showAddPayment = true },
                onViewInvoice = {}
            )
        } else {
            DetailsMessage("No bookings in this category yet.")
        }
    }
}