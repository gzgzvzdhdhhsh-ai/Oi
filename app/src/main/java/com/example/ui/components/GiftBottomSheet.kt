package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GiftItem
import com.example.model.VoiceSeat
import com.example.ui.theme.SleekAmber400
import com.example.ui.theme.SleekIndigo400
import com.example.ui.theme.SleekIndigo500
import com.example.ui.theme.SleekIndigo600
import com.example.ui.theme.SleekPink500
import com.example.ui.theme.SleekSlate100
import com.example.ui.theme.SleekSlate400
import com.example.ui.theme.SleekSlate500
import com.example.ui.theme.SleekSlate600
import com.example.ui.theme.SleekSlate700
import com.example.ui.theme.SleekSlate800
import com.example.ui.theme.SleekSlate850
import com.example.ui.theme.SleekSlate900
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.availableGiftsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    seats: List<VoiceSeat>,
    coinBalance: Int,
    onSendGift: (gift: GiftItem, targetSeatIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGift by remember { mutableStateOf<GiftItem?>(availableGiftsList.firstOrNull()) }
    var selectedSeatIndex by remember { mutableIntStateOf(0) } // Default to Host

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
            // Header: Title, Coins, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎁 Send Gifts to Stage",
                    color = SleekSlate100,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Coin Balance Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSlate800)
                        .border(1.dp, SleekAmber400.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = SleekAmber400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coinBalance",
                        color = SleekAmber400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
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

            // Select Recipient Seat Row
            Text(
                text = "Send To:",
                color = SleekSlate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            val occupiedSeats = seats.filter { it.user != null }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(occupiedSeats) { seat ->
                    val isSelected = seat.index == selectedSeatIndex
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) SleekIndigo600 else SleekSlate800
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SleekIndigo400 else SleekSlate700,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedSeatIndex = seat.index }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = seat.user?.avatarEmoji ?: "👤", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (seat.index == 0) "Host (${seat.user?.name ?: ""})" else "#${seat.index} ${seat.user?.name ?: ""}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                items(availableGiftsList) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) SleekSlate800
                                else SleekSlate900
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) SleekPink500 else SleekSlate700,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGift = gift }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    ) {
                        Text(text = gift.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gift.name,
                            color = SleekSlate100,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${gift.cost}",
                                color = SleekAmber400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send Action Button
            Button(
                onClick = {
                    selectedGift?.let {
                        onSendGift(it, selectedSeatIndex)
                        onDismiss()
                    }
                },
                enabled = selectedGift != null && coinBalance >= (selectedGift?.cost ?: 0),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selectedGift != null && coinBalance >= (selectedGift?.cost ?: 0)) {
                            Brush.horizontalGradient(listOf(SleekPink500, SleekIndigo600))
                        } else {
                            Brush.horizontalGradient(listOf(SleekSlate700, SleekSlate800))
                        }
                    )
            ) {
                Text(
                    text = "Send ${selectedGift?.emoji ?: ""} ${selectedGift?.name ?: "Gift"}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
