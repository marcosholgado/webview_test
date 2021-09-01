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

import android.content.Context
import androidx.annotation.WorkerThread
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.ddg.android.feature.browser.db.TrackerEntityDao
import okio.Okio
import timber.log.Timber
import javax.inject.Inject

class TrackerDataSetLoader @Inject constructor(
  private val context: Context,
  private val moshi: Moshi,
  private val trackerBlockListDao: TrackerBlockListDao,
  private val trackerEntityDao: TrackerEntityDao
) {

  private val jsonBlockListJsonAdapter: JsonAdapter<JsonBlockList> by lazy {
    moshi.adapter(JsonBlockList::class.java)
  }

  @WorkerThread
  fun loadData() {
    loadLocalBlockListIntoDb()
  }

  private fun loadLocalBlockListIntoDb() {
    Timber.v("ddg: Loading remote tracker block list")
    val jsonBlockList = loadBlockListFromJsonFile("blocklist.json")
    val blockList = jsonBlockList?.toBlockListEntity()
    val entityList = jsonBlockList?.toEntityList()

    blockList?.let {
      if (trackerBlockListDao.count() == 0) {
        trackerBlockListDao.insert(it)
      }
    }
    entityList?.let {
      if (trackerEntityDao.count() == 0) {
        trackerEntityDao.insert(it)
      }
    }
  }

  private fun loadBlockListFromJsonFile(filename: String): JsonBlockList? {
    val inputStream = context.assets.open(filename)
    val source = Okio.buffer(Okio.source(inputStream))

    val content = source.readUtf8()
    source.close()

    return jsonBlockListJsonAdapter.fromJson(content)
  }
}
