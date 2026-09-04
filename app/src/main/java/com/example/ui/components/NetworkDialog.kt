package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkPeer
import com.example.ui.theme.SleekGreen500
import com.example.ui.theme.SleekIndigo400
import com.example.ui.theme.SleekIndigo500
import com.example.ui.theme.SleekIndigo600
import com.example.ui.theme.SleekSlate100
import com.example.ui.theme.SleekSlate400
import com.example.ui.theme.SleekSlate500
import com.example.ui.theme.SleekSlate600
import com.example.ui.theme.SleekSlate700
import com.example.ui.theme.SleekSlate800
import com.example.ui.theme.SleekSlate850
import com.example.ui.theme.SleekSlate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDialog(
    localIp: String,
    discoveredPeers: List<NetworkPeer>,
    isLoopbackEchoEnabled: Boolean,
    onToggleLoopback: () -> Unit,
    onAddDirectIp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var manualIpInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SleekSlate850,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Network",
                        tint = SleekIndigo400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Phone Network Voice Audio",
                        color = SleekSlate100,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SleekSlate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bilingual Info Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekSlate700, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = SleekIndigo400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ফোন থেকে অন্য ফোনে কথা বলার নিয়ম:",
                        color = SleekSlate100,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• দুটি ফোন একই Wi-Fi অথবা একটি ফোনের Hotspot দিয়ে অপর ফোন যুক্ত করুন।\n• যে কোনো খালি সিটে ক্লিক করে উপরে উঠুন এবং Mic অন করে কথা বলুন।\n• লাইভ অডিও রিয়েলটাইমে অন্য সব ফোনে শোনা যাবে!",
                    color = SleekSlate400,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // My Device IP & Port
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekSlate700, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "IP",
                        tint = SleekGreen500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "My Phone IP Address", color = SleekSlate400, fontSize = 11.sp)
                        Text(text = "$localIp:9876", color = SleekSlate100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "UDP BROADCAST",
                    color = SleekGreen500,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SleekGreen500.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loopback Self-Test Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SleekSlate800)
                    .border(1.dp, SleekSlate700, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Loopback",
                        tint = SleekIndigo400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Mic Echo / Self-Test",
                            color = SleekSlate100,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hear your own mic voice on this device",
                            color = SleekSlate400,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isLoopbackEchoEnabled,
                    onCheckedChange = { onToggleLoopback() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SleekIndigo600
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct Peer IP Connect Input
            Text(
                text = "Manual Direct Connect (If Wi-Fi blocks broadcast):",
                color = SleekSlate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualIpInput,
                    onValueChange = { manualIpInput = it },
                    placeholder = { Text("e.g. 192.168.1.55", color = SleekSlate500, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SleekIndigo400,
                        unfocusedBorderColor = SleekSlate700
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (manualIpInput.isNotBlank()) {
                            onAddDirectIp(manualIpInput.trim())
                            manualIpInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekIndigo600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add IP")
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Discovered Phones on Wi-Fi
            Text(
                text = "Discovered Phones on Network (${discoveredPeers.size}):",
                color = SleekSlate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (discoveredPeers.isEmpty()) {
                Text(
                    text = "Waiting for other phones on this Wi-Fi... (Open this app on another phone or hotspot)",
                    color = SleekSlate500,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    items(discoveredPeers) { peer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSlate800)
                                .border(1.dp, SleekSlate700, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = "Peer Phone",
                                    tint = SleekGreen500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = peer.name, color = SleekSlate100, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = peer.ipAddress, color = SleekSlate400, fontSize = 10.sp)
                                }
                            }

                            Text(
                                text = if (peer.seatIndex >= 0) "Seat #${peer.seatIndex}" else "Audience",
                                color = SleekIndigo400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
