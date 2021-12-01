/*
 * Copyright (C) 2020. DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ddg.android.feature.browser.model

data class Site(
  val url: String,
  val title: String
)

data class BrowserTabModel(
  val currentIndex: Int,
  val site: List<Site>,
) {

  companion object {
    fun create(url: String? = null, title: String? = null): BrowserTabModel {
      return BrowserTabModel(0, listOf(Site(url = url ?: "about:blank", title = title ?: "blank")))
    }
  }
}

// Extension functions
fun BrowserTabModel.canGoBack(): Boolean {
  return site.isNotEmpty() && currentIndex >= 0
}

fun BrowserTabModel.previousSiteItem(): Site? {
  return if (canGoBack()) site[previousIndex()] else null
}

fun BrowserTabModel.previousIndex(): Int {
  return (currentIndex - 1).coerceAtLeast(0)
}

fun BrowserTabModel.lastTabIsCurrent(): Boolean {
  return site.isNotEmpty() && site.size == (currentIndex - 1)
}

fun BrowserTabModel.canGoForward(): Boolean {
  return site.isNotEmpty() && site.size > (currentIndex + 1)
}

fun BrowserTabModel.currentUrlItem(): Site {
  site[currentIndex].let { return it }
}

fun BrowserTabModel.nextUrlItem(): Site? {
  return if (canGoForward()) {
    site[currentIndex + 1]
  } else null
}

fun BrowserTabModel.nextIndex(): Int {
  return (currentIndex + 1).coerceAtMost(site.size - 1)
}

fun BrowserTabModel.currentTabSite(): Site? {
  return when {
    currentIndex < site.size -> site[currentIndex]
    else -> null
  }
}

fun BrowserTabModel.updateTab(newPage: Browser.Event.BrowserNewPage): BrowserTabModel {
  val currentSiteItem = currentTabSite()
    ?: return this.copy(currentIndex = 0, site = listOf(Site(newPage.url, newPage.title)))

  // newPage is the same as current one, do nothing
  if (currentSiteItem.url == newPage.url) {
    return this
  }

  // current tab is the last in the stack, add new one and update
  return if (lastTabIsCurrent()) {
    this.copy(
      site = site.toMutableList().also { it.add(Site(newPage.url, newPage.title)) },
      currentIndex = site.size - 1
    )
  } else {
    // default: current tab is in the middle of the stack
    // insert and clear trail elements
    this.copy(
      site = site.toMutableList().subList(0, currentIndex + 1).also {
        it.add(Site(newPage.url, newPage.title))
      },
      currentIndex = currentIndex + 1
    )
  }
}
