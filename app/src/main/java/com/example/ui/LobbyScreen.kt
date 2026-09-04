package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceRoom
import com.example.ui.components.CreateRoomDialog
import com.example.ui.components.NetworkDialog
import com.example.ui.theme.SleekAmber400
import com.example.ui.theme.SleekGreen500
import com.example.ui.theme.SleekIndigo400
import com.example.ui.theme.SleekIndigo500
import com.example.ui.theme.SleekIndigo600
import com.example.ui.theme.SleekPurple500
import com.example.ui.theme.SleekSlate100
import com.example.ui.theme.SleekSlate400
import com.example.ui.theme.SleekSlate500
import com.example.ui.theme.SleekSlate600
import com.example.ui.theme.SleekSlate700
import com.example.ui.theme.SleekSlate800
import com.example.ui.theme.SleekSlate850
import com.example.ui.theme.SleekSlate900
import com.example.viewmodel.VoiceRoomUiState
import com.example.viewmodel.VoiceRoomViewModel

@Composable
fun LobbyScreen(
    viewModel: VoiceRoomViewModel,
    uiState: VoiceRoomUiState,
    onEnterRoom: (VoiceRoom) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekSlate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(SleekIndigo500, SleekPurple500)))
                            .border(2.dp, SleekIndigo400, CircleShape)
                    ) {
                        Text(text = "🎙️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LiveVoice Party",
                            color = SleekSlate100,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Group Voice Rooms • Sleek Stage",
                            color = SleekSlate400,
                            fontSize = 12.sp
                        )
                    }
                }

                // Network & Info button
                IconButton(
                    onClick = { showNetworkDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SleekSlate800)
                        .border(1.dp, SleekSlate700, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Network Status",
                        tint = SleekIndigo400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Profile Mini Card (Sleek Slate 800)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekSlate700, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SleekSlate850)
                            .border(1.5.dp, SleekIndigo500, CircleShape)
                    ) {
                        Text(text = uiState.currentUser.avatarEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.currentUser.name,
                                color = SleekSlate100,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SleekAmber400.copy(alpha = 0.18f))
                                    .border(0.5.dp, SleekAmber400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Lv.${uiState.currentUser.level}",
                                    color = SleekAmber400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "IP: ${uiState.localIpAddress} • Port 9876",
                            color = SleekSlate400,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙 ${uiState.coinBalance}", color = SleekAmber400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Live Voice Rooms (${uiState.allRooms.size})",
                color = SleekSlate100,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Room List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(uiState.allRooms) { room ->
                    RoomItemCard(
                        room = room,
                        onClick = {
                            viewModel.switchRoom(room)
                            onEnterRoom(room)
                        }
                    )
                }
            }
        }

        // Floating Action Button to Create Room (Sleek Indigo)
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = SleekIndigo600,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Room")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Room", fontWeight = FontWeight.Bold)
            }
        }

        if (showCreateDialog) {
            CreateRoomDialog(
                onDismiss = { showCreateDialog = false },
                onCreateRoom = { title, cat ->
                    viewModel.createRoom(title, cat)
                    onEnterRoom(viewModel.uiState.value.currentRoom)
                }
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
    }
}

@Composable
fun RoomItemCard(
    room: VoiceRoom,
    onClick: () -> Unit
) {
    val occupiedCount = room.seats.count { it.user != null }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSlate800)
            .border(1.dp, SleekSlate700, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(SleekSlate850, SleekSlate900))
                    )
                    .border(1.5.dp, SleekIndigo500.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Text(text = room.host.avatarEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = room.title,
                    color = SleekSlate100,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Host: ${room.host.name} • ${room.category}",
                    color = SleekSlate400,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SleekGreen500)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$occupiedCount/9 On Stage • ${room.onlineCount} listening",
                        color = SleekIndigo400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SleekIndigo600)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Join",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
