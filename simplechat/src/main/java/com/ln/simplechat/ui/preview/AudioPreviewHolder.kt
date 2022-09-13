package com.ln.simplechat.ui.preview

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.ln.simplechat.R
import com.ln.simplechat.databinding.LayoutPreviewAudioBinding
import com.ln.simplechat.model.ChatMedia
import com.luck.picture.lib.utils.DateUtils
import java.io.IOException

class AudioPreviewHolder(binding: LayoutPreviewAudioBinding) : PreviewHolder(binding.root) {

    companion object {
        private val MAX_UPDATE_INTERVAL_MS: Long = 1000
        private val MIN_CURRENT_POSITION: Long = 1000
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private var ivPlayButton = binding.ivPlayVideo
    private var tvAudioName = binding.tvAudioName
    private var tvTotalDuration = binding.tvTotalDuration
    private var tvCurrentTime = binding.tvCurrentTime
    private var seekBar = binding.musicSeekBar
    private var mPlayer: MediaPlayer = MediaPlayer()
    private var isPausePlayer = false

    private var mTickerRunnable: Runnable = object : Runnable {
        override fun run() {
            val currentPosition = mPlayer.currentPosition.toLong()
            val time = DateUtils.formatDurationTime(currentPosition)
            if (!TextUtils.equals(time, tvCurrentTime.text)) {
                tvCurrentTime.text = time
                if (mPlayer.duration - currentPosition > MIN_CURRENT_POSITION) {
                    seekBar.progress = currentPosition.toInt()
                } else {
                    seekBar.progress = mPlayer.duration
                }
            }
            val nextSecondMs = MAX_UPDATE_INTERVAL_MS - currentPosition % MAX_UPDATE_INTERVAL_MS
            mHandler.postDelayed(this, nextSecondMs)
        }
    }

    override fun bind(media: ChatMedia) {
        tvAudioName.text = media.description
        tvAudioName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            com.luck.picture.lib.R.drawable.ps_ic_audio_play_cover,
            0,
            0
        )
        tvTotalDuration.text = DateUtils.formatDurationTime(media.duration)
        seekBar.max = media.duration.toInt()

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar.progress = progress
                    setCurrentPlayTime(progress)
                    if (mPlayer.isPlaying) {
                        mPlayer.seekTo(seekBar.progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        ivPlayButton.setOnClickListener {
            try {
                if (mPlayer.isPlaying) {
                    pausePlayer()
                } else {
                    if (isPausePlayer) {
                        resumePlayer()
                    } else {
                        startPlayer(media.path)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        startPlayer(media.path)
    }


    private fun startPlayer(path: String) {
        try {
            mPlayer.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mPlayer.setDataSource(path)
            mPlayer.prepare()
            mPlayer.seekTo(seekBar.progress)
            mPlayer.start()
            isPausePlayer = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun pausePlayer() {
        mPlayer.pause()
        isPausePlayer = true
        playerDefaultUI(false)
        stopUpdateProgress()
    }

    private fun resumePlayer() {
        mPlayer.seekTo(seekBar.progress)
        mPlayer.start()
        startUpdateProgress()
        playerIngUI()
    }

    private fun resetMediaPlayer() {
        isPausePlayer = false
        mPlayer.stop()
        mPlayer.reset()
    }

    private fun setCurrentPlayTime(progress: Int) {
        val time = DateUtils.formatDurationTime(progress.toLong())
        tvCurrentTime.text = time
    }

    private val mPlayCompletionListener = OnCompletionListener {
        stopUpdateProgress()
        resetMediaPlayer()
        playerDefaultUI(true)
    }

    private val mPlayErrorListener =
        MediaPlayer.OnErrorListener { mp, what, extra ->
            resetMediaPlayer()
            playerDefaultUI(true)
            false
        }

    private val mPlayPreparedListener = OnPreparedListener { mp ->
        if (mp.isPlaying) {
            seekBar.max = mp.duration
            startUpdateProgress()
            playerIngUI()
        } else {
            stopUpdateProgress()
            resetMediaPlayer()
            playerDefaultUI(true)
        }
    }

    private fun startUpdateProgress() {
        mHandler.post(mTickerRunnable)
    }

    private fun stopUpdateProgress() {
        mHandler.removeCallbacks(mTickerRunnable)
    }

    private fun playerDefaultUI(isResetProgress: Boolean) {
        stopUpdateProgress()
        if (isResetProgress) {
            seekBar.progress = 0
            tvCurrentTime.text = "00:00"
        }
        ivPlayButton.setImageResource(R.drawable.ps_ic_audio_play)
    }

    private fun playerIngUI() {
        startUpdateProgress()
        ivPlayButton.setImageResource(R.drawable.ps_ic_audio_stop)
    }

    override fun onViewAttachedToWindow() {
        isPausePlayer = false
        setMediaPlayerListener()
        playerDefaultUI(true)
    }

    override fun onViewDetachedFromWindow() {
        isPausePlayer = false
        mHandler.removeCallbacks(mTickerRunnable)
        setNullMediaPlayerListener()
        resetMediaPlayer()
        playerDefaultUI(true)
    }

    fun releaseAudio() {
        mHandler.removeCallbacks(mTickerRunnable)
        if (mPlayer != null) {
            setNullMediaPlayerListener()
            mPlayer.release()
        }
    }

    private fun setMediaPlayerListener() {
        mPlayer.setOnCompletionListener(mPlayCompletionListener)
        mPlayer.setOnErrorListener(mPlayErrorListener)
        mPlayer.setOnPreparedListener(mPlayPreparedListener)
    }

    private fun setNullMediaPlayerListener() {
        mPlayer.setOnCompletionListener(null)
        mPlayer.setOnErrorListener(null)
        mPlayer.setOnPreparedListener(null)
    }
}
