<p class="center">
  <img alt='BlurKit Header' src='.art/logo.png' />
</p>

### ***Work in progress!***

**This project is completely experimental right now, it's far from production usable, and it still has a lot of bugs to be fixed.**

https://user-images.githubusercontent.com/58068445/167137785-663904e2-c443-4e11-933f-e5b35d35902c.mp4



## Disclaimer

Originally born as a concept project just to validate the implementation of real-time blur effect in **Jetpack Compose**, as you can see in the video, it was not ready to be put into a production environment as a library.

There are still a few things that need to be addressed to accomplish this feat, and once they are done, the project will be able to give birth to a lot of interesting effects, like [pixelation](https://en.wikipedia.org/wiki/Pixelation).



## Improvements

- **Fix flickering.** The problem probably occurs in large part because **recomposition** happens all the time and it takes a bit of finesse to get rid of it, maybe something like `Flow.debounce` or `Flow.distinctUntilChanged`?
- **Use with [accompanist's](https://github.com/google/accompanist) `Pager`.** The problem is [here in Slack](https://kotlinlang.slack.com/archives/CJLTWPH7S/p1651522733995939).
- **Draw the contents from the `SurfaceView` and `TextureView`.** This should be solved by traversing the `ViewGroup` tree to draw each view (refers ), but not sure if this will affect performance.
- **A way to be able to observe changes of some composable content**. The current `view.isDirty` is not available for `Modifier.graphicsLayers` and requires more practice here.
- **Support `RenderEffect` and more.**

> In addition, for the reasons and performance issues regarding the birth of this library see [this thread](https://kotlinlang.slack.com/archives/CJLTWPH7S/p1651861216776359?thread_ts=1651845634.528419&cid=CJLTWPH7S), [@romainguy](https://github.com/romainguy) explains it very well 🙂.

## Contribution

Any PRs are always welcome, and I hope more people will come and work with me to complete and improve this project, which was the original purpose of pushing this project to Github, since I'm not a graphics master 😉.
