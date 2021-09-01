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
package com.ddg.android.feature.browser.tracker

import androidx.annotation.WorkerThread

interface TrackerBlocker {
  /**
   * Returns whether the resource requested should be blocked
   *
   * @param url the URL that is loading
   * @param resource the requested resource
   *
   * @returns a Tracker when the provided resource should be blocked, else
   * it will return <code>null</code>
   */
  @WorkerThread
  fun shouldBlock(url: String, resource: String): Tracker?
}

data class Tracker(val categories: List<String>, val resource: String)

internal class RealTrackerBlocker : TrackerBlocker {

  override fun shouldBlock(url: String, resource: String): Tracker? {
    // TODO
    return null
  }
}
