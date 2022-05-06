import org.jetbrains.compose.compose

plugins {
  id(Plugins.Jetbrains.Compose)
}

androidApp {
  namespace = "chachako.visual.effects.showcase"
  applicationId(namespace!!)
  versionName("1.0")
  versionCode(1)
}

commonTarget {
  main.dependencies {
    implementationOf(
      compose.runtime,
      project(Projects.Library)
    )
  }
}

androidTarget {
  main.dependencies {
    implementationOf(
      Libs.AndroidX.Core.Ktx,
      Libs.AndroidX.Activity.Ktx,
      Libs.AndroidX.Activity.Compose,
      Libs.AndroidX.Lifecycle.ViewModel.Ktx,
      Libs.Coil.Kt.Coil.Compose,
      Libs.Meowool.Toolkit.Sweekt,
      Libs.Google.Android.Exoplayer,
      Libs.Google.Accompanist.Pager,
      Libs.Google.Accompanist.Pager.Indicators,
      Libs.Google.Accompanist.Systemuicontroller,
    )
  }
}

compose {
  android {
    useAndroidX = true
    androidxVersion = "1.2.0-alpha08"
  }
}

// Ignore all warnings for experimental APIs
optIn(
  "androidx.compose.material.ExperimentalMaterialApi",
  "com.google.accompanist.pager.ExperimentalPagerApi",
)