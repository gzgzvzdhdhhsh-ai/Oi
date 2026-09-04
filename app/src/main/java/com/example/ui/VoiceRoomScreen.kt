package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.model.VoiceSeat
import com.example.ui.components.GiftBottomSheet
import com.example.ui.components.NetworkDialog
import com.example.ui.components.SoundEffectsSheet
import com.example.ui.components.VoiceSeatView
import com.example.ui.theme.SleekAmber400
import com.example.ui.theme.SleekGreen500
import com.example.ui.theme.SleekIndigo400
import com.example.ui.theme.SleekIndigo500
import com.example.ui.theme.SleekIndigo600
import com.example.ui.theme.SleekPink500
import com.example.ui.theme.SleekPurple500
import com.example.ui.theme.SleekRed500
import com.example.ui.theme.SleekSlate100
import com.example.ui.theme.SleekSlate300
import com.example.ui.theme.SleekSlate400
import com.example.ui.theme.SleekSlate500
import com.example.ui.theme.SleekSlate600
import com.example.ui.theme.SleekSlate700
import com.example.ui.theme.SleekSlate800
import com.example.ui.theme.SleekSlate850
import com.example.ui.theme.SleekSlate900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VoiceRoomUiState
import com.example.viewmodel.VoiceRoomViewModel

