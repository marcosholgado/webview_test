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
import com.ddg.android.feature.browser.model.BrowserAction
import com.jakewharton.rxrelay2.PublishRelay
import com.mancj.materialsearchbar.MaterialSearchBar
import com.ddg.android.R
import com.ddg.android.extension.hideKeyboard
import com.ddg.android.feature.browser.BrowserWebViewClient
import com.ddg.android.feature.browser.BrowserWebViewClientListener
import com.ddg.android.feature.browser.model.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.browser_view.view.*
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
  private val eventRelay: PublishRelay<BrowserViewEvent> = PublishRelay.create()

  private var disposable: Disposable? = null

  lateinit var presenter: ObservableTransformer<BrowserViewEvent, BrowserViewModel>

  lateinit var browserWebViewClient: BrowserWebViewClient

  private var popupMenu: CascadePopupMenu? = null

  var listener: BrowserViewListener? = null

  val url: String
    get() = root.webview.url.orEmpty()

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    configureWebView()
    configureOverflowMenu()

    disposable = events()
      .compose(presenter)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(::render)
  }

  private fun configureOverflowMenu() {
    popupMenu?.let { return }
    popupMenu = CascadePopupMenu(context, root.overflow_menu)
    popupMenu?.inflate(R.menu.browser_menu)
    popupMenu?.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.refresh -> eventRelay.accept(BrowserRefresh)
        R.id.back -> eventRelay.accept(BrowserGoBack)
        R.id.forward -> eventRelay.accept(BrowserGoForward)
        else -> Timber.w("Unknown menu item")
      }
      true
    }
    root.overflow_menu.setOnClickListener { popupMenu?.show() }
  }

  private fun configureWebView() {
    browserWebViewClient.listener = object : BrowserWebViewClientListener {
      override fun newPageStarted(browserNewPage: BrowserNewPage) {
        eventRelay.accept(browserNewPage)
      }

      override fun pageFinished(browserNewPage: BrowserNewPage) {
        eventRelay.accept(BrowserUpdateTitle(browserNewPage.title))
      }

      override fun resourceBlocked(url: String, resource: String) {}
    }
    root.webview.webViewClient = browserWebViewClient
    root.webview.settings.run {
      javaScriptEnabled = true
      databaseEnabled = true
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    disposable?.dispose()
  }

  private fun events(): Observable<BrowserViewEvent> {
    return Observable.merge(
      eventRelay,
      Observable.create { emitter ->
        root.webview.webChromeClient = object : WebChromeClient() {
          override fun onProgressChanged(view: WebView?, newProgress: Int) {
            emitter.onNext(BrowserLoadProgress(newProgress))
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
              emitter.onNext(BrowserQuerySearch(text?.toString().orEmpty()))
            }
          }
        )
      }
    )
  }

  private fun render(model: BrowserViewModel) {
    when {
      model is LoadUrlViewModel && model.isEqualTo(root.webview.url) -> { /* noop */ }
      model is LoadUrlViewModel && model.url != null -> {
        root.webview.loadUrl(model.url)
      }
      model is NavigateBackViewModel && model.url != null -> root.webview.loadUrl(model.url)
      model is NavigateBackViewModel && model.url == null -> listener?.onDetach()
      model is NavigateForwardViewModel && model.url != null -> root.webview.loadUrl(model.url)
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  private fun refresh() {
    action(BrowserAction.Refresh)
  }

  fun action(action: BrowserAction) {
    when (action) {
      BrowserAction.NavigateBack -> eventRelay.accept(BrowserGoBack)
      BrowserAction.NavigateForward -> eventRelay.accept(BrowserGoForward)
      BrowserAction.Refresh -> eventRelay.accept(BrowserViewInitEvent)
      is BrowserAction.NewTab -> TODO()
    }
  }
}
