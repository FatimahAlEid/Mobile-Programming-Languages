package com.example.brideline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.ui.BridelineServicesBottomBar
import com.example.brideline.ui.ServicesNavTab
import com.example.brideline.ui.theme.BridelineTheme

// Styling colors matching the soft, elegant Brideline aesthetic
private val PaleLavenderBg = Color(0xFFF3EFF4)
private val DustyRosePrimary = Color(0xFFD2647D)
private val SoftBlushTagBg = Color(0xFFFCEBF0)
private val TextDark = Color(0xFF332227)
private val TextGray = Color(0xFF756F7A)
private val StarGold = Color(0xFFFFC107)

/**
 * Mobile Provider Details Screen for Brideline.
 *
 * Displays detailed information about a vendor/provider including photos, starting price,
 * rating, location, description, services tags, and booking action buttons.
 */
@Composable
fun ProviderDetailsScreen(
    providerName: String = "Rose Land",
    location: String = "Eastern Province",
    startingPrice: String = "Starting from SAR 200",
    rating: Double = 4.9,
    aboutText: String = "Elegant wedding flowers and custom floral arrangements designed for your special day.",
    services: List<String> = listOf("Bouquets", "Decor", "Centerpieces"),
    // Kept for compatibility with existing callers; this screen currently shows a static placeholder.
    imageUrl: String = "",
    onBackClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onBottomNavClick: (ServicesNavTab) -> Unit = {}
) {
    // Outer phone frame container with thin rose outline
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaleLavenderBg)
            .border(BorderStroke(1.5.dp, DustyRosePrimary.copy(alpha = 0.6f)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaleLavenderBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onBookNowClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DustyRosePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text(
                                text = "Book Now",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = onMessageClick,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, DustyRosePrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DustyRosePrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                        ) {
                            Text(
                                text = "Message",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    BridelineServicesBottomBar(
                        selectedTab = ServicesNavTab.Services,
                        onTabSelected = onBottomNavClick
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Bar with Back Arrow and Centered Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }

                    Text(
                        text = "Provider Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    // Spacer to balance the back arrow button for true centering
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Large Provider Photo Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(SoftBlushTagBg),
                        contentAlignment = Alignment.Center
                    ) {
                        ProviderImagePlaceholder()
                    }
                }

                // Rose-Colored Provider Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DustyRosePrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = providerName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Rating Badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = StarGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = rating.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = location,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Price
                        Text(
                            text = startingPrice,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // About Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "About",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = aboutText,
                            fontSize = 14.sp,
                            color = TextGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Services Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Services",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        services.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SoftBlushTagBg)
                                    .border(
                                        BorderStroke(1.dp, DustyRosePrimary.copy(alpha = 0.35f)),
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DustyRosePrimary
                                )
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
private fun ProviderImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBlushTagBg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(42.dp)) {
            val iconColor = TextGray
            val lineWidth = 1.8.dp.toPx()
            val inset = lineWidth

            drawRoundRect(
                color = iconColor,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = lineWidth)
            )
            drawCircle(
                color = iconColor,
                radius = 2.5.dp.toPx(),
                center = Offset(size.width * 0.68f, size.height * 0.34f)
            )
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.18f, size.height * 0.78f),
                end = Offset(size.width * 0.43f, size.height * 0.52f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.43f, size.height * 0.52f),
                end = Offset(size.width * 0.58f, size.height * 0.66f),
                strokeWidth = lineWidth
            )
            drawLine(
                color = iconColor,
                start = Offset(size.width * 0.58f, size.height * 0.66f),
                end = Offset(size.width * 0.76f, size.height * 0.48f),
                strokeWidth = lineWidth
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Preview
// -----------------------------------------------------------------------------

@Preview(showBackground = true, name = "Provider Details Screen Preview", widthDp = 402, heightDp = 850)
@Composable
fun ProviderDetailsScreenPreview() {
    BridelineTheme {
        ProviderDetailsScreen()
    }
}
