package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsPlayer
import com.example.audio.VoiceNetworkEngine
import com.example.model.ChatMessage
import com.example.model.GiftItem
import com.example.model.MessageType
import com.example.model.NetworkPeer
import com.example.model.RoomUser
import com.example.model.SoundFxItem
import com.example.model.VoiceRoom
import com.example.model.VoiceSeat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveGiftBanner(
    val gift: GiftItem,
    val senderName: String,
    val targetName: String
)

data class VoiceRoomUiState(
    val currentUser: RoomUser = RoomUser(
        id = UUID.randomUUID().toString().take(8),
        name = "My Phone",
        avatarEmoji = "🎙️",
        level = 12
    ),
    val currentRoom: VoiceRoom = defaultRooms[0],
    val allRooms: List<VoiceRoom> = defaultRooms,
    val coinBalance: Int = 8500,
    val isMicMuted: Boolean = true,
    val isSpeakerMuted: Boolean = false,
    val mySeatIndex: Int = -1, // -1 = in audience
    val chatMessages: List<ChatMessage> = emptyList(),
    val activeGiftBanner: ActiveGiftBanner? = null,
    val isLoopbackEchoEnabled: Boolean = false,
    val localIpAddress: String = "127.0.0.1",
    val discoveredPeers: List<NetworkPeer> = emptyList(),
    val activeSoundFx: String? = null
)

private val defaultRooms = listOf(
    VoiceRoom(
        id = "1001",
        title = "🔥 Bangla Adda Party 🇧🇩",
        category = "Hangout",
        host = RoomUser("host1", "Shakib (Host)", "👑", level = 45, isHost = true),
        seats = listOf(
            VoiceSeat(0, "Host", RoomUser("host1", "Shakib", "👑", 45, true)),
            VoiceSeat(1, "Seat 1", RoomUser("u1", "Apon", "😎", 18)),
            VoiceSeat(2, "Seat 2", null),
            VoiceSeat(3, "Seat 3", RoomUser("u3", "Farhana", "🌸", 24)),
            VoiceSeat(4, "Seat 4", null),
            VoiceSeat(5, "Seat 5", null),
            VoiceSeat(6, "Seat 6", null),
            VoiceSeat(7, "Seat 7", null),
            VoiceSeat(8, "Seat 8", null)
        ),
        onlineCount = 38
    ),
    VoiceRoom(
        id = "1002",
        title = "🎵 Live Acoustic & Songs",
        category = "Music",
        host = RoomUser("host2", "Nabila (Singer)", "🎤", level = 32, isHost = true),
        seats = listOf(
            VoiceSeat(0, "Host", RoomUser("host2", "Nabila", "🎤", 32, true)),
            VoiceSeat(1, "Seat 1", null),
            VoiceSeat(2, "Seat 2", null),
            VoiceSeat(3, "Seat 3", null),
            VoiceSeat(4, "Seat 4", null),
            VoiceSeat(5, "Seat 5", null),
            VoiceSeat(6, "Seat 6", null),
            VoiceSeat(7, "Seat 7", null),
            VoiceSeat(8, "Seat 8", null)
        ),
        onlineCount = 54
    ),
    VoiceRoom(
        id = "1003",
        title = "🎮 Gamers Voice Lounge",
        category = "Gaming",
        host = RoomUser("host3", "ProGamer_BD", "⚡", level = 50, isHost = true),
        seats = listOf(
            VoiceSeat(0, "Host", RoomUser("host3", "ProGamer", "⚡", 50, true)),
            VoiceSeat(1, "Seat 1", null),
            VoiceSeat(2, "Seat 2", null),
            VoiceSeat(3, "Seat 3", null),
            VoiceSeat(4, "Seat 4", null),
            VoiceSeat(5, "Seat 5", null),
            VoiceSeat(6, "Seat 6", null),
            VoiceSeat(7, "Seat 7", null),
            VoiceSeat(8, "Seat 8", null)
        ),
        onlineCount = 42
    )
)

