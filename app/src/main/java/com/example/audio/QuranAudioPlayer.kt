package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.network.QuranApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Surah? = null,
    val currentReciter: Reciter = QuranApiService.availableReciters.first(),
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val errorMessage: String? = null
)

class QuranAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playSurah(surah: Surah, reciter: Reciter = _playbackState.value.currentReciter) {
        stop()

        _playbackState.value = _playbackState.value.copy(
            isLoading = true,
            currentSurah = surah,
            currentReciter = reciter,
            errorMessage = null,
            currentPositionMs = 0,
            durationMs = 0
        )

        try {
            val audioUrl = QuranApiService.getSurahAudioUrl(reciter, surah.id)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        durationMs = mp.duration
                    )
                    startProgressTracking()
                }
                setOnCompletionListener {
                    stopProgressTracking()
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPositionMs = 0
                    )
                }
                setOnErrorListener { _, what, extra ->
                    stopProgressTracking()
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = false,
                        errorMessage = "تعذر تشغيل الصوت من المصدر"
                    )
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                isLoading = false,
                isPlaying = false,
                errorMessage = "خطأ في تشغيل الصوت: ${e.message}"
            )
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            stopProgressTracking()
        } else {
            mp.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracking()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun setReciter(reciter: Reciter) {
        _playbackState.value = _playbackState.value.copy(currentReciter = reciter)
        val currentSurah = _playbackState.value.currentSurah
        if (currentSurah != null && _playbackState.value.isPlaying) {
            playSurah(currentSurah, reciter)
        }
    }

    fun stop() {
        stopProgressTracking()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isLoading = false,
            currentPositionMs = 0
        )
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = mp.currentPosition,
                            durationMs = mp.duration
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
