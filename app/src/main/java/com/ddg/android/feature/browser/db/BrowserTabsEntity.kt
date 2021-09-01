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
package com.ddg.android.feature.browser.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.ddg.android.feature.browser.model.BrowserNewPage
import com.ddg.android.network.NetworkModule

data class Site(
  val url: String,
  val title: String
)

@Entity(tableName = "browser_tabs")
data class BrowserTabsEntity(
  @PrimaryKey val tabId: Int,
  val currentIndex: Int,
  val site: List<Site>,
  val base64Snapshot: String? = null
)

class SiteItemTypeConverter {
  @TypeConverter
  fun toSiteItem(json: String): List<Site> {
    return Adapters.siteAdapter.fromJson(json)!!
  }

  @TypeConverter
  fun fromSiteItem(item: List<Site>): String {
    return Adapters.siteAdapter.toJson(item)
  }
}

// Extension functions
fun BrowserTabsEntity.canGoBack(): Boolean {
  return site.isNotEmpty() && currentIndex > 0
}

fun BrowserTabsEntity.previousSiteItem(): Site? {
  return if (canGoBack()) site[currentIndex - 1] else null
}

fun BrowserTabsEntity.previousIndex(): Int {
  return (currentIndex - 1).coerceAtLeast(0)
}

fun BrowserTabsEntity.lastTabIsCurrent(): Boolean {
  return site.isNotEmpty() && site.size == (currentIndex - 1)
}

fun BrowserTabsEntity.canGoForward(): Boolean {
  return site.isNotEmpty() && site.size > (currentIndex + 1)
}

fun BrowserTabsEntity.currentUrlItem(): Site {
  site[currentIndex].let { return it }
}

fun BrowserTabsEntity.nextUrlItem(): Site? {
  return if (canGoForward()) {
    site[currentIndex + 1]
  } else null
}

fun BrowserTabsEntity.nextIndex(): Int {
  return (currentIndex + 1).coerceAtMost(site.size - 1)
}

fun BrowserTabsEntity.currentTabSite(): Site? {
  return when {
    currentIndex < site.size -> site[currentIndex]
    else -> null
  }
}

fun BrowserTabsEntity.updateTab(newPage: BrowserNewPage): BrowserTabsEntity {
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

// DB adapters
class Adapters {
  companion object {
    private val moshi = NetworkModule().provideMoshi()
    private val siteListType = Types.newParameterizedType(List::class.java, Site::class.java)
    val siteAdapter: JsonAdapter<List<Site>> = moshi.adapter(siteListType)
  }
}
