package com.ddg.android.feature.browser.ui

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ddg.android.extension.withScheme
import com.ddg.android.feature.browser.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class BrowserViewModel : ViewModel() {

  private var cachedTab: BrowserTabModel? = null

  fun reduce(event: Browser.Event): Flow<Browser.State> {
    return when (event) {
      Browser.Event.BrowserGoBack -> onBrowserGoBack()
      Browser.Event.BrowserGoForward -> onBrowserGoForward()
      is Browser.Event.BrowserLoadProgress -> onBrowserLoadProgress(event)
      is Browser.Event.BrowserNewPage -> onBrowserNewPage(event)
      is Browser.Event.BrowserQuerySearch -> onBrowserQuerySearch(event)
      Browser.Event.BrowserRefresh -> onBrowserRefresh()
      is Browser.Event.BrowserUpdateTitle -> onBrowserUpdateTitle()
      Browser.Event.BrowserViewInitEvent -> onBrowserViewInitEvent()
    }
  }

  private fun onBrowserGoBack(): Flow<Browser.State> = flow {
    val tab = cachedTab
    val prevUrlItem = tab?.previousSiteItem()
    val prevIndex = tab?.previousIndex()
    Browser.State.NavigateBackViewModel(prevUrlItem?.url).also {
      tab?.let {
        cachedTab = tab.copy(currentIndex = prevIndex!!)
      }
      emit(it)
    }
  }

  private fun onBrowserGoForward(): Flow<Browser.State> = flow {
    val tab = cachedTab

    if (tab == null) {
      emit(Browser.State.LoadUrlViewModel())
    } else {
      val nextUrl = tab.nextUrlItem()?.url
      val nextIndex = tab.nextIndex()
      Browser.State.NavigateForwardViewModel(nextUrl).also {
        cachedTab = tab.copy(currentIndex = nextIndex)
        emit(it)
      }
    }
  }

  private fun onBrowserLoadProgress(event: Browser.Event.BrowserLoadProgress): Flow<Browser.State> = flow {
    Timber.d("Progress ${event.progress}")
  }

  private fun onBrowserNewPage(newPage: Browser.Event.BrowserNewPage): Flow<Browser.State> = flow {
    (cachedTab ?: BrowserTabModel.create(newPage.url, newPage.title)).also {
      val updatedTab = it.updateTab(newPage)
      cachedTab = updatedTab
    }
  }

  private fun onBrowserQuerySearch(event: Browser.Event.BrowserQuerySearch): Flow<Browser.State> = flow {
    if (event.isValidUrl()) {
      val url = event.query.toUri().withScheme().toString()
      Browser.State.LoadUrlViewModel(url = url, title = url)
        .also { emit(it) }
    } else {
      Browser.State.LoadUrlViewModel(
        url = "https://duckduckgo.com/?q=${event.query}&kp=-1&kl=us-en&ko=-2",
        title = event.query
      ).also { emit(it) }
    }

    emit(Browser.State.LoadUrlViewModel())
  }

  private fun onBrowserRefresh(): Flow<Browser.State> = flow {
    val tab = cachedTab

    if (tab == null) {
      emit(Browser.State.LoadUrlViewModel())
    } else {
      val currentSite = tab.currentUrlItem()
      emit(Browser.State.LoadUrlViewModel(currentSite.url, currentSite.title))
    }
  }

  private fun onBrowserUpdateTitle(): Flow<Browser.State> = flow {}

  private fun onBrowserViewInitEvent(): Flow<Browser.State> = flow {
    val tab = cachedTab ?: BrowserTabModel.create().also { cachedTab = it }

    if (tab.site.isEmpty()) {
      emit(Browser.State.LoadUrlViewModel(url = "about:blank"))
    } else {
      emit(Browser.State.LoadUrlViewModel(tab.site[tab.currentIndex].url))
    }
  }
}

@Suppress("UNCHECKED_CAST")
class BrowserViewModelModelFactory : ViewModelProvider.NewInstanceFactory() {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return with(modelClass) {
      when {
        isAssignableFrom(BrowserViewModel::class.java) -> BrowserViewModel()
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
      }
    } as T
  }
}