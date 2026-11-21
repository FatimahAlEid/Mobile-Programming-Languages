package com.example.brideline

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brideline.ui.BridelineBottomBar
import com.example.brideline.ui.BridelineNavTab
import com.example.brideline.ui.theme.BridelineTheme

/**
 * Tab options for the Bookings & Calendar screen.
 */
enum class BookingsTab {
    REQUESTS,
    CALENDAR
}

/**
 * Data model for pending customer booking requests.
 */
data class BookingRequest(
    val id: String = "",
    val customerInitials: String,
    val customerName: String,
    val serviceName: String,
    val requestedDate: String
)

/**
 * Data model for confirmed provider appointments.
 */
data class ConfirmedAppointment(
    val id: String = "",
    val monthShort: String,
    val dayOfMonth: String,
    val customerName: String,
    val serviceName: String,
    val time: String,
    val fullDateString: String = "",
    val status: String = "Confirmed"
)

/**
 * Data model for individual calendar days in the calendar grid.
 */
data class CalendarDay(
    val dayNumber: Int,
    val isCurrentMonth: Boolean = true,
    val hasAppointment: Boolean = false,
    val isSelected: Boolean = false,
    val dateString: String = ""
)

// Styling constants matching the Brideline theme
private val RosePrimary = Color(0xFFD2647D)
private val RoseSoftBg = Color(0xFFF7D6DF)
private val ScreenBackground = Color(0xFFF6F6F8)
private val TextDark = Color(0xFF332227)
private val TextGray = Color(0xFF8C8A93)
private val TabBarBg = Color(0xFFEAE6E8)
private val StatusGreenBg = Color(0xFFE8F5E9)
private val StatusGreenText = Color(0xFF2E7D32)

/**
 * Main Bookings & Calendar screen for the Brideline provider app.
 *
 * Receives all data via parameters and exposes callbacks for user actions.
 */
@Composable
fun BookingsCalendarScreen(
    selectedTab: BookingsTab = BookingsTab.REQUESTS,
    onTabSelected: (BookingsTab) -> Unit = {},
    // Requests tab parameters
    bookingRequests: List<BookingRequest> = SampleBookingRequests,
    confirmedAppointments: List<ConfirmedAppointment> = SampleConfirmedAppointments,
    onAcceptRequest: (BookingRequest) -> Unit = {},
    onDeclineRequest: (BookingRequest) -> Unit = {},
    // Calendar tab parameters
    selectedMonthYear: String = "October 2026",
    onPreviousMonthClick: () -> Unit = {},
    onNextMonthClick: () -> Unit = {},
    calendarDays: List<CalendarDay> = SampleCalendarDays,
    onDateSelected: (CalendarDay) -> Unit = {},
    selectedAppointment: ConfirmedAppointment? = SampleConfirmedAppointments.firstOrNull(),
    onAppointmentClick: (ConfirmedAppointment) -> Unit = {},
    // Bottom navigation callback
    onBottomNavClick: (BridelineNavTab) -> Unit = {},
    roleLabel: String = "Bride Line · Provider"
) {
    var currentTab by remember(selectedTab) { mutableStateOf(selectedTab) }
    var displayedCalendarDays by remember(calendarDays) { mutableStateOf(calendarDays) }
    var displayedAppointment by remember(selectedAppointment) {
        mutableStateOf(selectedAppointment)
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BridelineBottomBar(
                selectedTab = BridelineNavTab.Calendar,
                onTabSelected = onBottomNavClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Keep the screen header and tabs in place while the content below scrolls.
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = roleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = RosePrimary
                    )
                    Text(
                        text = "Bookings & Calendar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                TabSelector(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        onTabSelected(tab)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (currentTab) {
                    BookingsTab.REQUESTS -> {
                        RequestsTabContent(
                            requests = bookingRequests,
                            appointments = confirmedAppointments,
                            onAcceptRequest = onAcceptRequest,
                            onDeclineRequest = onDeclineRequest,
                            onAppointmentClick = onAppointmentClick
                        )
                    }
                    BookingsTab.CALENDAR -> {
                        CalendarTabContent(
                            selectedMonthYear = selectedMonthYear,
                            onPreviousMonthClick = onPreviousMonthClick,
                            onNextMonthClick = onNextMonthClick,
                            calendarDays = displayedCalendarDays,
                            onDateSelected = { selectedDay ->
                                displayedCalendarDays = displayedCalendarDays.map { day ->
                                    day.copy(
                                        isSelected = day.isCurrentMonth &&
                                                day.dateString == selectedDay.dateString
                                    )
                                }
                                val monthNumber = selectedDay.dateString
                                    .split("-")
                                    .getOrNull(1)
                                    ?.toIntOrNull()
                                val monthShort = monthNumber
                                    ?.takeIf { it in 1..12 }
                                    ?.let {
                                        listOf(
                                            "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                                            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
                                        )[it - 1]
                                    }
                                displayedAppointment = confirmedAppointments.firstOrNull {
                                    it.dayOfMonth.toIntOrNull() == selectedDay.dayNumber &&
                                            it.monthShort.equals(monthShort, ignoreCase = true)
                                }
                                onDateSelected(selectedDay.copy(isSelected = true))
                            },
                            selectedAppointment = displayedAppointment,
                            onAppointmentClick = onAppointmentClick
                        )
                    }
                }
            }
        }

    }
}

