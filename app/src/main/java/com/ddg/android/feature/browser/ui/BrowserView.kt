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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mancj.materialsearchbar.MaterialSearchBar
import com.ddg.android.R
import com.ddg.android.extension.hideKeyboard
import com.ddg.android.feature.browser.BrowserWebViewClient
import com.ddg.android.feature.browser.BrowserWebViewClientListener
import com.ddg.android.feature.browser.model.*
import kotlinx.android.synthetic.main.browser_view.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.saket.cascade.CascadePopupMenu
import timber.log.Timber

interface BrowserViewListener {
  fun onDetach()
}

class BrowserView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr, defStyleRes), LifecycleObserver {

  private val root: View by lazy {
    LayoutInflater.from(context).inflate(R.layout.browser_view, this, true)
  }
  private val events = MutableSharedFlow<Browser.Event>()

  lateinit var browserWebViewClient: BrowserWebViewClient

  private var popupMenu: CascadePopupMenu? = null

  var listener: BrowserViewListener? = null

  val url: String
    get() = root.webview.url.orEmpty()

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    configureWebView()
    configureOverflowMenu()
  }

  private fun configureOverflowMenu() {
    popupMenu?.let { return }
    popupMenu = CascadePopupMenu(context, root.overflow_menu)
    popupMenu?.inflate(R.menu.browser_menu)
    popupMenu?.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.refresh -> emitEvent(Browser.Event.BrowserRefresh)
        R.id.back -> emitEvent(Browser.Event.BrowserGoBack)
        R.id.forward -> emitEvent(Browser.Event.BrowserGoForward)
        else -> Timber.w("Unknown menu item")
      }
      true
    }
    root.overflow_menu.setOnClickListener { popupMenu?.show() }
  }

  private fun configureWebView() {
    browserWebViewClient.listener = object : BrowserWebViewClientListener {
      override fun newPageStarted(browserNewPage: Browser.Event.BrowserNewPage) {
        emitEvent(browserNewPage)
      }

      override fun pageFinished(browserNewPage: Browser.Event.BrowserNewPage) {
        emitEvent(Browser.Event.BrowserUpdateTitle(browserNewPage.title))
      }

      override fun resourceBlocked(url: String, resource: String) {}
    }
    root.webview.webViewClient = browserWebViewClient
    root.webview.settings.run {
      javaScriptEnabled = true
      databaseEnabled = true
    }

    root.webview.webChromeClient = object : WebChromeClient() {
      override fun onProgressChanged(view: WebView?, newProgress: Int) {
        emitEvent(Browser.Event.BrowserLoadProgress(newProgress))
      }
    }

    root.searchBar.setOnSearchActionListener(
      object : MaterialSearchBar.OnSearchActionListener {
        override fun onButtonClicked(buttonCode: Int) {
        }

        override fun onSearchStateChanged(enabled: Boolean) {
        }

        override fun onSearchConfirmed(text: CharSequence?) {
          hideKeyboard()
          emitEvent(Browser.Event.BrowserQuerySearch(text?.toString().orEmpty()))
        }
      }
    )
  }

  private fun emitEvent(event: Browser.Event) {
    GlobalScope.launch { events.emit(event) }
  }

  fun events(): Flow<Browser.Event> = events.asSharedFlow()

  fun render(model: Browser.State) {
    when {
      model is Browser.State.LoadUrlViewModel && model.isEqualTo(root.webview.url) -> { /* noop */ }
      model is Browser.State.LoadUrlViewModel && model.url != null -> {
        root.webview.loadUrl(model.url)
      }
      model is Browser.State.NavigateBackViewModel && model.url != null -> root.webview.loadUrl(model.url)
      model is Browser.State.NavigateBackViewModel && model.url == null -> listener?.onDetach()
      model is Browser.State.NavigateForwardViewModel && model.url != null -> root.webview.loadUrl(model.url)
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  private fun refresh() {
    action(Browser.Action.Refresh)
  }

  fun action(action: Browser.Action) {
    when (action) {
      Browser.Action.NavigateBack -> emitEvent(Browser.Event.BrowserGoBack)
      Browser.Action.NavigateForward -> emitEvent(Browser.Event.BrowserGoForward)
      Browser.Action.Refresh -> emitEvent(Browser.Event.BrowserViewInitEvent)
      is Browser.Action.NewTab -> TODO()
    }
  }
}
