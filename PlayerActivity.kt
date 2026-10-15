package com.atmosplayer.app

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val view = PlayerView(this).apply { setShowNextButton(false); setShowPreviousButton(false) }
        setContentView(view)
        hideSystemBars()

        // Prefer Dolby (E-AC-3 JOC = Atmos) tracks, allow passthrough to the device/HDMI/Bluetooth,
        // and don't downmix multichannel audio when the device can spatialize it.
        val renderers = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            .setEnableDecoderFallback(true)

        player = ExoPlayer.Builder(this, renderers).build().also { p ->
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
                .setPreferredAudioMimeTypes(
                    MimeTypes.AUDIO_E_AC3_JOC, MimeTypes.AUDIO_E_AC3,
                    MimeTypes.AUDIO_AC3, MimeTypes.AUDIO_AAC
                )
                .setConstrainAudioChannelCountToDeviceCapabilities(false)
                .setPreferredAudioLanguage("en")
                .build()
            view.player = p
            p.setMediaItem(MediaItem.fromUri(Uri.parse(intent.dataString)))
            p.prepare()
            p.playWhenReady = true
        }
    }

    private fun hideSystemBars() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
