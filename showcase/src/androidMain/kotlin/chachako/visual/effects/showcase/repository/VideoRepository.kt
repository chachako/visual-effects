package chachako.visual.effects.showcase.repository

import android.content.res.AssetManager
import android.net.Uri

/**
 * @author Chachako
 */
object VideoRepository {
  fun list(assets: AssetManager): List<Uri> = assets.list("videos")!!.map {
    Uri.parse("file:///android_asset/videos/$it")
  }
}