val availableGiftsList = listOf(
    GiftItem("rose", "Rose", "🌹", 10, "Sweet Fragrance"),
    GiftItem("heart", "Love", "💖", 50, "Full of Affection"),
    GiftItem("cake", "Party Cake", "🎂", 200, "Celebration Treat"),
    GiftItem("car", "Supercar", "🏎️", 1000, "Luxury Sports Car"),
    GiftItem("rocket", "Space Rocket", "🚀", 2500, "Blast Off to the Moon"),
    GiftItem("crown", "Diamond Crown", "👑", 5000, "Royal VIP Status")
)

val soundEffectsList = listOf(
    SoundFxItem("applause", "Clap", "👏", "Loud room applause"),
    SoundFxItem("cheer", "Cheer", "🎉", "Party celebration crowd"),
    SoundFxItem("laugh", "Laugh", "😂", "Hilarious group laughter"),
    SoundFxItem("airhorn", "Horn", "📯", "DJ Hype air horn blast"),
    SoundFxItem("bell", "Bell", "🔔", "Clear chime notification"),
    SoundFxItem("whistle", "Whistle", "🎶", "Carnival party whistle")
)

class VoiceRoomViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(VoiceRoomUiState())
    val uiState: StateFlow<VoiceRoomUiState> = _uiState.asStateFlow()

    val networkEngine = VoiceNetworkEngine(application, viewModelScope)
    private var giftDismissJob: Job? = null

    init {
        val deviceId = UUID.randomUUID().toString()
        networkEngine.initialize(deviceId)
        networkEngine.currentRoomId = _uiState.value.currentRoom.id
        networkEngine.currentUserName = _uiState.value.currentUser.name
        val localIp = networkEngine.getLocalIpAddress()

        _uiState.update { it.copy(localIpAddress = localIp) }

        // Setup Network Engine Callbacks
        networkEngine.onSeatVolumeChanged = { seatIdx, vol ->
            viewModelScope.launch {
                updateSeatVolume(seatIdx, vol)
            }
        }

        networkEngine.onPeerDiscovered = { peers ->
            viewModelScope.launch {
                _uiState.update { it.copy(discoveredPeers = peers) }
            }
        }

        networkEngine.onSoundEffectReceived = { effectId ->
            viewModelScope.launch {
                playLocalSoundFx(effectId)
            }
        }

        // Add welcome message
        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderAvatar = "📢",
                text = "Welcome to Live Voice Chat! Network audio broadcasting is ready. Tap an empty seat to speak!",
                type = MessageType.SYSTEM
            )
        )
    }

    private fun updateSeatVolume(seatIdx: Int, volume: Float) {
        val currentSeats = _uiState.value.currentRoom.seats
        if (seatIdx in currentSeats.indices) {
            val updatedSeats = currentSeats.map { seat ->
                if (seat.index == seatIdx) {
                    seat.copy(talkingLevel = volume)
                } else seat
            }
            _uiState.update { state ->
                state.copy(currentRoom = state.currentRoom.copy(seats = updatedSeats))
            }
        }
    }

    fun takeSeat(seatIndex: Int, hasAudioPermission: Boolean) {
        val state = _uiState.value
        val targetSeat = state.currentRoom.seats.find { it.index == seatIndex } ?: return
        if (targetSeat.isLocked || (targetSeat.user != null && targetSeat.user.id != state.currentUser.id)) {
            return
        }

        // Leave any previous seat
        val updatedSeats = state.currentRoom.seats.map { seat ->
            when {
                seat.index == seatIndex -> seat.copy(user = state.currentUser, isMuted = state.isMicMuted)
                seat.user?.id == state.currentUser.id -> seat.copy(user = null, talkingLevel = 0f)
                else -> seat
            }
        }

        _uiState.update {
            it.copy(
                mySeatIndex = seatIndex,
                currentRoom = it.currentRoom.copy(seats = updatedSeats)
            )
        }

        networkEngine.currentSeatIndex = seatIndex
        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderAvatar = "🎙️",
                text = "${state.currentUser.name} joined ${if (seatIndex == 0) "Host Stage" else "Seat $seatIndex"}",
                type = MessageType.SEAT_ACTION
            )
        )

        if (!state.isMicMuted && hasAudioPermission) {
            networkEngine.startMicrophoneTransmission()
        }
    }

    fun leaveSeat() {
        val state = _uiState.value
        val prevSeatIdx = state.mySeatIndex
        if (prevSeatIdx < 0) return

        networkEngine.stopMicrophoneTransmission()
        networkEngine.currentSeatIndex = -1

        val updatedSeats = state.currentRoom.seats.map { seat ->
            if (seat.index == prevSeatIdx) {
                seat.copy(user = null, talkingLevel = 0f)
            } else seat
        }

        _uiState.update {
            it.copy(
                mySeatIndex = -1,
                isMicMuted = true,
                currentRoom = it.currentRoom.copy(seats = updatedSeats)
            )
        }

        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderAvatar = "👋",
                text = "${state.currentUser.name} left the seat and moved to audience",
                type = MessageType.SEAT_ACTION
            )
        )
    }

    fun toggleMic(hasPermission: Boolean, onPermissionRequired: () -> Unit) {
        val state = _uiState.value
        if (state.mySeatIndex < 0) {
            // Not in seat: prompt user to take a seat first
            addMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = "System",
                    senderAvatar = "ℹ️",
                    text = "Please tap an open seat on stage to speak!",
                    type = MessageType.SYSTEM
                )
            )
            return
        }

        val willUnmute = state.isMicMuted
        if (willUnmute && !hasPermission) {
            onPermissionRequired()
            return
        }

        val newMuteState = !state.isMicMuted
        networkEngine.isMicMuted = newMuteState

        val updatedSeats = state.currentRoom.seats.map { seat ->
            if (seat.index == state.mySeatIndex) {
                seat.copy(isMuted = newMuteState)
            } else seat
        }

        _uiState.update {
            it.copy(
                isMicMuted = newMuteState,
                currentRoom = it.currentRoom.copy(seats = updatedSeats)
            )
        }

        if (newMuteState) {
            networkEngine.stopMicrophoneTransmission()
        } else {
            networkEngine.startMicrophoneTransmission()
        }
    }

    fun toggleSpeaker() {
        val newSpeakerState = !_uiState.value.isSpeakerMuted
        networkEngine.setSpeakerMuted(newSpeakerState)
        _uiState.update { it.copy(isSpeakerMuted = newSpeakerState) }
    }

    fun toggleLoopbackEcho() {
        val next = !_uiState.value.isLoopbackEchoEnabled
        networkEngine.setLoopbackEchoEnabled(next)
        _uiState.update { it.copy(isLoopbackEchoEnabled = next) }
    }

    fun toggleSeatLock(seatIndex: Int) {
        val updatedSeats = _uiState.value.currentRoom.seats.map { seat ->
            if (seat.index == seatIndex) {
                seat.copy(isLocked = !seat.isLocked)
            } else seat
        }
        _uiState.update {
            it.copy(currentRoom = it.currentRoom.copy(seats = updatedSeats))
        }
    }

    fun sendGift(gift: GiftItem, targetSeatIndex: Int) {
        val state = _uiState.value
        if (state.coinBalance < gift.cost) {
            addMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = "System",
                    senderAvatar = "⚠️",
                    text = "Insufficient coins! Need ${gift.cost} coins.",
                    type = MessageType.SYSTEM
                )
            )
            return
        }

        val targetSeat = state.currentRoom.seats.find { it.index == targetSeatIndex }
        val targetName = targetSeat?.user?.name ?: if (targetSeatIndex == 0) "Host" else "Seat $targetSeatIndex"

        _uiState.update {
            it.copy(
                coinBalance = it.coinBalance - gift.cost,
                activeGiftBanner = ActiveGiftBanner(gift, state.currentUser.name, targetName)
            )
        }

        // Play celebration cheer sound
        SoundEffectsPlayer.playEffect("cheer", viewModelScope)

        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = state.currentUser.name,
                senderAvatar = state.currentUser.avatarEmoji,
                text = "sent ${gift.emoji} ${gift.name} to $targetName!",
                type = MessageType.GIFT,
                giftEmoji = gift.emoji,
                giftName = gift.name
            )
        )

        // Dismiss banner after 3 seconds
        giftDismissJob?.cancel()
        giftDismissJob = viewModelScope.launch {
            delay(3200)
            _uiState.update { it.copy(activeGiftBanner = null) }
        }
    }

    fun playSoundEffect(effectId: String) {
        playLocalSoundFx(effectId)
        networkEngine.broadcastSoundEffect(effectId)
    }

    private fun playLocalSoundFx(effectId: String) {
        _uiState.update { it.copy(activeSoundFx = effectId) }
        SoundEffectsPlayer.playEffect(effectId, viewModelScope)
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(activeSoundFx = null) }
        }
    }

    fun sendChatMessage(text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        val state = _uiState.value
        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = state.currentUser.name,
                senderAvatar = state.currentUser.avatarEmoji,
                text = cleanText,
                type = MessageType.CHAT
            )
        )
    }

    private fun addMessage(msg: ChatMessage) {
        _uiState.update {
            val updated = it.chatMessages + msg
            it.copy(chatMessages = if (updated.size > 80) updated.takeLast(80) else updated)
        }
    }

    fun updateUserName(newName: String) {
        val clean = newName.trim()
        if (clean.isNotEmpty()) {
            _uiState.update {
                it.copy(currentUser = it.currentUser.copy(name = clean))
            }
            networkEngine.currentUserName = clean
        }
    }

    fun addDirectPeerIp(ip: String) {
        networkEngine.addDirectPeerIp(ip)
    }

    fun switchRoom(room: VoiceRoom) {
        leaveSeat()
        networkEngine.currentRoomId = room.id
        _uiState.update {
            it.copy(currentRoom = room, chatMessages = emptyList())
        }
        addMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "System",
                senderAvatar = "📢",
                text = "Switched to ${room.title}. Network audio synced.",
                type = MessageType.SYSTEM
            )
        )
    }

    fun createRoom(title: String, category: String) {
        val state = _uiState.value
        val newRoom = VoiceRoom(
            id = (1000 + (100..999).random()).toString(),
            title = title,
            category = category,
            host = state.currentUser.copy(isHost = true),
            seats = listOf(
                VoiceSeat(0, "Host", state.currentUser.copy(isHost = true)),
                VoiceSeat(1, "Seat 1", null),
                VoiceSeat(2, "Seat 2", null),
                VoiceSeat(3, "Seat 3", null),
                VoiceSeat(4, "Seat 4", null),
                VoiceSeat(5, "Seat 5", null),
                VoiceSeat(6, "Seat 6", null),
                VoiceSeat(7, "Seat 7", null),
                VoiceSeat(8, "Seat 8", null)
            ),
            onlineCount = 1
        )

        _uiState.update {
            it.copy(
                allRooms = listOf(newRoom) + it.allRooms,
                currentRoom = newRoom,
                mySeatIndex = 0,
                isMicMuted = false
            )
        }
        networkEngine.currentRoomId = newRoom.id
        networkEngine.currentSeatIndex = 0
        networkEngine.isMicMuted = false
        networkEngine.startMicrophoneTransmission()
    }

    override fun onCleared() {
        super.onCleared()
        networkEngine.release()
    }
}
