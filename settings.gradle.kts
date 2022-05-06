/*
 * Copyright (c) 2022. Chachako
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In addition, if you fork this project, your forked code file must contain
 * the URL of the original project: https://github.com/chachako/visual-effects
 */
rootProject.name = "visual-effects"

pluginManagement {
  repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.meowool.gradle.toolkit") version "0.1.1-SNAPSHOT"
}

buildscript {
  configurations.all {
    resolutionStrategy {
      cacheChangingModulesFor(0, TimeUnit.DAYS)
      eachDependency {
        // Force Kotlin's version for all dependencies
        if (requested.group == "org.jetbrains.kotlin") useVersion("1.6.21")
      }
    }
  }
}

gradleToolkitWithMeowoolSpec(spec = {
  licenseHeader = rootProject.projectDir.resolve("LICENSE").readLines().joinToString(
    separator = "\n",
    prefix = "/*\n",
    postfix = "\n */"
  ) { " * $it" }
})

importProjects(rootDir)

// Only set in the CI environment, waiting the issue to be fixed:
// https://youtrack.jetbrains.com/issue/KT-48291
if (isCiEnvironment) extra["kotlin.mpp.enableGranularSourceSetsMetadata"] = true
