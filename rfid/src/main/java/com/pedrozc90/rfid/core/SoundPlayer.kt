package com.pedrozc90.rfid.core

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.SystemClock
import com.pedrozc90.rfid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @Composable
 * fun SoundDemoScreen(context: Context = LocalContext.current) {
 *     val scope = rememberCoroutineScope()
 *     val soundPlayer = remember { SoundPlayer(context) }
 *
 *     LaunchedEffect(Unit) {
 *         soundPlayer.init()
 *         soundPlayer.start(scope)
 *     }
 *
 *     DisposableEffect(Unit) {
 *         onDispose {
 *             soundPlayer.stop()
 *             soundPlayer.release()
 *         }
 *     }
 *
 *     Column {
 *         Button(onClick = { soundPlayer.play(90) }) { Text("Fast") }
 *         Button(onClick = { soundPlayer.play(60) }) { Text("Medium") }
 *         Button(onClick = { soundPlayer.play(30) }) { Text("Slow") }
 *         Button(onClick = { soundPlayer.playSound(2) }) { Text("Error beep") }
 *     }
 * }
 */
class SoundPlayer(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private var manager: AudioManager? = null
    private var volumeRatio = 1f

    private var playJob: Job? = null
    private var interval = 500L
    private var lastPlayTime = SystemClock.elapsedRealtime()

    fun init() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attrs)
            .build()

        soundMap[1] = soundPool!!.load(context, R.raw.barcodebeep, 1)
        soundMap[2] = soundPool!!.load(context, R.raw.serror, 1)
        manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun playSound(id: Int) {
        val audioMaxVolume = manager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        val audioCurrentVolume =
            manager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        volumeRatio = audioCurrentVolume / audioMaxVolume

        soundMap[id]?.let {
            soundPool?.play(it, volumeRatio, volumeRatio, 1, 0, 1f)
        }
    }

    fun start(scope: CoroutineScope) {
        if (playJob?.isActive == true) return

        playJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastPlayTime < 500) {
                    playSound(1)
                }
                delay(interval)
            }
        }
    }

    fun play(speed: Int) {
        interval = when {
            speed > 85 -> 3L
            speed > 66 -> (100 - speed).toLong()
            speed > 33 -> ((100 - speed) * 2).toLong()
            else -> ((100 - speed) * 3).toLong()
        }
        lastPlayTime = SystemClock.elapsedRealtime()
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }

}

