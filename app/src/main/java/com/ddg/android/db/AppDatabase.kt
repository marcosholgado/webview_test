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
package com.ddg.android.db

import androidx.room.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.ddg.android.feature.browser.db.BrowserTabsDao
import com.ddg.android.feature.browser.db.BrowserTabsEntity
import com.ddg.android.feature.browser.db.SiteItemTypeConverter
import com.ddg.android.feature.browser.db.TrackerEntity
import com.ddg.android.feature.browser.db.TrackerEntityDao
import com.ddg.android.feature.browser.tracker.BlockListEntity
import com.ddg.android.feature.browser.tracker.TrackerBlockListDao
import com.ddg.android.network.NetworkModule

class StringListConverter {
  @TypeConverter
  fun toList(json: String): List<String> {
    return Adapters.stringListAdapter.fromJson(json)!!
  }

  @TypeConverter
  fun fromList(item: List<String>): String {
    return Adapters.stringListAdapter.toJson(item)
  }
}

class Adapters {
  companion object {
    private val moshi = NetworkModule().provideMoshi()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    val stringListAdapter: JsonAdapter<List<String>> = moshi.adapter(stringListType)
  }
}

@Database(
  exportSchema = false,
  version = 1,
  entities = [
    TrackerEntity::class,
    BlockListEntity::class,
    BrowserTabsEntity::class
  ]
)
@TypeConverters(
  StringListConverter::class,
  SiteItemTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trackerBlockListDao(): TrackerBlockListDao
  abstract fun trackerEntityDao(): TrackerEntityDao
  abstract fun browserTabsDao(): BrowserTabsDao
}
