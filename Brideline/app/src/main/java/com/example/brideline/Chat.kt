package com.example.brideline

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.brideline.ui.BridelineServicesBottomBar
import com.example.brideline.ui.ServicesNavTab
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private val ChatRose = Color(0xFFD2647D)
private val ChatBlush = Color(0xFFEDBEC8)
private val ChatInk = Color(0xFF45353A)
private val ChatMuted = Color(0xFF756D72)
private val ChatBackground = Color(0xFFECEDF3)

private val ChatFont = FontFamily(
    Font(R.font.archivo_semibold, FontWeight.SemiBold)
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long? = null,
    val type: String = "text",
    val attachmentUrl: String? = null,
    val attachmentName: String? = null
)

private data class PendingAttachment(
    val type: String,
    val name: String,
    val url: String?
)

@Composable
fun ChatScreen(
    appointmentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
    onServices: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    key(appointmentId, user?.uid) {
        var providerName by remember { mutableStateOf("Service Provider") }
        var providerImageUrl by remember { mutableStateOf<String?>(null) }
        var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
        var loading by remember { mutableStateOf(true) }
        var bookingError by remember { mutableStateOf<String?>(null) }
        var chatError by remember { mutableStateOf<String?>(null) }

        DisposableEffect(db, appointmentId) {
            if (appointmentId.isBlank()) {
                loading = false
                chatError = "Appointment not found."
                onDispose {}
            } else {
                val bookingListener = db.collection("bookings")
                    .document(appointmentId)
                    .addSnapshotListener { document, failure ->
                        if (failure != null) {
                            bookingError = failure.localizedMessage
                                ?: "Could not load provider."
                        } else {
                            bookingError = null
                            providerName = document?.getString("providerName")
                                .orEmpty()
                                .ifBlank { "Service Provider" }

                            providerImageUrl = document?.getString("providerImageUrl")
                                ?: document?.getString("profileImageUrl")
                                        ?: document?.getString("imageUrl")
                        }
                    }

                val messagesListener = db.collection("chats")
                    .document(appointmentId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, failure ->
                        loading = false

                        if (failure != null) {
                            chatError = failure.localizedMessage
                                ?: "Could not load messages."
                        } else {
                            chatError = null
                            messages = snapshot?.documents.orEmpty().map { doc ->
                                val time = when (val value = doc.get("timestamp")) {
                                    is Number -> value.toLong()
                                    is Timestamp -> value.toDate().time
                                    else -> null
                                }

                                ChatMessage(
                                    id = doc.id,
                                    senderId = doc.getString("senderId").orEmpty(),
                                    text = doc.getString("text").orEmpty(),
                                    timestamp = time,
                                    type = doc.getString("type") ?: "text",
                                    attachmentUrl = doc.getString("attachmentUrl"),
                                    attachmentName = doc.getString("attachmentName")
                                )
                            }
                        }
                    }

                onDispose {
                    bookingListener.remove()
                    messagesListener.remove()
                }
            }
        }

        ChatScreenContent(
            providerName = providerName,
            providerImageUrl = providerImageUrl,
            currentUserPhotoUrl = user?.photoUrl?.toString(),
            currentUserId = user?.uid,
            messages = messages,
            loading = loading,
            error = chatError ?: bookingError,
            onBack = onBack,
            onHome = onHome,
            onServices = onServices,
            onProfile = onProfile,
            modifier = modifier,
            onSend = { text, attachment ->
                val senderId = auth.currentUser?.uid
                    ?: error("Please sign in to send messages.")

                require(appointmentId.isNotBlank()) { "Appointment not found." }

                val messageData = mapOf(
                    "senderId" to senderId,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis(),
                    "type" to (attachment?.type ?: "text"),
                    "attachmentUrl" to attachment?.url,
                    "attachmentName" to attachment?.name
                )

                db.collection("chats")
                    .document(appointmentId)
                    .collection("messages")
                    .add(messageData)
                    .await()

                Unit
            }
        )
    }
}

