package com.example.brideline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.ui.BridelineBottomBar
import com.example.brideline.ui.BridelineNavTab
import com.example.brideline.ui.theme.BridelineTheme

/**
 * Data model for pending customer booking requests.
 */
data class PendingRequest(
    val id: String = "",
    val customerInitials: String,
    val customerName: String,
    val serviceName: String,
    val appointmentDateString: String
)

/**
 * Data model for upcoming provider appointments.
 */
data class UpcomingAppointment(
    val id: String = "",
    val monthShort: String,
    val dayOfMonth: String,
    val titleOrCustomerService: String,
    val dateTimeString: String
)

/**
 * Quick access item destinations.
 */
enum class QuickAccessType {
    SERVICES,
    CALENDAR,
    MESSAGES
}

// Color palette matching the Brideline design.
private val RosePrimary = Color(0xFFD2647D)
private val RoseSoftBg = Color(0xFFF7D6DF)
private val ScreenBackground = Color(0xFFF6F6F8)
private val TextDark = Color(0xFF332227)
private val TextGray = Color(0xFF8C8A93)
private val ButtonGreyBg = Color(0xFFE5E0E2)

@Composable
fun ProviderDashboardScreen(
    providerName: String,
    todayBookingCount: Int = 0,
    todayDateString: String = "",
    pendingRequests: List<PendingRequest> = emptyList(),
    upcomingAppointments: List<UpcomingAppointment> = emptyList(),
    roleLabel: String = "Bride Line · Provider",
    onQuickAccessClick: (QuickAccessType) -> Unit = {},
    onViewAllRequestsClick: () -> Unit = {},
    onReviewRequestClick: (PendingRequest) -> Unit = {},
    onAcceptRequestClick: (PendingRequest) -> Unit = {},
    onDeclineRequestClick: (PendingRequest) -> Unit = {},
    onAppointmentClick: (UpcomingAppointment) -> Unit = {},
    onBottomNavClick: (String) -> Unit = {}
) {
    var showCalendarScreen by remember { mutableStateOf(false) }

    if (showCalendarScreen) {
        BookingsCalendarScreen(
            onBottomNavClick = { tab ->
                when (tab) {
                    BridelineNavTab.Home -> showCalendarScreen = false
                    BridelineNavTab.Calendar -> Unit
                    else -> onBottomNavClick(tab.name)
                }
            }
        )
    } else {
        Scaffold(
            containerColor = ScreenBackground,
            bottomBar = {
                BridelineBottomBar(
                    selectedTab = BridelineNavTab.Home,
                    onTabSelected = { tab ->
                        if (tab == BridelineNavTab.Calendar) {
                            showCalendarScreen = true
                        } else {
                            onBottomNavClick(tab.name)
                        }
                    }
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
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = roleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = RosePrimary
                    )
                    Text(
                        text = "Provider Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Welcome back, $providerName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = RosePrimary
                    )
                }

                TodaysOverviewCard(
                    bookingCount = todayBookingCount,
                    dateString = todayDateString
                )

                QuickAccessSection(
                    onQuickAccessClick = { destination ->
                        if (destination == QuickAccessType.CALENDAR) {
                            showCalendarScreen = true
                        } else {
                            onQuickAccessClick(destination)
                        }
                    }
                )

                PendingRequestsSection(
                    requests = pendingRequests,
                    onViewAllClick = onViewAllRequestsClick,
                    onReviewClick = onReviewRequestClick,
                    onAcceptClick = onAcceptRequestClick,
                    onDeclineClick = onDeclineRequestClick
                )

                UpcomingAppointmentsSection(
                    appointments = upcomingAppointments,
                    onAppointmentClick = onAppointmentClick
                )
            }
        }
    }
}

@Composable
private fun TodaysOverviewCard(
    bookingCount: Int,
    dateString: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RosePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Today’s Overview",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "$bookingCount Bookings Today",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = dateString,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun QuickAccessSection(
    onQuickAccessClick: (QuickAccessType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Access",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = "Services",
                icon = Icons.Outlined.Assignment,
                onClick = { onQuickAccessClick(QuickAccessType.SERVICES) }
            )
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = "Calendar",
                icon = Icons.Outlined.CalendarMonth,
                onClick = { onQuickAccessClick(QuickAccessType.CALENDAR) }
            )
            QuickAccessCard(
                modifier = Modifier.weight(1f),
                title = "Messages",
                icon = Icons.Outlined.Email,
                onClick = { onQuickAccessClick(QuickAccessType.MESSAGES) }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoseSoftBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextDark,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }
    }
}

@Composable
private fun PendingRequestsSection(
    requests: List<PendingRequest>,
    onViewAllClick: () -> Unit,
    onReviewClick: (PendingRequest) -> Unit,
    onAcceptClick: (PendingRequest) -> Unit,
    onDeclineClick: (PendingRequest) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pending Requests",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "View all",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = RosePrimary,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        requests.forEach { request ->
            PendingRequestCard(
                request = request,
                onReviewClick = { onReviewClick(request) },
                onAcceptClick = { onAcceptClick(request) },
                onDeclineClick = { onDeclineClick(request) }
            )
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: PendingRequest,
    onReviewClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RoseSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.customerInitials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Column {
                    Text(
                        text = request.customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = request.serviceName,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            Text(
                text = request.appointmentDateString,
                fontSize = 13.sp,
                color = TextGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReviewClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonGreyBg,
                        contentColor = TextDark
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text(
                        text = "Review Request",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onAcceptClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Accept",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                OutlinedButton(
                    onClick = onDeclineClick,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RosePrimary
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 10.dp
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Decline",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingAppointmentsSection(
    appointments: List<UpcomingAppointment>,
    onAppointmentClick: (UpcomingAppointment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Upcoming Appointments",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        appointments.forEach { appointment ->
            UpcomingAppointmentCard(
                appointment = appointment,
                onClick = { onAppointmentClick(appointment) }
            )
        }
    }
}

@Composable
private fun UpcomingAppointmentCard(
    appointment: UpcomingAppointment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoseSoftBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = appointment.monthShort,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosePrimary
                    )
                    Text(
                        text = appointment.dayOfMonth,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosePrimary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = appointment.titleOrCustomerService,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = appointment.dateTimeString,
                    fontSize = 12.sp,
                    color = TextDark.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Provider Dashboard Preview")
@Composable
fun ProviderDashboardScreenPreview() {
    val samplePendingRequests = listOf(
        PendingRequest(
            id = "1",
            customerInitials = "ZA",
            customerName = "Zainab Al Abbas",
            serviceName = "Bridal Makeup",
            appointmentDateString = "Wed - 11 Mar 2027"
        )
    )

    val sampleUpcomingAppointments = listOf(
        UpcomingAppointment(
            id = "1",
            monthShort = "NOV",
            dayOfMonth = "11",
            titleOrCustomerService = "Juman Bridal Makeup",
            dateTimeString = "Wednesday - 05:00 PM"
        )
    )

    BridelineTheme {
        ProviderDashboardScreen(
            providerName = "Wejdan",
            todayBookingCount = 3,
            todayDateString = "Friday, 2 October 2026",
            pendingRequests = samplePendingRequests,
            upcomingAppointments = sampleUpcomingAppointments,
            roleLabel = "Bride Line · Provider"
        )
    }
}