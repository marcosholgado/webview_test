package com.ddg.android.feature.browser.model

import android.util.Patterns

object Browser {
  sealed class Event {
    object BrowserViewInitEvent : Event()
    data class BrowserLoadProgress(val progress: Int) : Event()
    data class BrowserQuerySearch(val query: String) : Event()
    data class BrowserNewPage(
      val url: String,
      val title: String
    ) : Event()
    object BrowserRefresh : Event()
    object BrowserGoBack : Event()
    object BrowserGoForward : Event()
    data class BrowserUpdateTitle(val title: String = "") : Event()
  }

  sealed class State {
    data class LoadUrlViewModel(val url: String? = null, val title: String? = null) : State()
    data class NavigateBackViewModel(val url: String? = null) : State()
    data class NavigateForwardViewModel(val url: String? = null) : State()
  }

  sealed class Action {
    object NavigateBack : Action()
    object Refresh : Action()
    data class NewTab(val url: String? = null) : Action()
    object NavigateForward : Action()
  }
}

// some extension funs
fun Browser.Event.BrowserQuerySearch.isValidUrl(): Boolean {
  return Patterns.WEB_URL.matcher(query).matches()
}

fun Browser.State.LoadUrlViewModel.isEqualTo(otherUrl: String?): Boolean {
  return url != null &&
      otherUrl != null &&
      url.isNotBlank() &&
      otherUrl.isNotBlank() &&
      url == otherUrl
}