@Composable
private fun ChatScreenContent(
    providerName: String,
    providerImageUrl: String?,
    currentUserPhotoUrl: String?,
    currentUserId: String?,
    messages: List<ChatMessage>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onServices: () -> Unit,
    onProfile: () -> Unit,
    onSend: suspend (String, PendingAttachment?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var draft by rememberSaveable { mutableStateOf("") }
    var attachmentType by rememberSaveable { mutableStateOf<String?>(null) }
    var attachmentName by rememberSaveable { mutableStateOf<String?>(null) }
    var attachmentUrl by rememberSaveable { mutableStateOf<String?>(null) }

    var showMap by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    fun selectAttachment(attachment: PendingAttachment?) {
        attachmentType = attachment?.type
        attachmentName = attachment?.name
        attachmentUrl = attachment?.url
        actionError = null
    }

    fun selectDocument(uri: Uri, type: String, fallbackName: String) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // The picker may provide access for the current session only.
        }

        selectAttachment(
            PendingAttachment(
                type = type,
                name = attachmentDisplayName(context, uri, fallbackName),
                url = uri.toString()
            )
        )
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectDocument(uri, "image", "Selected photo")
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) selectDocument(uri, "file", "Selected file")
    }

    val pendingAttachment = attachmentType?.let { type ->
        PendingAttachment(type, attachmentName.orEmpty(), attachmentUrl)
    }

    fun sendMessage() {
        val text = draft.trim()
        val attachment = pendingAttachment
        if (sending || (text.isEmpty() && attachment == null)) return

        sending = true
        actionError = null

        scope.launch {
            try {
                onSend(text, attachment)
                draft = ""
                selectAttachment(null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                actionError = e.localizedMessage ?: "Could not send message."
            } finally {
                sending = false
            }
        }
    }

    fun openLocation(message: ChatMessage) {
        val place = message.attachmentName
            ?.takeIf { it.isNotBlank() }
            ?: message.text.removePrefix("Location:").trim()

        if (place.isBlank()) return

        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri(place)))
        } catch (_: ActivityNotFoundException) {
            actionError = "No app is available to open the map."
        }
    }

    LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    if (showMap) {
        ChatMapPickerDialog(
            onDismiss = { showMap = false },
            onSelected = { latitude, longitude ->
                val coordinates = String.format(
                    Locale.US, "%.6f, %.6f", latitude, longitude
                )

                selectAttachment(
                    PendingAttachment(
                        type = "location",
                        name = coordinates,
                        url = mapsUri(coordinates).toString()
                    )
                )
                showMap = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ChatBackground)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = ChatInk
                )
            }

            ChatAvatar(providerImageUrl, providerName, 42.dp)
            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = providerName,
                    fontFamily = ChatFont,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatInk,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Chat with service provider",
                    color = ChatMuted,
                    fontSize = 12.sp
                )
            }
        }

        HorizontalDivider(color = ChatBlush)

        when {
            loading -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ChatRose)
            }

            messages.isEmpty() -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (error == null) {
                    Text(
                        "No messages yet. Start the conversation!",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = ChatMuted,
                        fontSize = 13.sp
                    )
                }
            }

            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { _, message -> message.id }
                ) { index, message ->
                    val date = formatMessageDate(message.timestamp)
                    val previousDate = messages.getOrNull(index - 1)
                        ?.let { formatMessageDate(it.timestamp) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (date.isNotEmpty() && date != previousDate) {
                            Text(
                                text = date,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                color = ChatMuted,
                                fontSize = 11.sp
                            )
                        }

                        val isMine = message.senderId == currentUserId

                        ChatMessageBubble(
                            message = message,
                            isMine = isMine,
                            avatarUrl = if (isMine) currentUserPhotoUrl else providerImageUrl,
                            avatarName = if (isMine) "You" else providerName,
                            onOpenLocation = { openLocation(message) },
                            onOpenFile = {
                                if (!openSelectedFile(context, message.attachmentUrl)) {
                                    actionError = "This file cannot be opened on this device."
                                }
                            }
                        )
                    }
                }
            }
        }

        val visibleError = actionError ?: error
        if (visibleError != null) {
            Text(
                text = visibleError,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = ChatRose,
                fontSize = 12.sp
            )
        }

        if (pendingAttachment != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (pendingAttachment.type) {
                    "image" -> Icons.Outlined.Image
                    "file" -> Icons.Outlined.Description
                    else -> Icons.Outlined.LocationOn
                }

                Icon(icon, null, Modifier.size(20.dp), tint = ChatRose)
                Spacer(Modifier.width(8.dp))

                Text(
                    text = pendingAttachment.name,
                    modifier = Modifier.weight(1f),
                    color = ChatInk,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    enabled = !sending,
                    onClick = { selectAttachment(null) }
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Remove attachment",
                        tint = ChatMuted
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    enabled = !sending,
                    onClick = { showMenu = true },
                    modifier = Modifier.size(44.dp).background(ChatRose, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "Add attachment",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = Color.White
                ) {
                    DropdownMenuItem(
                        text = { Text("Photo", color = ChatInk) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Image, null, tint = ChatRose)
                        },
                        onClick = {
                            showMenu = false
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("File", color = ChatInk) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Description, null, tint = ChatRose)
                        },
                        onClick = {
                            showMenu = false
                            filePicker.launch(arrayOf("*/*"))
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Location", color = ChatInk) },
                        leadingIcon = {
                            Icon(Icons.Outlined.LocationOn, null, tint = ChatRose)
                        },
                        onClick = {
                            showMenu = false
                            showMap = true
                        }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                enabled = !sending,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = ChatMuted) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatRose,
                    unfocusedBorderColor = ChatBlush,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = ChatRose
                )
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                enabled = !sending && (draft.isNotBlank() || pendingAttachment != null),
                onClick = { sendMessage() },
                modifier = Modifier.size(48.dp).background(ChatRose, CircleShape)
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Outlined.Send, contentDescription = "Send", tint = Color.White)
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
private fun ChatMessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    avatarUrl: String?,
    avatarName: String,
    onOpenLocation: () -> Unit,
    onOpenFile: () -> Unit
) {
    val ink = if (isMine) Color.White else ChatInk
    val isPhoto = message.type == "image"

    val oldCaption = when (message.type) {
        "image" -> "Photo: ${message.attachmentName}"
        "file" -> "File: ${message.attachmentName}"
        "location" -> "Location: ${message.attachmentName}"
        else -> null
    }

    val caption = message.text.takeIf {
        it.isNotBlank() && it != oldCaption
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            ChatAvatar(avatarUrl, avatarName, 34.dp)
            Spacer(Modifier.width(7.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isMine) ChatRose else Color.White)
                    .padding(if (isPhoto) 2.dp else 12.dp)
                    .then(if (isPhoto) Modifier.width(200.dp) else Modifier)
            ) {
                when (message.type) {
                    "image" -> {
                        var failed by remember(message.attachmentUrl) {
                            mutableStateOf(false)
                        }

                        if (!message.attachmentUrl.isNullOrBlank() && !failed) {
                            AsyncImage(
                                model = message.attachmentUrl,
                                contentDescription = message.attachmentName ?: "Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop,
                                onError = { failed = true }
                            )
                        } else {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Image, null, tint = ink)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    message.attachmentName ?: "Photo unavailable",
                                    fontSize = 14.sp,
                                    color = ink
                                )
                            }
                        }
                    }

                    "file" -> Row(
                        modifier = Modifier.clickable(onClick = onOpenFile),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            null,
                            Modifier.size(20.dp),
                            tint = ink
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(message.attachmentName ?: "Open file", color = ink, fontSize = 14.sp)
                    }

                    "location" -> Row(
                        modifier = Modifier.clickable(onClick = onOpenLocation),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            null,
                            Modifier.size(20.dp),
                            tint = ink
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "View location",
                                color = ink,
                                fontFamily = ChatFont,
                                fontSize = 14.sp
                            )
                            Text(
                                message.attachmentName ?: message.text,
                                color = ink,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (caption != null) {
                    if (message.type != "text") Spacer(Modifier.height(4.dp))
                    Text(
                        text = caption,
                        modifier = if (isPhoto) {
                            Modifier.padding(start = 8.dp, end = 8.dp, bottom = 6.dp)
                        } else {
                            Modifier
                        },
                        color = ink,
                        fontSize = 15.sp
                    )
                }
            }

            if (message.timestamp != null) {
                Text(
                    text = formatMessageTime(message.timestamp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                    color = ChatMuted,
                    fontSize = 10.sp
                )
            }
        }

        if (isMine) {
            Spacer(Modifier.width(7.dp))
            ChatAvatar(avatarUrl, avatarName, 34.dp)
        }
    }
}

