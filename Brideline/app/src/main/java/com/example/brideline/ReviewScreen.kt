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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
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
 * Brideline — View Reviews (wireframe "provider_reviews", Milestone 2)
 * Reads (Milestone 3-A): BusinessDetails.businessName,
 * Rating.stars / review / reviewDate / reviewerId (+ User.fullName of the reviewer).
 * Self-contained: colours, sample data and components are all in this file.
 */

// ---------------- Colours (sampled from the Milestone 2 wireframes) ----------------
private val Rose = Color(0xFFD2647D)
private val PinkCard = Color(0xFFEDBEC6)
private val Canvas = Color(0xFFEDEEF3)
private val Plum = Color(0xFF3E2A30)

private val ReviewColors = lightColorScheme(
    primary = Rose, onPrimary = Color.White, background = Canvas, onBackground = Plum,
    surface = Color.White, onSurface = Plum, onSurfaceVariant = Color(0xFF7A6A6E),
)

// ---------------- Data ----------------
private data class ProviderReview(val id: String, val reviewer: String, val stars: Int, val text: String, val date: Date)

private data class ProviderReviewList(val providerName: String, val reviews: List<ProviderReview>) {
    val average get() = if (reviews.isEmpty()) 0.0 else reviews.map { it.stars }.average()
    fun share(stars: Int) = if (reviews.isEmpty()) 0f else reviews.count { it.stars == stars }.toFloat() / reviews.size
}

private fun reviewDate(y: Int, m: Int, d: Int): Date = Calendar.getInstance().apply { set(y, m - 1, d, 18, 0, 0) }.time

private val sampleReviews = ProviderReviewList(
    "Rose Land",
    listOf(
        ProviderReview("b1", "Sara A.", 5, "Beautiful flowers and excellent service.", reviewDate(2026, 9, 15)),
        ProviderReview("b2", "Noura M.", 5, "The arrangements were elegant and exactly what I wanted.", reviewDate(2026, 9, 2)),
        ProviderReview("b3", "Layan H.", 4, "Lovely flowers and good communication.", reviewDate(2026, 8, 20)),
    ),
)

// ---------------- Screen ----------------
/**
 * View Reviews for a service provider.
 * @param onBack go back to the previous screen
 */
@Composable
fun ReviewsScreen(onBack: () -> Unit = {}) {
    MaterialTheme(colorScheme = ReviewColors) {
        ReviewsBody(sampleReviews, onBack)
    }
}

@Composable
private fun ReviewsBody(data: ProviderReviewList, onBack: () -> Unit) {
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    val reviews = if (newestFirst) data.reviews.sortedByDescending { it.date } else data.reviews.sortedByDescending { it.stars }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Text("←", fontSize = 22.sp, color = Plum) }
                Text("Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Plum)
            }
        },
        bottomBar = { ReviewsBottomBar() },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { RatingSummaryCard(data) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Customer Reviews", color = Plum, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(
                        "Sort by: ${if (newestFirst) "Most Recent" else "Highest Rated"} ▾", color = Plum, style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { newestFirst = !newestFirst },
                    )
                }
            }
            if (reviews.isEmpty()) {
                item {
                    Text("No reviews yet", Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            } else {
                items(reviews, key = { it.id }) { ReviewItemCard(it) }
            }
        }
    }
}

@Composable
private fun RatingSummaryCard(data: ProviderReviewList) {
    Column(
        Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(12.dp))
            .background(PinkCard, RoundedCornerShape(12.dp)).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(data.providerName, color = Plum, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(String.format(Locale.US, "%.1f ★", data.average), color = Plum, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("Based on ${data.reviews.size} reviews", color = Plum, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        for (stars in 5 downTo 1) {
            val share = data.share(stars)
            Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$stars ★", color = Plum, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp))
                LinearProgressIndicator(
                    progress = { share }, color = Rose, trackColor = Color.White,
                    strokeCap = StrokeCap.Round, drawStopIndicator = {},
                    modifier = Modifier.weight(1f).height(6.dp),
                )
                Text("${Math.round(share * 100)}%", color = Plum, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 12.dp).width(36.dp))
            }
        }
    }
}

@Composable
private fun ReviewItemCard(r: ProviderReview) {
    Column(
        Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.reviewer, color = Plum, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("★".repeat(r.stars) + "☆".repeat(5 - r.stars), color = Rose, fontSize = 14.sp, letterSpacing = 1.sp)
        }
        Text("“${r.text}”", color = Plum, style = MaterialTheme.typography.bodyMedium)
        Text(SimpleDateFormat("dd MMM yyyy", Locale.US).format(r.date), color = Plum, style = MaterialTheme.typography.bodySmall)
    }
}

/** Bottom bar from the wireframe: Home, Services (selected), Profile. */
@Composable
private fun ReviewsBottomBar() {
    Column(Modifier.fillMaxWidth().background(Canvas)) {
        HorizontalDivider(color = PinkCard)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("⌂" to "Home", "▤" to "Services", "👤" to "Profile").forEachIndexed { i, (icon, label) ->
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
@Preview(showBackground = true, name = "View Reviews", heightDp = 800)
@Composable
private fun ReviewsScreenPreview() {
    ReviewsScreen()
}

@Preview(showBackground = true, name = "View Reviews – empty", heightDp = 800)
@Composable
private fun ReviewsScreenEmptyPreview() {
    MaterialTheme(colorScheme = ReviewColors) { ReviewsBody(sampleReviews.copy(reviews = emptyList()), onBack = {}) }
}
