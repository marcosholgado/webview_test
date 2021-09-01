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
import com.ddg.android.extension.baseHost
import com.ddg.android.feature.browser.tracker.Tracker
import com.ddg.android.feature.browser.tracker.TrackerBlocker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.measureNanoTime

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

internal class RealBrowserRequestInterceptor
constructor(private val trackerBlocker: TrackerBlocker) : BrowserRequestInterceptor {
  override fun intercept(webView: WebView, request: WebResourceRequest): WebResourceResponse? {
    return runBlocking {
      // all webview method calls shall be in the same thread
      val documentUrl = withContext(Dispatchers.Main) { webView.url.orEmpty() }

      // if request should be upgraded to HTTPS do it
      val upgradedRequest = upgradeToHttps(request)
      upgradedRequest?.let { return@runBlocking it }

      // see if we need to block the resource
      var hit: Tracker?
      val time = measureNanoTime {
        hit = trackerBlocker.shouldBlock(documentUrl, request.url?.baseHost.orEmpty())
      }
      if (hit != null) {
        Timber.d("Blocked (${time.div(1000)} us): $hit")
        return@runBlocking BlockedWebResourceResponse
      }

      return@runBlocking null
    }
  }

  private fun upgradeToHttps(request: WebResourceRequest): WebResourceResponse? {
    // TODO
    return null
  }
}