/**
 * Top Tab Selector for switching between Requests and Calendar views.
 */
@Composable
private fun TabSelector(
    selectedTab: BookingsTab,
    onTabSelected: (BookingsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TabBarBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabItem(
            modifier = Modifier.weight(1f),
            title = "Requests",
            isSelected = selectedTab == BookingsTab.REQUESTS,
            onClick = { onTabSelected(BookingsTab.REQUESTS) }
        )
        TabItem(
            modifier = Modifier.weight(1f),
            title = "Calendar",
            isSelected = selectedTab == BookingsTab.CALENDAR,
            onClick = { onTabSelected(BookingsTab.CALENDAR) }
        )
    }
}

@Composable
private fun TabItem(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) RosePrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextDark
        )
    }
}

/**
 * Content view for the Requests tab state.
 */
@Composable
private fun RequestsTabContent(
    requests: List<BookingRequest>,
    appointments: List<ConfirmedAppointment>,
    onAcceptRequest: (BookingRequest) -> Unit,
    onDeclineRequest: (BookingRequest) -> Unit,
    onAppointmentClick: (ConfirmedAppointment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Booking Requests Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Booking Requests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                // Request Count Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RosePrimary)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = requests.size.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (requests.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "No pending booking requests",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                requests.forEach { request ->
                    BookingRequestCard(
                        request = request,
                        onAccept = { onAcceptRequest(request) },
                        onDecline = { onDeclineRequest(request) }
                    )
                }
            }
        }

        // Confirmed Appointments Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Confirmed Appointments",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (appointments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "No confirmed appointments",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                appointments.forEach { appointment ->
                    ConfirmedAppointmentCard(
                        appointment = appointment,
                        onClick = { onAppointmentClick(appointment) }
                    )
                }
            }
        }
    }
}

/**
 * Card for displaying an individual booking request with Accept and Decline actions.
 */
@Composable
private fun BookingRequestCard(
    request: BookingRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
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

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                text = request.requestedDate,
                fontSize = 13.sp,
                color = TextGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Accept",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RosePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RosePrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Decline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Card for displaying a confirmed appointment.
 */
@Composable
private fun ConfirmedAppointmentCard(
    appointment: ConfirmedAppointment,
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
            // Date Badge Box
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
                    text = appointment.customerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = appointment.serviceName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark.copy(alpha = 0.85f)
                )
                Text(
                    text = appointment.time,
                    fontSize = 12.sp,
                    color = TextDark.copy(alpha = 0.65f)
                )
            }
        }
    }
}

/**
 * Content view for the Calendar tab state.
 */
