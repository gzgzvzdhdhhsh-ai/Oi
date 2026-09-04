package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceSeat
import com.example.ui.theme.SleekGreen500
import com.example.ui.theme.SleekIndigo400
import com.example.ui.theme.SleekIndigo500
import com.example.ui.theme.SleekIndigo600
import com.example.ui.theme.SleekPink500
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

@Composable
fun VoiceSeatView(
    seat: VoiceSeat,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    onClick: () -> Unit
) {
    val isHost = seat.index == 0
    val isSpeaking = seat.talkingLevel > 0.05f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.25f else if (isHost) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size + 14.dp)
        ) {
            // Animated Pulse Glow (Indigo for Host / Green when speaking)
            if (isSpeaking || isHost) {
                Box(
                    modifier = Modifier
                        .size(size + 12.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isSpeaking) SleekGreen500.copy(alpha = 0.35f)
                                    else SleekIndigo500.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Avatar circle / Empty slot
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        if (seat.user != null) {
                            if (isHost) {
                                Brush.linearGradient(listOf(SleekSlate800, SleekSlate850))
                            } else if (isCurrentUser) {
                                Brush.linearGradient(listOf(Color(0xFF312E81), SleekSlate800))
                            } else {
                                Brush.linearGradient(listOf(SleekSlate800, SleekSlate850))
                            }
                        } else {
                            Brush.linearGradient(listOf(SleekSlate800, SleekSlate800))
                        }
                    )
                    .border(
                        width = if (isHost) 3.5.dp else if (isSpeaking) 2.dp else if (isCurrentUser) 2.dp else 1.5.dp,
                        color = when {
                            isSpeaking -> SleekGreen500
                            isHost -> SleekIndigo500
                            isCurrentUser -> SleekIndigo400
                            seat.user != null -> SleekIndigo400.copy(alpha = 0.7f)
                            else -> SleekSlate600
                        },
                        shape = CircleShape
                    )
            ) {
                if (seat.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Seat",
                        tint = SleekSlate500,
                        modifier = Modifier.size(size * 0.38f)
                    )
                } else if (seat.user != null) {
                    Text(
                        text = seat.user.avatarEmoji,
                        fontSize = (size.value * 0.44f).sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Empty Seat",
                        tint = SleekSlate500,
                        modifier = Modifier.size(size * 0.42f)
                    )
                }
            }

            // Top-right Mic / Mute indicator badge (matching Sleek Interface design)
            if (seat.user != null) {
                if (seat.isMuted) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(SleekRed500)
                            .border(2.dp, SleekSlate900, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Muted",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                } else if (isSpeaking || isHost) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(SleekGreen500)
                            .border(2.dp, SleekSlate900, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic On",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // "HOST" Pill Badge anchored at bottom of Host Avatar
            if (isHost) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekIndigo500)
                        .border(1.dp, SleekIndigo400, RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "HOST",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isHost) 6.dp else 2.dp))

        // Seat User Name / Label
        Text(
            text = if (seat.user != null) {
                if (isCurrentUser) "You" else seat.user.name
            } else if (seat.isLocked) {
                "Locked"
            } else {
                "Seat ${seat.index}"
            },
            color = if (seat.user != null) SleekSlate100 else SleekSlate400,
            fontSize = if (isHost) 13.sp else 10.sp,
            fontWeight = if (seat.user != null) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        // Pill indicator for Seat Number (for guest seats)
        if (!isHost) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SleekSlate800)
                    .border(0.5.dp, SleekSlate700, RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "#${seat.index}",
                    color = SleekSlate400,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

