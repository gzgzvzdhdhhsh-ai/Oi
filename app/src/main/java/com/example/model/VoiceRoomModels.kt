package com.example.model

enum class MessageType {
    CHAT,
    SYSTEM,
    GIFT,
    SEAT_ACTION
}

data class RoomUser(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val level: Int = 1,
    val isHost: Boolean = false,
    val ipAddress: String = ""
)

data class VoiceSeat(
    val index: Int, // 0 = Host seat, 1..8 = Guest seats
    val label: String,
    val user: RoomUser? = null,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val talkingLevel: Float = 0f // 0.0f to 1.0f for speaking ripples
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val type: MessageType = MessageType.CHAT,
    val timestamp: Long = System.currentTimeMillis(),
    val giftEmoji: String? = null,
    val giftName: String? = null
)

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Int,
    val description: String
)

data class SoundFxItem(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String
)

data class VoiceRoom(
    val id: String,
    val title: String,
    val category: String,
    val host: RoomUser,
    val seats: List<VoiceSeat>,
    val onlineCount: Int = 1,
    val announcement: String = "Welcome to the Live Voice Party! Tap any seat to join the stage and talk."
)

data class NetworkPeer(
    val deviceId: String,
    val name: String,
    val ipAddress: String,
    val seatIndex: Int,
    val isMuted: Boolean,
    val lastSeenMs: Long
)