@Composable
private fun ChatAvatar(imageUrl: String?, name: String, size: Dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(ChatBlush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = ChatInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$name profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ChatMapPickerDialog(
    onDismiss: () -> Unit,
    onSelected: (Double, Double) -> Unit
) {
    val context = LocalContext.current

    var selectedLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

    val mapView = remember(context) {
        val cacheDirectory = File(context.cacheDir, "chat-map").apply { mkdirs() }

        Configuration.getInstance().apply {
            userAgentValue = "Brideline (${context.packageName})"
            osmdroidBasePath = cacheDirectory
            osmdroidTileCache = File(cacheDirectory, "tiles").apply { mkdirs() }
        }

        val streetTiles = XYTileSource(
            "Mapnik",
            0,
            19,
            256,
            ".png",
            arrayOf("https://tile.openstreetmap.org/")
        )

        MapView(context).apply {
            setTileSource(streetTiles)
            setMultiTouchControls(true)
            controller.setZoom(12.0)

            val latitude = selectedLatitude
            val longitude = selectedLongitude
            val initialPoint = if (latitude != null && longitude != null) {
                GeoPoint(latitude, longitude)
            } else {
                GeoPoint(26.4207, 50.0888)
            }

            controller.setCenter(initialPoint)

            val map = this
            val marker = Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setInfoWindow(null)
            }

            fun selectPoint(point: GeoPoint): Boolean {
                selectedLatitude = point.latitude
                selectedLongitude = point.longitude
                marker.position = point

                if (!map.overlays.contains(marker)) {
                    map.overlays.add(marker)
                }

                map.invalidate()
                return true
            }

            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                        return selectPoint(point)
                    }

                    override fun longPressHelper(point: GeoPoint): Boolean {
                        return selectPoint(point)
                    }
                })
            )

            if (latitude != null && longitude != null) {
                marker.position = initialPoint
                overlays.add(marker)
            }
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    val latitude = selectedLatitude
    val longitude = selectedLongitude

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose a location", fontFamily = ChatFont, color = ChatInk)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Tap the map to place a pin.",
                    color = ChatMuted,
                    fontSize = 13.sp
                )

                AndroidView(
                    factory = { mapView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Text(
                    text = "© OpenStreetMap contributors",
                    color = ChatMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        val uri = Uri.parse("https://www.openstreetmap.org/copyright")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: ActivityNotFoundException) {
                            // The attribution remains visible under the map.
                        }
                    }
                )

                if (latitude != null && longitude != null) {
                    Text(
                        text = String.format(
                            Locale.US, "Selected: %.6f, %.6f", latitude, longitude
                        ),
                        color = ChatInk,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = latitude != null && longitude != null,
                onClick = {
                    if (latitude != null && longitude != null) {
                        onSelected(latitude, longitude)
                    }
                }
            ) {
                Text("Attach location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ChatInk)
            }
        }
    )
}