@Composable
fun VoiceRoomScreen(
    viewModel: VoiceRoomViewModel,
    uiState: VoiceRoomUiState,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onLeaveRoom: () -> Unit
) {
    var showGiftSheet by remember { mutableStateOf(false) }
    var showSoundboard by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    var seatActionTarget by remember { mutableStateOf<VoiceSeat?>(null) }

    val chatListState = rememberLazyListState()

    // Auto-scroll chat to bottom
    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekSlate900)
    ) {
        // Atmospheric Background
        Image(
            painter = painterResource(id = R.drawable.img_room_bg),
            contentDescription = "Room Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.28f
        )

        // Gradient Veil (Deep Sleek Slate)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SleekSlate900.copy(alpha = 0.92f),
                            SleekSlate900.copy(alpha = 0.82f),
                            SleekSlate900.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Sleek Top Bar
            RoomTopBar(
                title = uiState.currentRoom.title,
                roomId = uiState.currentRoom.id,
                onlineCount = uiState.currentRoom.onlineCount,
                localIp = uiState.localIpAddress,
                onBack = onLeaveRoom,
                onOpenNetwork = { showNetworkDialog = true }
            )

            // Stage Area (Host + 8 Guest Seats)
            StageArea(
                seats = uiState.currentRoom.seats,
                currentUserId = uiState.currentUser.id,
                onSeatClick = { seat ->
                    if (seat.user == null) {
                        viewModel.takeSeat(seat.index, hasAudioPermission)
                        if (!hasAudioPermission) {
                            onRequestAudioPermission()
                        }
                    } else {
                        seatActionTarget = seat
                    }
                }
            )

            // Active Gift Banner Alert
            AnimatedVisibility(
                visible = uiState.activeGiftBanner != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                uiState.activeGiftBanner?.let { banner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekSlate800)
                            .border(1.dp, SleekIndigo500, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = banner.gift.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${banner.senderName} sent ${banner.gift.name} to ${banner.targetName}!",
                            color = SleekSlate100,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(uiState.chatMessages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            // Quick Emojis Row
            QuickEmojiBar(
                onEmojiClick = { emoji ->
                    viewModel.sendChatMessage(emoji)
                }
            )

            // Bottom Action Controls
            BottomActionBar(
                chatText = chatInputText,
                onChatTextChange = { chatInputText = it },
                onSendChat = {
                    viewModel.sendChatMessage(chatInputText)
                    chatInputText = ""
                },
                isSeated = uiState.mySeatIndex >= 0,
                isMicMuted = uiState.isMicMuted,
                isSpeakerMuted = uiState.isSpeakerMuted,
                onToggleMic = {
                    viewModel.toggleMic(hasAudioPermission) {
                        onRequestAudioPermission()
                    }
                },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onToggleSeat = {
                    if (uiState.mySeatIndex >= 0) {
                        viewModel.leaveSeat()
                    } else {
                        val openSeat = uiState.currentRoom.seats.firstOrNull { it.user == null && !it.isLocked }
                        if (openSeat != null) {
                            viewModel.takeSeat(openSeat.index, hasAudioPermission)
                            if (!hasAudioPermission) {
                                onRequestAudioPermission()
                            }
                        }
                    }
                },
                onOpenGiftSheet = { showGiftSheet = true },
                onOpenSoundboard = { showSoundboard = true },
                onOpenNetwork = { showNetworkDialog = true }
            )
        }

        // Modals & Dialogs
        if (showGiftSheet) {
            GiftBottomSheet(
                seats = uiState.currentRoom.seats,
                coinBalance = uiState.coinBalance,
                onSendGift = { gift, targetSeatIdx ->
                    viewModel.sendGift(gift, targetSeatIdx)
                },
                onDismiss = { showGiftSheet = false }
            )
        }

        if (showSoundboard) {
            SoundEffectsSheet(
                activeEffectId = uiState.activeSoundFx,
                onPlayEffect = { effectId ->
                    viewModel.playSoundEffect(effectId)
                },
                onDismiss = { showSoundboard = false }
            )
        }

        if (showNetworkDialog) {
            NetworkDialog(
                localIp = uiState.localIpAddress,
                discoveredPeers = uiState.discoveredPeers,
                isLoopbackEchoEnabled = uiState.isLoopbackEchoEnabled,
                onToggleLoopback = { viewModel.toggleLoopbackEcho() },
                onAddDirectIp = { viewModel.addDirectPeerIp(it) },
                onDismiss = { showNetworkDialog = false }
            )
        }

        // Seat Info / Action Dialog
        seatActionTarget?.let { seat ->
            val isMySeat = seat.user?.id == uiState.currentUser.id
            AlertDialog(
                onDismissRequest = { seatActionTarget = null },
                containerColor = SleekSlate850,
                title = {
                    Text(
                        text = if (seat.index == 0) "Stage Host: ${seat.user?.name}" else "Seat #${seat.index}: ${seat.user?.name}",
                        color = SleekSlate100,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (isMySeat) "You are currently on this seat. You can leave the stage to let someone else join."
                            else "Send gifts to show support or cheer for ${seat.user?.name}!",
                            color = SleekSlate400,
                            fontSize = 13.sp
                        )
                    }
                },
                confirmButton = {
                    if (isMySeat) {
                        Button(
                            onClick = {
                                viewModel.leaveSeat()
                                seatActionTarget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekRed500)
                        ) {
                            Text("Leave Seat (সিট ছাড়ুন)")
                        }
                    } else {
                        Button(
                            onClick = {
                                seatActionTarget = null
                                showGiftSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekIndigo600)
                        ) {
                            Text("Send Gift 🎁")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { seatActionTarget = null }) {
                        Text("Cancel", color = SleekSlate400)
                    }
                }
            )
        }
    }
}

@Composable
fun RoomTopBar(
    title: String,
    roomId: String,
    onlineCount: Int,
    localIp: String,
    onBack: () -> Unit,
    onOpenNetwork: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekSlate900.copy(alpha = 0.9f))
            .border(width = 1.dp, color = SleekSlate800)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Room Icon and Title Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Gradient Graphic EQ Room Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(SleekIndigo500, SleekPurple500)
                        )
                    )
                    .border(2.dp, SleekIndigo400, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Room Audio",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = SleekSlate100,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SleekGreen500)
                    )
                    Text(
                        text = "LIVE ($onlineCount) • ID: $roomId",
                        color = SleekSlate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }

        // Right: LAN status badge + Close button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Network Status Pill Badge (Clickable)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekSlate700, RoundedCornerShape(16.dp))
                    .clickable { onOpenNetwork() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = "Voice LAN",
                    tint = SleekGreen500,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LAN",
                    color = SleekGreen500,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Close / Leave Button (matching Sleek design's red circular close button)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SleekRed500.copy(alpha = 0.15f))
                    .clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Leave",
                    tint = SleekRed500,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun StageArea(
    seats: List<VoiceSeat>,
    currentUserId: String,
    onSeatClick: (VoiceSeat) -> Unit
) {
    val hostSeat = seats.firstOrNull { it.index == 0 } ?: VoiceSeat(0, "Host")
    val guestSeats = seats.filter { it.index != 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Host Stage View (Sleek Elevated Stage)
        VoiceSeatView(
            seat = hostSeat,
            isCurrentUser = hostSeat.user?.id == currentUserId,
            size = 76.dp,
            onClick = { onSeatClick(hostSeat) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 8 Guest Seats in 2 Rows of 4
        val row1 = guestSeats.take(4)
        val row2 = guestSeats.drop(4).take(4)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row1.forEach { seat ->
                VoiceSeatView(
                    seat = seat,
                    isCurrentUser = seat.user?.id == currentUserId,
                    size = 52.dp,
                    onClick = { onSeatClick(seat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row2.forEach { seat ->
                VoiceSeatView(
                    seat = seat,
                    isCurrentUser = seat.user?.id == currentUserId,
                    size = 52.dp,
                    onClick = { onSeatClick(seat) }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    when (message.type) {
        MessageType.SYSTEM, MessageType.SEAT_ACTION -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSlate800.copy(alpha = 0.6f))
                    .border(0.5.dp, SleekSlate700.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System: ",
                    color = SleekIndigo400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.text,
                    color = SleekSlate300,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        MessageType.GIFT -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekPink500.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = message.giftEmoji ?: "🎁", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.senderName,
                    color = SleekPink500,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = message.text,
                    color = SleekAmber400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        MessageType.CHAT -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSlate800.copy(alpha = 0.75f))
                    .border(0.5.dp, SleekSlate700, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = message.senderAvatar, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = message.senderName,
                        color = SleekIndigo400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message.text,
                        color = SleekSlate300,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickEmojiBar(onEmojiClick: (String) -> Unit) {
    val emojis = listOf("🔥", "❤️", "👏", "🎉", "🎤", "💃", "⭐", "😍")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emojis) { emoji ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SleekSlate800)
                    .border(0.5.dp, SleekSlate700, CircleShape)
                    .clickable { onEmojiClick(emoji) }
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun BottomActionBar(
    chatText: String,
    onChatTextChange: (String) -> Unit,
    onSendChat: () -> Unit,
    isSeated: Boolean,
    isMicMuted: Boolean,
    isSpeakerMuted: Boolean,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleSeat: () -> Unit,
    onOpenGiftSheet: () -> Unit,
    onOpenSoundboard: () -> Unit,
    onOpenNetwork: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekSlate900)
            .border(width = 1.dp, color = SleekSlate800)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Chat text input box (Sleek Slate 800 with Slate 700 border)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SleekSlate800)
                .border(1.dp, SleekSlate700, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = chatText,
                onValueChange = onChatTextChange,
                textStyle = TextStyle(color = SleekSlate100, fontSize = 13.sp),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (chatText.isEmpty()) {
                        Text(text = "Say something...", color = SleekSlate500, fontSize = 12.sp)
                    }
                    innerTextField()
                }
            )

            if (chatText.isNotBlank()) {
                IconButton(onClick = onSendChat, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = SleekIndigo400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Take/Leave Seat Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(
                    if (isSeated) SleekSlate800 else SleekIndigo600
                )
                .border(
                    width = 1.dp,
                    color = if (isSeated) SleekRed500.copy(alpha = 0.5f) else SleekIndigo500,
                    shape = RoundedCornerShape(19.dp)
                )
                .clickable { onToggleSeat() }
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = if (isSeated) "Leave" else "Seat",
                color = if (isSeated) SleekRed500 else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Live Microphone Toggle Button (Indigo-600 active / Red muted / Slate800 unseated)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !isSeated -> SleekSlate800
                        isMicMuted -> SleekRed500
                        else -> SleekIndigo600
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (!isSeated) SleekSlate700 else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onToggleMic() }
        ) {
            Icon(
                imageVector = if (!isSeated || isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mic Toggle",
                tint = if (!isSeated) SleekSlate500 else Color.White,
                modifier = Modifier.size(19.dp)
            )
        }

        // DJ Soundboard Button
        IconButton(
            onClick = onOpenSoundboard,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SleekSlate800)
                .border(1.dp, SleekSlate700, CircleShape)
        ) {
            Text(text = "🎛️", fontSize = 16.sp)
        }

        // Gift Button (Gradient pink to indigo)
        IconButton(
            onClick = onOpenGiftSheet,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(SleekPink500, SleekIndigo500)
                    )
                )
        ) {
            Text(text = "🎁", fontSize = 16.sp)
        }

        // Speaker Toggle Button
        IconButton(
            onClick = onToggleSpeaker,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SleekSlate800)
                .border(1.dp, SleekSlate700, CircleShape)
        ) {
            Icon(
                imageVector = if (isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Speaker",
                tint = if (isSpeakerMuted) SleekRed500 else SleekIndigo400,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
