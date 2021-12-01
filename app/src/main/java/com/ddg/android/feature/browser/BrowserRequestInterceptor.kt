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

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.WorkerThread

interface BrowserRequestInterceptor {
  /**
   * Intercepts the request and performs actinos like;
   * - upgrade to HTTPS if necessary
   * - block trackers
   *
   * @param webView the webview
   * @param request the resource request
   *
   * @return a new resource response if intercepted, else null
   */
  @WorkerThread
  fun intercept(
    webView: WebView,
    request: WebResourceRequest
  ): WebResourceResponse?
}

object BlockedWebResourceResponse : WebResourceResponse(null, null, null)

internal class RealBrowserRequestInterceptor : BrowserRequestInterceptor {
  override fun intercept(webView: WebView, request: WebResourceRequest): WebResourceResponse? {

    // TODO: implement

    return null
  }

}
