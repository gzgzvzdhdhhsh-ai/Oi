package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.NetworkPeer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class VoiceNetworkEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "VoiceNetworkEngine"
        const val PORT = 9876
        const val SAMPLE_RATE = 16000
        const val MAGIC_BYTE: Byte = 0x56 // 'V'
        const val TYPE_AUDIO: Byte = 1
        const val TYPE_BEACON: Byte = 2
        const val TYPE_SOUND_FX: Byte = 3
    }

    private var socket: DatagramSocket? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    private val isRunning = AtomicBoolean(false)
    private val isTransmitting = AtomicBoolean(false)
    private val isSpeakerMuted = AtomicBoolean(false)
    private val isLoopbackEchoEnabled = AtomicBoolean(false)

    private var receiveJob: Job? = null
    private var recordJob: Job? = null
    private var beaconJob: Job? = null

    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    private val directPeerIps = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private val activePeers = ConcurrentHashMap<String, NetworkPeer>()

    // Current session state
    var currentRoomId: String = "1001"
    var currentDeviceId: String = ""
    var currentUserName: String = "User"
    var currentSeatIndex: Int = -1 // -1 = audience, 0 = host, 1..8 = guest
    var isMicMuted: Boolean = true

    // Callbacks
    var onSeatVolumeChanged: ((seatIndex: Int, volume: Float) -> Unit)? = null
    var onPeerDiscovered: ((List<NetworkPeer>) -> Unit)? = null
    var onSoundEffectReceived: ((effectId: String) -> Unit)? = null

    fun initialize(deviceId: String) {
        currentDeviceId = deviceId
        acquireMulticastLock()
        initAudioTrack()
        startSocket()
        startReceiving()
        startBeacon()
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("LiveVoiceMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire multicast lock: ${e.message}")
        }
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize * 2, 4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "AudioTrack init failed: ${e.message}")
        }
    }

    private fun startSocket() {
        try {
            socket = DatagramSocket(PORT).apply {
                broadcast = true
                reuseAddress = true
            }
            isRunning.set(true)
        } catch (e: Exception) {
            Log.w(TAG, "Could not bind to port $PORT, trying random port: ${e.message}")
            try {
                socket = DatagramSocket().apply {
                    broadcast = true
                }
                isRunning.set(true)
            } catch (ex: Exception) {
                Log.e(TAG, "Socket init completely failed: ${ex.message}")
            }
        }
    }

    private fun startReceiving() {
        receiveJob = coroutineScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(2048)
            val packet = DatagramPacket(buffer, buffer.size)

            while (isActive && isRunning.get()) {
                try {
                    val currentSocket = socket ?: break
                    currentSocket.receive(packet)

                    if (packet.length < 12) continue

                    val magic = buffer[0]
                    if (magic != MAGIC_BYTE) continue

                    val type = buffer[1]
                    val byteBuf = ByteBuffer.wrap(buffer, 2, packet.length - 2)
                    val roomIdHash = byteBuf.int
                    val senderIdHash = byteBuf.int
                    val seatIndex = byteBuf.get().toInt()
                    val volume = (byteBuf.get().toInt() and 0xFF) / 100f

                    // Ignore packets from other rooms
                    if (roomIdHash != currentRoomId.hashCode()) continue

                    val isFromMe = (senderIdHash == currentDeviceId.hashCode())
                    if (isFromMe && !isLoopbackEchoEnabled.get()) {
                        continue
                    }

                    val senderIp = packet.address.hostAddress ?: ""
                    if (senderIp.isNotEmpty() && !isFromMe) {
                        directPeerIps.add(senderIp)
                    }

                    when (type) {
                        TYPE_AUDIO -> {
                            val pcmLength = packet.length - 12
                            if (pcmLength > 0 && !isSpeakerMuted.get()) {
                                audioTrack?.write(buffer, 12, pcmLength)
                                onSeatVolumeChanged?.invoke(seatIndex, volume)
                            }
                        }
                        TYPE_BEACON -> {
                            val peerNameLen = if (packet.length > 12) buffer[12].toInt() else 0
                            val peerName = if (packet.length >= 13 + peerNameLen && peerNameLen > 0) {
                                String(buffer, 13, peerNameLen, Charsets.UTF_8)
                            } else "Peer ${packet.address.hostAddress?.takeLast(4) ?: ""}"

                            val peer = NetworkPeer(
                                deviceId = senderIdHash.toString(),
                                name = peerName,
                                ipAddress = senderIp,
                                seatIndex = seatIndex,
                                isMuted = (volume < 0.01f),
                                lastSeenMs = System.currentTimeMillis()
                            )
                            activePeers[peer.deviceId] = peer
                            onPeerDiscovered?.invoke(activePeers.values.toList())
                        }
                        TYPE_SOUND_FX -> {
                            val fxLen = if (packet.length > 12) buffer[12].toInt() else 0
                            if (fxLen > 0 && packet.length >= 13 + fxLen) {
                                val effectId = String(buffer, 13, fxLen, Charsets.UTF_8)
                                onSoundEffectReceived?.invoke(effectId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!isRunning.get()) break
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startMicrophoneTransmission() {
        if (isTransmitting.get()) return
        isTransmitting.set(true)

        recordJob = coroutineScope.launch(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, 1280) // ~40ms buffer

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord could not initialize")
                    isTransmitting.set(false)
                    return@launch
                }

                audioRecord?.startRecording()
                val audioBuffer = ByteArray(640) // 20ms frames
                val packetBuffer = ByteArray(12 + audioBuffer.size)

                while (isActive && isTransmitting.get() && !isMicMuted && currentSeatIndex >= 0) {
                    val readBytes = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                    if (readBytes > 0) {
                        // Calculate RMS level
                        val volume = calculateRmsVolume(audioBuffer, readBytes)
                        onSeatVolumeChanged?.invoke(currentSeatIndex, volume)

                        // Build Packet
                        packetBuffer[0] = MAGIC_BYTE
                        packetBuffer[1] = TYPE_AUDIO
                        val buf = ByteBuffer.wrap(packetBuffer, 2, 10)
                        buf.putInt(currentRoomId.hashCode())
                        buf.putInt(currentDeviceId.hashCode())
                        buf.put(currentSeatIndex.toByte())
                        buf.put((volume * 100).toInt().coerceIn(0, 100).toByte())
                        System.arraycopy(audioBuffer, 0, packetBuffer, 12, readBytes)

                        broadcastPacket(packetBuffer, 12 + readBytes)

                        // Loopback if enabled
                        if (isLoopbackEchoEnabled.get()) {
                            audioTrack?.write(audioBuffer, 0, readBytes)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio recording failed: ${e.message}")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                audioRecord = null
                isTransmitting.set(false)
                onSeatVolumeChanged?.invoke(currentSeatIndex, 0f)
            }
        }
    }

    fun stopMicrophoneTransmission() {
        isTransmitting.set(false)
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        if (currentSeatIndex >= 0) {
            onSeatVolumeChanged?.invoke(currentSeatIndex, 0f)
        }
    }

    private fun startBeacon() {
        beaconJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive && isRunning.get()) {
                sendPresenceBeacon()
                // Clean up stale peers older than 10s
                val now = System.currentTimeMillis()
                val staleKeys = activePeers.filter { now - it.value.lastSeenMs > 10000 }.keys
                staleKeys.forEach { activePeers.remove(it) }
                if (staleKeys.isNotEmpty()) {
                    onPeerDiscovered?.invoke(activePeers.values.toList())
                }
                delay(3000)
            }
        }
    }

    private fun sendPresenceBeacon() {
        try {
            val nameBytes = currentUserName.toByteArray(Charsets.UTF_8).take(30).toByteArray()
            val totalLen = 13 + nameBytes.size
            val beaconBuffer = ByteArray(totalLen)
            beaconBuffer[0] = MAGIC_BYTE
            beaconBuffer[1] = TYPE_BEACON
            val buf = ByteBuffer.wrap(beaconBuffer, 2, 10)
            buf.putInt(currentRoomId.hashCode())
            buf.putInt(currentDeviceId.hashCode())
            buf.put(currentSeatIndex.toByte())
            buf.put(if (isMicMuted) 0.toByte() else 100.toByte())
            beaconBuffer[12] = nameBytes.size.toByte()
            System.arraycopy(nameBytes, 0, beaconBuffer, 13, nameBytes.size)

            broadcastPacket(beaconBuffer, totalLen)
        } catch (e: Exception) {
            Log.w(TAG, "Beacon send failed: ${e.message}")
        }
    }

    fun broadcastSoundEffect(effectId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val fxBytes = effectId.toByteArray(Charsets.UTF_8)
                val totalLen = 13 + fxBytes.size
                val buffer = ByteArray(totalLen)
                buffer[0] = MAGIC_BYTE
                buffer[1] = TYPE_SOUND_FX
                val buf = ByteBuffer.wrap(buffer, 2, 10)
                buf.putInt(currentRoomId.hashCode())
                buf.putInt(currentDeviceId.hashCode())
                buf.put(currentSeatIndex.toByte())
                buf.put(80.toByte())
                buffer[12] = fxBytes.size.toByte()
                System.arraycopy(fxBytes, 0, buffer, 13, fxBytes.size)

                broadcastPacket(buffer, totalLen)
            } catch (e: Exception) {
                Log.w(TAG, "Sound effect broadcast failed: ${e.message}")
            }
        }
    }

    private fun broadcastPacket(data: ByteArray, length: Int) {
        val curSocket = socket ?: return
        try {
            // Broadcast to 255.255.255.255
            val broadcastAddr = InetAddress.getByName("255.255.255.255")
            val packet = DatagramPacket(data, length, broadcastAddr, PORT)
            curSocket.send(packet)

            // Also send directly to all known peer IPs to handle APs with broadcast isolation
            for (ip in directPeerIps) {
                try {
                    val peerAddr = InetAddress.getByName(ip)
                    val directPacket = DatagramPacket(data, length, peerAddr, PORT)
                    curSocket.send(directPacket)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            // Ignore temporary socket send hiccups
        }
    }

    private fun calculateRmsVolume(buffer: ByteArray, length: Int): Float {
        var sum = 0.0
        val numShorts = length / 2
        for (i in 0 until numShorts) {
            val sample = ((buffer[i * 2 + 1].toInt() shl 8) or (buffer[i * 2].toInt() and 0xFF)).toShort()
            sum += sample * sample
        }
        val rms = sqrt(sum / numShorts.coerceAtLeast(1))
        // Map 0..32767 to 0.0..1.0 with soft threshold
        return ((rms - 300.0) / 3500.0).toFloat().coerceIn(0f, 1f)
    }

    fun addDirectPeerIp(ip: String) {
        if (ip.isNotBlank()) {
            directPeerIps.add(ip.trim())
        }
    }

    fun setSpeakerMuted(muted: Boolean) {
        isSpeakerMuted.set(muted)
    }

    fun setLoopbackEchoEnabled(enabled: Boolean) {
        isLoopbackEchoEnabled.set(enabled)
    }

    fun isLoopbackEcho(): Boolean = isLoopbackEchoEnabled.get()

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr.hostAddress?.indexOf(':') ?: -1 < 0) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    fun release() {
        isRunning.set(false)
        stopMicrophoneTransmission()
        receiveJob?.cancel()
        beaconJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        try {
            socket?.close()
        } catch (_: Exception) {}
        try {
            multicastLock?.release()
        } catch (_: Exception) {}
    }
}
