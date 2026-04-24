package com.quick.play.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.quick.play.data.Channel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    channel: Channel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    
    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSize(1920, 1080)
                    .setPreferredVideoMimeType("video/avc")
            )
        }
    }
    
    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
            if (channel.userAgent.isNotEmpty()) {
                setUserAgent(channel.userAgent)
            }
            if (channel.cookie.isNotEmpty()) {
                setDefaultRequestProperties(mapOf("Cookie" to channel.cookie))
            }
        }
        
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .build().apply {
                if (channel.url.isNotEmpty()) {
                    val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(channel.url))
                    
                    val drmUuid = when (channel.licenseType.lowercase()) {
                        "clearkey" -> C.CLEARKEY_UUID
                        "com.widevine.alpha", "widevine" -> C.WIDEVINE_UUID
                        "playready" -> C.PLAYREADY_UUID
                        else -> null
                    }
                    
                    if (drmUuid != null && channel.licenseKey.isNotEmpty()) {
                        mediaItemBuilder.setDrmConfiguration(
                            MediaItem.DrmConfiguration.Builder(drmUuid)
                                .setLicenseUri(channel.licenseKey)
                                .build()
                        )
                    }
                    
                    setMediaItem(mediaItemBuilder.build())
                    prepare()
                    playWhenReady = true
                }
            }
    }

    DisposableEffect(Unit) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window?.attributes = window?.attributes?.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = originalOrientation
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
