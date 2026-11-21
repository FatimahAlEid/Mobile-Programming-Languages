package com.example.brideline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.ui.BridelineServicesBottomBar
import com.example.brideline.ui.ServicesNavTab
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ReviewRose = Color(0xFFD2647D)
private val ReviewBlush = Color(0xFFEDBEC8)
private val ReviewInk = Color(0xFF45353A)
private val ReviewMuted = Color(0xFF756D72)
private val ReviewBackground = Color(0xFFECEDF3)
private val ReviewFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

private fun DocumentSnapshot.extractFormattedDate(vararg fieldNames: String): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    for (field in fieldNames) {
        val raw = get(field) ?: continue
        when (raw) {
            is Timestamp -> return formatter.format(raw.toDate())
            is Date -> return formatter.format(raw)
            is Long -> return formatter.format(Date(raw))
            is String -> if (raw.isNotBlank()) return raw.trim()
        }
    }
    return ""
}

@Composable
fun LeaveReviewScreen(
    weddingId: String,
    appointmentId: String,
    providerName: String,
    onBack: () -> Unit,
    onReviewSubmitted: () -> Unit,
    onHome: () -> Unit = {},
    onServices: () -> Unit = {},
    onProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    var rating by rememberSaveable(appointmentId) { mutableIntStateOf(0) }
    var reviewText by rememberSaveable(appointmentId) { mutableStateOf("") }
    var submitting by remember(appointmentId) { mutableStateOf(false) }
    var error by remember(appointmentId) { mutableStateOf<String?>(null) }
    var previewSubmittedNotice by remember(appointmentId) { mutableStateOf(false) }

    var displayProvider by remember(appointmentId, providerName) {
        mutableStateOf(providerName.ifBlank { "Service Provider" })
    }
    var category by remember(appointmentId) {
        mutableStateOf(if (isPreview) "Florist" else "")
    }
    var region by remember(appointmentId) {
        mutableStateOf(if (isPreview) "Eastern Province" else "")
    }
    var completedDateText by remember(appointmentId) {
        mutableStateOf(if (isPreview) "05 Oct 2026" else "")
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(appointmentId, isPreview) {
        val listener = if (isPreview || appointmentId.isBlank()) {
            null
        } else {
            FirebaseFirestore.getInstance()
                .collection("bookings")
                .document(appointmentId)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        val fetchedProvider = doc.getString("providerName").orEmpty().trim()
                        if (fetchedProvider.isNotBlank()) {
                            displayProvider = fetchedProvider
                        }
                        category = doc.getString("category").orEmpty().trim()
                        region = doc.getString("region").orEmpty().trim()
                        completedDateText = doc.extractFormattedDate(
                            "scheduledDate",
                            "completedDate",
                            "date",
                            "appointmentDate",
                            "createdAt"
                        )
                    }
                }
        }

        onDispose { listener?.remove() }
    }

    val displayedDate = completedDateText.ifBlank { "05 Oct 2026" }

    Column(
        modifier
            .fillMaxSize()
            .background(ReviewBackground)
            .safeDrawingPadding()
            .imePadding()
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
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = ReviewInk)
            }
            ReviewText("Leave a Review", 22, Modifier.align(Alignment.Center))
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ReviewBlush)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReviewText(
                            text = displayProvider,
                            size = 20,
                            modifier = Modifier.weight(1f)
                        )
                        if (category.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = category,
                                color = ReviewInk,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (region.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = region,
                            color = ReviewMuted,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Appointment completed: $displayedDate",
                        color = ReviewMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            ReviewText("How was your experience?", 18)
            Spacer(Modifier.height(8.dp))
            Text("Tap a star to rate your experience.", color = ReviewMuted, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            enabled = !submitting,
                            onClick = {
                                rating = star
                                error = null
                                previewSubmittedNotice = false
                            }
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Outlined.Star
                                else Icons.Outlined.StarOutline,
                                contentDescription = if (star == 1) "1 star" else "$star stars",
                                tint = ReviewRose,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            ReviewText("Write your review", 18)
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = {
                    reviewText = it
                    previewSubmittedNotice = false
                },
                enabled = !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                placeholder = {
                    Text("Share your experience with this provider...", color = ReviewMuted)
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ReviewRose,
                    unfocusedBorderColor = ReviewBlush,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = ReviewRose
                )
            )

            Spacer(Modifier.height(8.dp))
            Text("Your review helps other couples.", color = ReviewMuted, fontSize = 12.sp)

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = ReviewRose, fontSize = 14.sp)
            }

            if (previewSubmittedNotice) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Preview: Review submitted ($rating/5).",
                    color = Color(0xFF406747),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                enabled = !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReviewRose),
                onClick = {
                    if (submitting) return@Button
                    if (rating == 0) {
                        error = "Please select a rating."
                        return@Button
                    }

                    val selectedRating = rating
                    val comment = reviewText.trim()
                    error = null

                    if (isPreview) {
                        previewSubmittedNotice = true
                        onReviewSubmitted()
                        return@Button
                    }

                    submitting = true

                    scope.launch {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            val bookingRef = db.collection("bookings").document(appointmentId)
                            val reviewRef = db.collection("reviews").document()

                            db.runTransaction { transaction ->
                                val booking = transaction.get(bookingRef)
                                check(booking.exists() && booking.getString("weddingId") == weddingId) {
                                    "Appointment not found for this wedding."
                                }
                                check(
                                    booking.getString("status").orEmpty().trim()
                                        .equals("completed", ignoreCase = true)
                                ) {
                                    "You can review a completed appointment."
                                }
                                check(booking.getBoolean("reviewed") != true) {
                                    "This appointment already has a review."
                                }

                                val review = mapOf(
                                    "weddingId" to weddingId,
                                    "bookingId" to appointmentId,
                                    "providerName" to booking.getString("providerName")
                                        .orEmpty().ifBlank { displayProvider },
                                    "rating" to selectedRating,
                                    "review" to comment,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                transaction.set(reviewRef, review)
                                transaction.update(bookingRef, "reviewed", true)
                                Unit
                            }.await()

                            onReviewSubmitted()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = e.localizedMessage ?: "Could not submit your review."
                        } finally {
                            submitting = false
                        }
                    }
                }
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    ReviewText("Submit Review", 15, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
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
private fun ReviewText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    color: Color = ReviewInk
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = ReviewFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp
    )
}

@Preview(
    name = "Leave a Review",
    showBackground = true,
    widthDp = 402,
    heightDp = 874
)
@Composable
private fun LeaveReviewScreenPreview() {
    MaterialTheme {
        LeaveReviewScreen(
            weddingId = "preview-wedding",
            appointmentId = "preview-appointment",
            providerName = "Rose Land",
            onBack = {},
            onReviewSubmitted = {}
        )
    }
}