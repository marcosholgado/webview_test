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
package com.ddg.android.feature.browser.model

import android.util.Patterns
import android.view.MenuItem

sealed class BrowserViewEvent

object BrowserViewInitEvent : BrowserViewEvent()
data class BrowserLoadProgress(val progress: Int) : BrowserViewEvent()
data class BrowserQuerySearch(val query: String) : BrowserViewEvent()
data class BrowserNewPage(
  val url: String,
  val title: String
) : BrowserViewEvent()
object BrowserRefresh : BrowserViewEvent()
object BrowserGoBack : BrowserViewEvent()
object BrowserGoForward : BrowserViewEvent()
data class BrowserUpdateTitle(val title: String = "") : BrowserViewEvent()
data class BrowserMenuItemClicked(val menuItem: MenuItem) : BrowserViewEvent()

// some extension funs
fun BrowserQuerySearch.isValidUrl(): Boolean {
  return Patterns.WEB_URL.matcher(query).matches()
}
