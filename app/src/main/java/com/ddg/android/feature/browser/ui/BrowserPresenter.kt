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
package com.ddg.android.feature.browser.ui

import androidx.core.net.toUri
import com.afollestad.rxkprefs.Pref
import com.ddg.android.extension.filterIsInstance
import com.ddg.android.extension.toHttps
import com.ddg.android.extension.withScheme
import com.ddg.android.feature.browser.db.*
import com.ddg.android.feature.browser.model.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

private fun Pref<Int>.reset() {
  this.set(0)
}

class BrowserPresenter(
  private val browserTabsDao: BrowserTabsDao,
  private val currentTab: Pref<Int>,
) : ObservableTransformer<BrowserViewEvent, BrowserViewModel> {

  private fun Observable<BrowserViewInitEvent>.handleInit(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .map {
        val tab = safeGetCurrentTabOrFirstOrNull()
        if (tab == null) {
          browserTabsDao.newEmptyTab().also {
            currentTab.set(it)
          }
        }
        safeGetCurrentTabOrFirstOrNull()
      }
      .observeOn(AndroidSchedulers.mainThread())
      .map { tabNavigationState ->
        if (tabNavigationState.site.isEmpty()) {
          LoadUrlViewModel(url = "about:blank")
        } else {
          LoadUrlViewModel(tabNavigationState.site[tabNavigationState.currentIndex].url)
        }
      }
  }

  private fun Observable<BrowserLoadProgress>.handleProgress(): Observable<BrowserViewModel> {
    return doOnNext { event ->
      Timber.d("Progress ${event.progress}")
    }
      .ignoreElements()
      .toObservable()
  }

  private fun Observable<BrowserQuerySearch>.handleQuery(): Observable<BrowserViewModel> {
    return this.map {
      if (it.isValidUrl()) {
        val url = it.query.toUri().withScheme().toHttps.toString()
        LoadUrlViewModel(url = url, title = url)
      } else {
        LoadUrlViewModel(
          url = "https://duckduckgo.com/?q=${it.query}&kp=-1&kl=us-en&ko=-2",
          title = it.query
        )
      }
    }
  }

  private fun Observable<BrowserNewPage>.handleNewPage(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .doOnNext { newPage ->
        // get the stored state for all tabs
        val tabStates = browserTabsDao.tabStates()
        // if we don't have tabs, start with a clean one
        when {
          tabStates.isEmpty() -> {
            browserTabsDao.newEmptyTab(newPage.url, newPage.title).also {
              currentTab.set(it)
            }
          }
          else -> {
            // get the current tab
            val tab = browserTabsDao.tab(currentTab.get())
            if (tab == null) {
              // it does not exist, first page in tab
              browserTabsDao.insertTabState(
                BrowserTabsEntity(
                  currentTab.get(),
                  0,
                  listOf(Site(newPage.url, newPage.title))
                )
              )
            } else {
              // update tab
              val updatedTab = tab.updateTab(newPage)
              browserTabsDao.insertTabState(updatedTab)
            }
          }
        }
      }
      .ignoreElements()
      .toObservable()
  }

  private fun Observable<BrowserGoBack>.handleGoBack(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .map {
        val currentTabId = currentTab.get()
        val currentTab = browserTabsDao.tab(currentTabId)
        val prevUrlItem = currentTab?.previousSiteItem()
        val prevIndex = currentTab?.previousIndex()
        return@map NavigateBackViewModel(prevUrlItem?.url).also {
          currentTab?.let {
            browserTabsDao.insertTabState(
              currentTab.copy(currentIndex = prevIndex!!)
            )
          }
        }
      }
  }

  private fun Observable<BrowserGoForward>.handleGoForward(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .map {
        val tabs = browserTabsDao.tabStates()
        if (tabs.isEmpty()) {
          return@map LoadUrlViewModel()
        }
        val currentTabId = currentTab.get()
        val currentTab = tabs[currentTabId]
        val nextUrl = currentTab.nextUrlItem()?.url
        val nextIndex = currentTab.nextIndex()
        return@map NavigateForwardViewModel(nextUrl).also {
          browserTabsDao.insertTabState(
            currentTab.copy(currentIndex = nextIndex)
          )
        }
      }
  }

  private fun Observable<BrowserRefresh>.handleRefresh(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .map {
        val tabs = browserTabsDao.tabStates()
        if (tabs.isEmpty()) {
          // this should noop
          return@map LoadUrlViewModel()
        }
        val currentTabId = currentTab.get()
        val currentTab = tabs[currentTabId]
        val currentSite = currentTab.currentUrlItem()
        return@map LoadUrlViewModel(currentSite.url, currentSite.title)
      }
  }

  private fun Observable<BrowserUpdateTitle>.handleTitleUpdate(): Observable<BrowserViewModel> {
    return this
      .observeOn(Schedulers.single())
      .doOnNext { model ->
        browserTabsDao.updateTitle(currentTab.get(), model.title)
      }
      .ignoreElements()
      .toObservable()
  }

  private fun safeGetCurrentTabOrFirstOrNull(): BrowserTabsEntity? {
    val tab = browserTabsDao.tab(currentTab.get())
    return if (tab == null) {
      // try the first tab
      currentTab.reset()
      val tabs = browserTabsDao.tabStates()
      if (tabs.isEmpty()) {
        null
      } else {
        tabs[0]
      }
    } else {
      tab
    }
  }

  override fun apply(events: Observable<BrowserViewEvent>): ObservableSource<BrowserViewModel> {
    return events.publish { e ->
      Observable.merge(
        listOf(
          e.filterIsInstance<BrowserViewInitEvent>().handleInit(),
          e.filterIsInstance<BrowserLoadProgress>().handleProgress(),
          e.filterIsInstance<BrowserQuerySearch>().handleQuery(),
          e.filterIsInstance<BrowserNewPage>().handleNewPage(),
          e.filterIsInstance<BrowserGoBack>().handleGoBack(),
          e.filterIsInstance<BrowserGoForward>().handleGoForward(),
          e.filterIsInstance<BrowserRefresh>().handleRefresh(),
          e.filterIsInstance<BrowserUpdateTitle>().handleTitleUpdate(),
        )
      )
    }
  }
}
