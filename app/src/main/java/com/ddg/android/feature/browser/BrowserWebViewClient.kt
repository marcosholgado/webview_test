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
package com.ddg.android.feature.browser

import android.graphics.Bitmap
import android.webkit.*
import com.ddg.android.extension.baseHost
import com.ddg.android.feature.browser.model.BrowserNewPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BrowserWebViewClientListener {
  fun newPageStarted(browserNewPage: BrowserNewPage)
  fun pageFinished(browserNewPage: BrowserNewPage)
  fun resourceBlocked(url: String, resource: String)
}

class BrowserWebViewClient @Inject constructor(
  private val interceptor: BrowserRequestInterceptor
) :
  WebViewClient() {

  var listener: BrowserWebViewClientListener? = null

  override fun shouldInterceptRequest(webView: WebView, request: WebResourceRequest): WebResourceResponse? {
    return runBlocking {
      val response = interceptor.intercept(webView, request)

      val url = withContext(Dispatchers.Main) { webView.url.orEmpty() }
      if (response == BlockedWebResourceResponse) {
        listener?.resourceBlocked(url, request.url?.baseHost.orEmpty())
      }
      return@runBlocking response
    }
  }

  override fun onPageStarted(webView: WebView, url: String, favicon: Bitmap?) {
    listener?.newPageStarted(
      BrowserNewPage(
        webView.copyBackForwardList().currentItem?.url ?: url,
        webView.copyBackForwardList().currentItem?.title.orEmpty()
      )
    )
    super.onPageStarted(webView, url, favicon)
  }

  override fun onPageFinished(webView: WebView, url: String) {
    listener?.pageFinished(
      BrowserNewPage(
        webView.copyBackForwardList().currentItem?.url ?: url,
        webView.copyBackForwardList().currentItem?.title.orEmpty()
      )
    )
    super.onPageFinished(webView, url)
  }
}
