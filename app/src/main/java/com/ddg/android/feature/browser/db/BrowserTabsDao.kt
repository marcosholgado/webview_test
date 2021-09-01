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

import androidx.room.*
import io.reactivex.Observable

private fun List<Site>.updateTitle(
  newTitle: String,
  block: (Site) -> Boolean
): List<Site> {
  return map {
    if (block(it)) it.copy(title = newTitle) else it
  }
}

fun BrowserTabsDao.newEmptyTab(url: String? = null, title: String? = null): Int {
  val tabs = tabStates()
  val id = if (tabs.isEmpty()) 0 else tabs.last().tabId + 1
  insertTabState(
    BrowserTabsEntity(
      id,
      0,
      listOf(
        Site(
          url = url ?: "about:blank",
          title = title ?: "blank"
        )
      )
    )
  )
  return id
}

fun BrowserTabsDao.newTab(url: String, title: String? = null): Int {
  val id = tabStates().last().tabId + 1
  insertTabState(
    BrowserTabsEntity(
      id,
      0,
      listOf(Site(url = url, title = title ?: url))
    )
  )
  return id
}

@Dao
abstract class BrowserTabsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertTabState(state: BrowserTabsEntity)

  @Query("select * from browser_tabs")
  abstract fun tabStates(): List<BrowserTabsEntity>

  @Query("select * from browser_tabs where tabId = :id")
  abstract fun tab(id: Int): BrowserTabsEntity?

  @Transaction
  open fun updateTitle(id: Int, title: String) {
    val tab = tab(id)
    tab?.let {
      val site = it.currentTabSite()
      val updatedSite = site?.copy(title = title)

      if (updatedSite == null) {
        return@let
      } else {
        val sites = it.site.toMutableList()
        sites[it.currentIndex] = updatedSite
        insertTabState(
          it.copy(
            site = sites
          )
        )
      }
    }
  }

  @Delete
  abstract fun delete(tab: BrowserTabsEntity)

  @Transaction
  open fun deleteTabAndGetAll(tab: BrowserTabsEntity): List<BrowserTabsEntity> {
    delete(tab)
    return tabStates()
  }

  @Query("delete from browser_tabs")
  abstract fun deleteAll()

  @Query("select COUNT(1) from browser_tabs")
  abstract fun count(): Observable<Long>
}
