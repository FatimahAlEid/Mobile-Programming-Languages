package com.example.brideline

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/*
 * Brideline — Groom Home Page (wireframe "groom_home", Milestone 2)
 * Reads (Milestone 3-A): User.fullName, Wedding.weddingDate / totalBudget, Payment.amount / status.
 * Self-contained: colours, sample data and components are all in this file.
 */

// ---------------- Colours (sampled from the Milestone 2 wireframes) ----------------
private val Rose = Color(0xFFD2647D)
private val PinkCard = Color(0xFFEDBEC6)
private val PinkLight = Color(0xFFFADDE2)
private val Canvas = Color(0xFFEDEEF3)
private val Plum = Color(0xFF3E2A30)

private val HomeColors = lightColorScheme(
    primary = Rose, onPrimary = Color.White, background = Canvas, onBackground = Plum,
    surface = Color.White, onSurface = Plum, onSurfaceVariant = Color(0xFF7A6A6E),
)

// ---------------- Sample data ----------------
private data class HomeUpcoming(val title: String, val due: Date, val amount: Double)

private data class GroomHomeInfo(
    val groomName: String,
    val weddingDate: Date,
    val totalPaid: Double,
    val remaining: Double,
    val upcoming: HomeUpcoming?,
)

private fun homeDate(y: Int, m: Int, d: Int): Date = Calendar.getInstance().apply { set(y, m - 1, d, 18, 0, 0) }.time

private val sampleGroomHome = GroomHomeInfo(
    groomName = "Ayman",
    weddingDate = homeDate(2027, 3, 11),
    totalPaid = 4500.0,
    remaining = 14500.0,
    upcoming = HomeUpcoming("Venue Deposit", homeDate(2026, 12, 20), 13000.0),
)

// ---------------- Screen ----------------
/**
 * Groom Home Page.
 * @param onPaymentHistory open Payment History (Quick Access tile and "See all")
 * @param onDueDates open Due Dates
 * @param onInvoices open Invoices
 */
@Composable
fun GroomHomeScreen(
    onPaymentHistory: () -> Unit = {},
    onDueDates: () -> Unit = {},
    onInvoices: () -> Unit = {},
) {
    MaterialTheme(colorScheme = HomeColors) {
        GroomHomeBody(sampleGroomHome, onPaymentHistory, onDueDates, onInvoices)
    }
}

@Composable
private fun GroomHomeBody(data: GroomHomeInfo, onPaymentHistory: () -> Unit, onDueDates: () -> Unit, onInvoices: () -> Unit) {
    Scaffold(containerColor = Canvas, bottomBar = { HomeBottomBar() }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                Text("Bride Line . Groom", color = Rose, style = MaterialTheme.typography.bodyMedium)
                Text("Plan your wedding", color = Plum, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Text("Welcome back, ${data.groomName}", color = Rose, style = MaterialTheme.typography.bodyLarge)
            }

            Column(
                Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(Rose, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                val days = TimeUnit.MILLISECONDS.toDays(data.weddingDate.time - System.currentTimeMillis()).coerceAtLeast(0)
                Text("Your wedding day", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyLarge)
                Text(SimpleDateFormat("d MMM yyyy", Locale.US).format(data.weddingDate), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                Text("$days days left", color = Color.White.copy(alpha = 0.9f))
            }

            HomeSectionTitle("Quick Access")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTile("🗓", "Due Dates", onDueDates, Modifier.weight(1f))
                QuickTile("💳", "Payment History", onPaymentHistory, Modifier.weight(1f), highlighted = true)
                QuickTile("🧾", "Invoices", onInvoices, Modifier.weight(1f))
            }

            HomeSectionTitle("Payment Overview", "See all", onPaymentHistory)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewTile("💵", "Total paid", homeSar(data.totalPaid), Modifier.weight(1f))
                OverviewTile("📄", "Remaining", homeSar(data.remaining), Modifier.weight(1f))
            }

            HomeSectionTitle("Upcoming Appointments")
            val up = data.upcoming
            if (up == null) {
                Text("No upcoming payments 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(
                    Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(10.dp))
                        .background(PinkCard, RoundedCornerShape(10.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) { Text("💰", fontSize = 34.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(up.title, color = Plum, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Due ${SimpleDateFormat("d MMM yyyy", Locale.US).format(up.due)} - ${String.format(Locale.US, "%,.0f", up.amount)}",
                            color = Plum, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun homeSar(v: Double) = String.format(Locale.US, "%,.0f SAR", v)

@Composable
private fun HomeSectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Plum, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action, color = Rose) }
    }
}

@Composable
private fun QuickTile(icon: String, label: String, onClick: () -> Unit, modifier: Modifier, highlighted: Boolean = false) {
    Column(
        modifier.height(76.dp).shadow(3.dp, RoundedCornerShape(10.dp))
            .background(if (highlighted) PinkCard else PinkLight, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Plum, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun OverviewTile(icon: String, label: String, value: String, modifier: Modifier) {
    Row(
        modifier.height(64.dp).shadow(3.dp, RoundedCornerShape(10.dp))
            .background(PinkLight, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 26.sp)
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(label, color = Plum, style = MaterialTheme.typography.labelMedium)
            Text(value, color = Plum, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Groom bottom bar: Home (selected), Budget, Profile. */
@Composable
private fun HomeBottomBar() {
    Column(Modifier.fillMaxWidth().background(Canvas)) {
        HorizontalDivider(color = PinkCard)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("⌂" to "Home", "▤" to "Budget", "👤" to "Profile").forEachIndexed { i, (icon, label) ->
                Column(
                    Modifier.size(64.dp).clip(CircleShape).background(if (i == 0) PinkCard else Color.Transparent),
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
@Preview(showBackground = true, name = "Groom Home", heightDp = 800)
@Composable
private fun GroomHomeScreenPreview() {
    GroomHomeScreen()
}

@Preview(showBackground = true, name = "Groom Home – nothing due", heightDp = 800)
@Composable
private fun GroomHomeScreenEmptyPreview() {
    MaterialTheme(colorScheme = HomeColors) {
        GroomHomeBody(sampleGroomHome.copy(upcoming = null), {}, {}, {})
    }
}
