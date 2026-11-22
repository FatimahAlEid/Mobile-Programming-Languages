package com.example.brideline

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pride.ui.components.BridelineBottomNavBar
import com.example.pride.ui.components.NavTab

// Self-contained Style Guide Colors
private val PrimaryPink = Color(0xFFD2647D)
private val AccentPink = Color(0xFFEDBEC8)
private val BackgroundSoft = Color(0xFFFAF6F7)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1F1F1F)
private val TextSecondary = Color(0xFF555555)

@Composable
fun BrideHomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryPink,
            secondary = AccentPink,
            background = BackgroundSoft,
            surface = CardWhite,
            onPrimary = Color.White,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        content = content
    )
}

@Composable
fun MakeupArtistsCanvasIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        val color = Color(0xFF2C2C2C)
        val style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // 1. Compact Powder (Base & Inner Powder Circle)
        val compactCenter = Offset(size.width * 0.30f, size.height * 0.65f)
        val compactRadius = size.width * 0.20f
        drawCircle(color = color, center = compactCenter, radius = compactRadius, style = style)
        drawCircle(color = color, center = compactCenter, radius = compactRadius * 0.55f, style = style)

        // 2. Open Mirror Lid (Tilted Circle top left of compact)
        val mirrorCenter = Offset(size.width * 0.24f, size.height * 0.32f)
        val mirrorRadius = size.width * 0.16f
        drawCircle(color = color, center = mirrorCenter, radius = mirrorRadius, style = style)
        // Hinge line
        drawLine(
            color = color,
            start = Offset(mirrorCenter.x + mirrorRadius * 0.4f, mirrorCenter.y + mirrorRadius * 0.7f),
            end = Offset(compactCenter.x - compactRadius * 0.4f, compactCenter.y - compactRadius * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // 3. Lipstick (Right side)
        val tubeLeft = size.width * 0.64f
        val tubeRight = size.width * 0.84f
        val tubeTop = size.height * 0.58f
        val tubeBottom = size.height * 0.86f
        drawRoundRect(
            color = color,
            topLeft = Offset(tubeLeft, tubeTop),
            size = Size(tubeRight - tubeLeft, tubeBottom - tubeTop),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = style
        )

        // Middle metal collar
        val collarTop = size.height * 0.42f
        drawRoundRect(
            color = color,
            topLeft = Offset(tubeLeft + 1.5.dp.toPx(), collarTop),
            size = Size((tubeRight - tubeLeft) - 3.dp.toPx(), tubeTop - collarTop),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
            style = style
        )

        // Slanted lipstick bullet
        val bulletPath = Path().apply {
            moveTo(tubeLeft + 2.5.dp.toPx(), collarTop)
            lineTo(tubeLeft + 2.5.dp.toPx(), size.height * 0.28f)
            lineTo(tubeRight - 2.5.dp.toPx(), size.height * 0.16f)
            lineTo(tubeRight - 2.5.dp.toPx(), collarTop)
            close()
        }
        drawPath(path = bulletPath, color = color, style = style)
    }
}

