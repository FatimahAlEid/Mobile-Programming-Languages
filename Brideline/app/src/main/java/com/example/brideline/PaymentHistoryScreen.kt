package com.example.brideline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Brideline — Payment History (wireframe "payment_history", Milestone 2)
 * Reads (Milestone 3-A): Payment.amount / paidAt / status,
 * BusinessDetails.businessName and Service.serviceName for each booking.
 * Self-contained: colours, sample data and components are all in this file.
 */

// ---------------- Colours (sampled from the Milestone 2 wireframes) ----------------
private val Rose = Color(0xFFD2647D)
private val PinkCard = Color(0xFFEDBEC6)
private val PinkLight = Color(0xFFFADDE2)
private val Canvas = Color(0xFFEDEEF3)
private val Plum = Color(0xFF3E2A30)

private val HistoryColors = lightColorScheme(
    primary = Rose, onPrimary = Color.White, background = Canvas, onBackground = Plum,
    surface = Color.White, onSurface = Plum, onSurfaceVariant = Color(0xFF7A6A6E),
)

// ---------------- Data ----------------
private enum class HistoryPayStatus(val label: String, val text: Color, val bg: Color) {
    PAID("Paid", Color(0xFF4C8A5B), Color(0xFFDCEBDF)),
    PENDING("Pending", Color(0xFFB7791F), Color(0xFFF6E6CC)),
    UNPAID("Unpaid", Color(0xFFC2506B), Color(0xFFF5D3DA)),
}

private data class HistoryPayment(
    val id: String,
    val title: String,      // BusinessDetails.businessName
    val subtitle: String,   // Service.serviceName / category
    val date: Date,         // Payment.paidAt
    val amount: Double,     // Payment.amount
    val status: HistoryPayStatus,
)

private fun historyDate(y: Int, m: Int, d: Int): Date = Calendar.getInstance().apply { set(y, m - 1, d, 18, 0, 0) }.time

private val sampleHistory = listOf(
    HistoryPayment("p1", "Venue Deposit", "Wedding Venue", historyDate(2026, 10, 5), 13000.0, HistoryPayStatus.PAID),
    HistoryPayment("p2", "Lulu Al-Otaibi", "Makeup Artist", historyDate(2026, 9, 20), 300.0, HistoryPayStatus.PAID),
    HistoryPayment("p3", "Rose Land", "Bridal Flowers", historyDate(2026, 9, 15), 200.0, HistoryPayStatus.UNPAID),
    HistoryPayment("p4", "Glam by Sara", "Hair Stylist", historyDate(2026, 8, 30), 450.0, HistoryPayStatus.PENDING),
)

// ---------------- Screen ----------------
/**
 * Payment History.
 * @param onBack go back to Groom Home
 */
@Composable
fun PaymentHistoryScreen(onBack: () -> Unit = {}) {
    MaterialTheme(colorScheme = HistoryColors) {
        PaymentHistoryBody(sampleHistory, onBack)
    }
}

@Composable
private fun PaymentHistoryBody(payments: List<HistoryPayment>, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<HistoryPayStatus?>(null) } // null = All
    var newestFirst by rememberSaveable { mutableStateOf(true) }

    val shown = payments
        .filter { filter == null || it.status == filter }
        .filter { query.isBlank() || it.title.contains(query, true) || it.subtitle.contains(query, true) }
        .let { if (newestFirst) it.sortedByDescending { p -> p.date } else it.sortedBy { p -> p.date } }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Text("←", fontSize = 22.sp, color = Plum) }
                Text("Payment History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Plum)
            }
        },
        bottomBar = { HistoryBottomBar() },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().background(PinkCard, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔍", fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) Text("Search payment history...", color = Plum.copy(alpha = 0.7f))
                        BasicTextField(query, { query = it }, singleLine = true, textStyle = TextStyle(color = Plum, fontSize = 16.sp), modifier = Modifier.fillMaxWidth())
                    }
                    Box(Modifier.size(30.dp).clip(CircleShape).background(PinkLight), contentAlignment = Alignment.Center) {
                        Text("☰", color = Rose, fontSize = 14.sp)
                    }
                }
            }
            item {
                Text("Filter by status", color = Plum, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryChip("All", filter == null) { filter = null }
                    HistoryPayStatus.entries.forEach { s -> HistoryChip(s.label, filter == s) { filter = s } }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Payment History", color = Plum, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(
                        "Sort by: ${if (newestFirst) "Newest" else "Oldest"} ▾", color = Plum, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { newestFirst = !newestFirst },
                    )
                }
            }
            if (shown.isEmpty()) {
                item {
                    Text(
                        if (payments.isEmpty()) "No payments yet" else "No matching payments",
                        Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(shown, key = { it.id }) { HistoryCard(it) }
            }
        }
    }
}

@Composable
private fun HistoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.White else Plum,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Rose else PinkLight.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun HistoryCard(p: HistoryPayment) {
    Column(
        Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(p.title, color = Plum, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(p.subtitle, color = Plum, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Surface(color = p.status.bg, contentColor = p.status.text, shape = RoundedCornerShape(50)) {
                Text("• ${p.status.label}", Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
            }
        }
        HistoryInfoRow("📅", "Date: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(p.date)}")
        HistoryInfoRow("💳", "Amount: ${String.format(Locale.US, "SAR %,.0f", p.amount)}")
    }
}

@Composable
private fun HistoryInfoRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(PinkCard), contentAlignment = Alignment.Center) { Text(icon, fontSize = 12.sp) }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Plum)
    }
}

/** Groom bottom bar: Home, Budget (selected), Profile. */
@Composable
private fun HistoryBottomBar() {
    Column(Modifier.fillMaxWidth().background(Canvas)) {
        HorizontalDivider(color = PinkCard)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("⌂" to "Home", "▤" to "Budget", "👤" to "Profile").forEachIndexed { i, (icon, label) ->
                Column(
                    Modifier.size(64.dp).clip(CircleShape).background(if (i == 1) PinkCard else Color.Transparent),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(icon, fontSize = 20.sp, color = Plum)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = Plum)
                }
            }
        }
    }
}

// ---------------- Previews ----------------
@Preview(showBackground = true, name = "Payment History", heightDp = 800)
@Composable
private fun PaymentHistoryScreenPreview() {
    PaymentHistoryScreen()
}

@Preview(showBackground = true, name = "Payment History – empty", heightDp = 800)
@Composable
private fun PaymentHistoryScreenEmptyPreview() {
    MaterialTheme(colorScheme = HistoryColors) { PaymentHistoryBody(emptyList(), onBack = {}) }
}