private fun mapsUri(place: String): Uri {
    return Uri.parse("https://www.google.com/maps/search/")
        .buildUpon()
        .appendQueryParameter("api", "1")
        .appendQueryParameter("query", place)
        .build()
}

private fun attachmentDisplayName(
    context: Context,
    uri: Uri,
    fallback: String
): String {
    val name = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull()

    return name.orEmpty().ifBlank { fallback }
}

private fun openSelectedFile(context: Context, value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    val uri = Uri.parse(value)

    return try {
        val intent = when (uri.scheme) {
            "content" -> Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            "https" -> Intent(Intent.ACTION_VIEW, uri)
            else -> return false
        }

        context.startActivity(Intent.createChooser(intent, "Open file"))
        true
    } catch (_: Exception) {
        false
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun formatMessageDate(timestamp: Long?): String {
    if (timestamp == null) return ""

    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun sameDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
                first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    return when {
        sameDay(messageDate, today) -> "Today"
        sameDay(messageDate, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            .format(Date(timestamp))
    }
}

@Preview(
    name = "Brideline Chat",
    showBackground = true,
    showSystemUi = true,
    widthDp = 402,
    heightDp = 874
)
@Composable
private fun ChatScreenPreview() {
    val now = remember { System.currentTimeMillis() }

    var previewMessages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "1",
                    senderId = "provider",
                    text = "Hello! Thank you for contacting Rose Studio.",
                    timestamp = now - 86_400_000L
                ),
                ChatMessage(
                    id = "2",
                    senderId = "customer",
                    text = "Hi! I would like to ask about my appointment.",
                    timestamp = now - 3_600_000L
                ),
                ChatMessage(
                    id = "3",
                    senderId = "provider",
                    text = "Of course! How can I help you?",
                    timestamp = now - 1_800_000L
                ),
                ChatMessage(
                    id = "4",
                    senderId = "customer",
                    text = "Can you confirm the appointment time?",
                    timestamp = now - 600_000L
                ),
                ChatMessage(
                    id = "5",
                    senderId = "provider",
                    text = "Your appointment is confirmed. We look forward to seeing you!",
                    timestamp = now
                )
            )
        )
    }

    MaterialTheme {
        ChatScreenContent(
            providerName = "Rose Studio",
            providerImageUrl = null,
            currentUserPhotoUrl = null,
            currentUserId = "customer",
            messages = previewMessages,
            loading = false,
            error = null,
            onBack = {},
            onHome = {},
            onServices = {},
            onProfile = {},
            onSend = { text, attachment ->
                previewMessages = previewMessages + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = "customer",
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    type = attachment?.type ?: "text",
                    attachmentName = attachment?.name,
                    attachmentUrl = attachment?.url
                )
            }
        )
    }
}