@Composable
fun HairStylistsCanvasIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(36.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        val color = Color(0xFF2C2C2C)
        val style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        val w = size.width
        val h = size.height

        // 1. Chin Contour
        val chinPath = Path().apply {
            moveTo(w * 0.35f, h * 0.44f)
            cubicTo(
                w * 0.35f, h * 0.70f,
                w * 0.65f, h * 0.70f,
                w * 0.65f, h * 0.44f
            )
        }
        drawPath(path = chinPath, color = color, style = style)

        // 2. Left Hair Strand
        val leftHair = Path().apply {
            moveTo(w * 0.50f, h * 0.24f)
            cubicTo(
                w * 0.32f, h * 0.25f,
                w * 0.22f, h * 0.42f,
                w * 0.22f, h * 0.62f
            )
            cubicTo(
                w * 0.22f, h * 0.80f,
                w * 0.28f, h * 0.86f,
                w * 0.24f, h * 0.88f
            )
        }
        drawPath(path = leftHair, color = color, style = style)

        // 3. Right Hair Strand
        val rightHair = Path().apply {
            moveTo(w * 0.50f, h * 0.24f)
            cubicTo(
                w * 0.68f, h * 0.25f,
                w * 0.78f, h * 0.42f,
                w * 0.78f, h * 0.62f
            )
            cubicTo(
                w * 0.78f, h * 0.80f,
                w * 0.70f, h * 0.86f,
                w * 0.76f, h * 0.88f
            )
        }
        drawPath(path = rightHair, color = color, style = style)

        // 4. Hair Outer Top Dome
        val hairDome = Path().apply {
            moveTo(w * 0.24f, h * 0.52f)
            cubicTo(
                w * 0.20f, h * 0.26f,
                w * 0.36f, h * 0.14f,
                w * 0.50f, h * 0.14f
            )
            cubicTo(
                w * 0.64f, h * 0.14f,
                w * 0.80f, h * 0.26f,
                w * 0.76f, h * 0.52f
            )
        }
        drawPath(path = hairDome, color = color, style = style)

        // 5. Flower on Hair (Top Left)
        val flowerCenter = Offset(w * 0.28f, h * 0.30f)
        val flowerR = 3.5.dp.toPx()
        drawCircle(color = color, center = flowerCenter, radius = flowerR, style = style)

        // 6. Sparkles (Top Right & Bottom Right)
        val sp1X = w * 0.82f
        val sp1Y = h * 0.28f
        val s1 = 3.5.dp.toPx()
        drawLine(color, Offset(sp1X - s1, sp1Y), Offset(sp1X + s1, sp1Y), strokeWidth, cap = StrokeCap.Round)
        drawLine(color, Offset(sp1X, sp1Y - s1), Offset(sp1X, sp1Y + s1), strokeWidth, cap = StrokeCap.Round)

        val sp2X = w * 0.84f
        val sp2Y = h * 0.64f
        val s2 = 3.5.dp.toPx()
        drawLine(color, Offset(sp2X - s2, sp2Y), Offset(sp2X + s2, sp2Y), strokeWidth, cap = StrokeCap.Round)
        drawLine(color, Offset(sp2X, sp2Y - s2), Offset(sp2X, sp2Y + s2), strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
fun BrideHomeScreen(
    onNavigateToServices: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onNavigateToInvoices: () -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToAppointmentDetails: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    BrideHomeTheme {
        Scaffold(
            containerColor = BackgroundSoft,
            bottomBar = {
                BridelineBottomNavBar(
                    selectedTab = NavTab.HOME,
                    onNavigateToHome = {},
                    onNavigateToServices = onNavigateToServices,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Bride Line . Bride",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryPink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Plan your wedding",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Welcome back, Zainab",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPink
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "Options",
                        tint = TextPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Hero Countdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPink)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Your wedding day",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "11 Mar 2027",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "158 days left",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Quick Access Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Access",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessCard(
                            title = "Services",
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            onClick = onNavigateToServices,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Bookings",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = onNavigateToBookings,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessCard(
                            title = "Invoices",
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            onClick = onNavigateToInvoices,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Popular Categories Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Categories",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "See all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink,
                            modifier = Modifier.clickable { onNavigateToServices() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryCard(
                            title = "Makeup Artists",
                            icon = { MakeupArtistsCanvasIcon() },
                            onClick = { onNavigateToCategory("browse_makeup") },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            title = "Hair Stylists",
                            icon = { HairStylistsCanvasIcon() },
                            onClick = { onNavigateToCategory("browse_hair") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Upcoming Appointments Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Appointments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "See all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink,
                            modifier = Modifier.clickable { onNavigateToBookings() }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAppointmentDetails() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentPink)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CardWhite.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Zahra Alsaffar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Hair stylist - Zahra",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "11 Mar 2027 - 10:00 AM",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View appointment details",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AccentPink)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextPrimary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AccentPink)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BrideHomeScreenPreview() {
    BrideHomeScreen()
}
