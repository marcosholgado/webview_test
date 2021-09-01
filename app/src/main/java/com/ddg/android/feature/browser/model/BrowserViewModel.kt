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

sealed class BrowserViewModel

data class LoadUrlViewModel(val url: String? = null, val title: String? = null) : BrowserViewModel()
data class NavigateBackViewModel(val url: String? = null) : BrowserViewModel()
data class NavigateForwardViewModel(val url: String? = null) : BrowserViewModel()

fun LoadUrlViewModel.isEqualTo(otherUrl: String?): Boolean {
  return url != null &&
    otherUrl != null &&
    url.isNotBlank() &&
    otherUrl.isNotBlank() &&
    url == otherUrl
}
