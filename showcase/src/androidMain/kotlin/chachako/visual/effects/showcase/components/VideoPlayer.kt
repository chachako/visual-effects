package chachako.visual.effects.showcase.components

import android.net.Uri
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.mp4.FragmentedMp4Extractor
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import com.google.android.exoplayer2.ui.StyledPlayerView

/**
 * State of the [VideoPlayer] composable.
 *
 * @author Chachako
 */
@Stable
class VideoPlayerState(val inner: ExoPlayer) {

  /**
   * Whether the video player is playing.
   */
  val isPlaying: Boolean get() = inner.isPlaying

  /**
   * Start the video player.
   */
  fun start() = inner.play()

  /**
   * Pause the video player.
   */
  fun pause() = inner.pause()

  /**
   * Play the next video.
   */
  fun next() = when (inner.nextMediaItemIndex) {
    C.INDEX_UNSET -> inner.seekToDefaultPosition(
      inner.currentTimeline.getFirstWindowIndex(inner.shuffleModeEnabled)
    )
    else -> inner.seekToNextMediaItem()
  }

  /**
   * Play the previous video.
   */
  fun previous() = when (inner.previousMediaItemIndex) {
    C.INDEX_UNSET -> inner.seekToDefaultPosition(
      inner.currentTimeline.getLastWindowIndex(inner.shuffleModeEnabled)
    )
    else -> inner.seekToPreviousMediaItem()
  }
}

/**
 * Remembers a new [VideoPlayer] with the given list of [videos].
 */
@Composable
fun rememberVideoPlayerState(
  videos: List<Uri>,
  prepare: Boolean = true,
  autoPlay: Boolean = true,
): VideoPlayerState {
  val context = LocalContext.current
  val player = remember(videos) {
    val extractorsFactory = DefaultExtractorsFactory()
      .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
      .setFragmentedMp4ExtractorFlags(FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
    val mediaSourceFactory = DefaultMediaSourceFactory(context, extractorsFactory)
    val inner = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build().apply {
      volume = 0f
      shuffleModeEnabled = true
      repeatMode = REPEAT_MODE_ONE
      setMediaItems(videos.map(MediaItem::fromUri), true)
      if (prepare) prepare()
    }
    VideoPlayerState(inner)
  }

  DisposableEffect(videos) {
    if (autoPlay) player.inner.play()

    // Just stop playing when the directory key is changed instead
    //   of releasing the player
    onDispose { player.inner.stop() }
  }

  DisposableEffect(player) {
    // Safely released here when the component is removed
    onDispose { player.inner.release() }
  }

  return player
}


/**
 * A player component that can be used to play videos.
 *
 * @author Chachako
 */
@Composable
fun VideoPlayer(state: VideoPlayerState, modifier: Modifier = Modifier) = AndroidView(
  factory = {
    StyledPlayerView(it).apply {
      useController = false
      resizeMode = RESIZE_MODE_ZOOM
      setKeepContentOnPlayerReset(true)
    }
  },
  modifier,
) { it.player = state.inner }