@Composable
private fun CalendarTabContent(
    selectedMonthYear: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    calendarDays: List<CalendarDay>,
    onDateSelected: (CalendarDay) -> Unit,
    selectedAppointment: ConfirmedAppointment?,
    onAppointmentClick: (ConfirmedAppointment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Month Selector Header & Calendar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonthClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = TextDark
                        )
                    }

                    Text(
                        text = selectedMonthYear,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    IconButton(onClick = onNextMonthClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = TextDark
                        )
                    }
                }

                // Weekday Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                    }
                }

                // Calendar Days Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    calendarDays.chunked(7).forEach { rowDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowDays.forEach { day ->
                                CalendarDayCell(
                                    modifier = Modifier.weight(1f),
                                    calendarDay = day,
                                    onClick = { onDateSelected(day) }
                                )
                            }
                            // Fill remaining empty spots in row if less than 7 items
                            repeat(7 - rowDays.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Selected Appointment Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Selected appointment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (selectedAppointment != null) {
                SelectedAppointmentCard(
                    appointment = selectedAppointment,
                    onClick = { onAppointmentClick(selectedAppointment) }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "No appointment selected for this date",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Individual cell for a calendar day.
 */
@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    calendarDay: CalendarDay,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(enabled = calendarDay.isCurrentMonth, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (calendarDay.isSelected) RosePrimary else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = calendarDay.dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (calendarDay.isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    calendarDay.isSelected -> Color.White
                    !calendarDay.isCurrentMonth -> TextGray.copy(alpha = 0.35f)
                    else -> TextDark
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Appointment indicator dot
        if (calendarDay.hasAppointment) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(RosePrimary)
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

/**
 * Detailed card for displaying the currently selected appointment below the calendar.
 */
@Composable
private fun SelectedAppointmentCard(
    appointment: ConfirmedAppointment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Full Date Header & Confirmation Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.fullDateString.ifEmpty {
                        "${appointment.monthShort} ${appointment.dayOfMonth}"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RosePrimary
                )

                // Confirmation Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGreenBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusGreenText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = appointment.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreenText
                    )
                }
            }

            // Customer Name & Service
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = appointment.customerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = appointment.serviceName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray
                )
            }

            // Time Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = appointment.time,
                    fontSize = 13.sp,
                    color = TextDark
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Previews using sample data only
// -----------------------------------------------------------------------------

private val SampleBookingRequests = listOf(
    BookingRequest(
        id = "1",
        customerInitials = "ZA",
        customerName = "Zainab Al Abbas",
        serviceName = "Bridal Makeup",
        requestedDate = "Wed - 11 Mar 2027"
    ),
    BookingRequest(
        id = "2",
        customerInitials = "MA",
        customerName = "Mona Al Ahmad",
        serviceName = "Hair Styling & Veil",
        requestedDate = "Fri - 20 Nov 2026"
    )
)

private val SampleConfirmedAppointments = listOf(
    ConfirmedAppointment(
        id = "1",
        monthShort = "OCT",
        dayOfMonth = "15",
        customerName = "Sarah Johnson",
        serviceName = "Bridal Makeup & Hair Package",
        time = "02:00 PM - 05:00 PM",
        fullDateString = "Thursday, 15 October 2026",
        status = "Confirmed"
    ),
    ConfirmedAppointment(
        id = "2",
        monthShort = "NOV",
        dayOfMonth = "11",
        customerName = "Juman Bridal",
        serviceName = "Juman Bridal Makeup",
        time = "Wednesday - 05:00 PM",
        fullDateString = "Wednesday, 11 November 2026",
        status = "Confirmed"
    )
)

private val SampleCalendarDays: List<CalendarDay> = buildList {
    // Days from previous month offset
    add(CalendarDay(dayNumber = 27, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 28, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 29, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 30, isCurrentMonth = false))

    // October 2026 days (1 to 31)
    for (day in 1..31) {
        val hasAppt = (day == 5 || day == 15 || day == 22 || day == 28)
        val isSel = (day == 15)
        add(
            CalendarDay(
                dayNumber = day,
                isCurrentMonth = true,
                hasAppointment = hasAppt,
                isSelected = isSel,
                dateString = "2026-10-$day"
            )
        )
    }

    // Days into next month
    add(CalendarDay(dayNumber = 1, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 2, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 3, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 4, isCurrentMonth = false))
    add(CalendarDay(dayNumber = 5, isCurrentMonth = false))
}

@Preview(showBackground = true, name = "Bookings Screen - Requests Tab")
@Composable
fun BookingsCalendarScreenRequestsPreview() {
    BridelineTheme {
        BookingsCalendarScreen(
            selectedTab = BookingsTab.REQUESTS,
            bookingRequests = SampleBookingRequests,
            confirmedAppointments = SampleConfirmedAppointments
        )
    }
}

@Preview(showBackground = true, name = "Bookings Screen - Calendar Tab")
@Composable
fun BookingsCalendarScreenCalendarPreview() {
    BridelineTheme {
        BookingsCalendarScreen(
            selectedTab = BookingsTab.CALENDAR,
            selectedMonthYear = "October 2026",
            calendarDays = SampleCalendarDays,
            selectedAppointment = SampleConfirmedAppointments[0]
        )
    }
}
