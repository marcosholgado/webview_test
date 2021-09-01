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

import androidx.room.*
import com.ddg.android.feature.browser.db.TrackerEntity

@Entity(tableName = "tracker_block_list")
data class BlockListEntity(
  @PrimaryKey val domain: String,
  val categories: List<String>
)

@Dao
abstract class TrackerBlockListDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insert(blockList: List<BlockListEntity>)

  @Query("select * from tracker_block_list")
  abstract fun blockList(): List<BlockListEntity>

  @Query("select * from tracker_block_list where domain = :domain")
  abstract fun getTracker(domain: String): BlockListEntity?

  @Query("select count(*) from tracker_block_list")
  abstract fun count(): Int
}

/**
 * Data class that represents the Json tracker block list
 */
data class JsonBlockList(
  val trackers: Map<String, JsonTracker>,
  val domains: Map<String, String>,
)

data class JsonTracker(
  val domain: String?,
  val categories: List<String>?,
)

/**
 * Convert the Json representation of the tracker block list into a list
 * of BlockListEntity
 */
fun JsonBlockList.toBlockListEntity(): List<BlockListEntity> {
  return trackers.entries.asSequence()
    .map { entry ->
      BlockListEntity(entry.value.domain ?: entry.key, entry.value.categories.orEmpty())
    }.toList()
}

/**
 * Convert the Json representation of the tracker block list into a list
 * of BlockListEntity
 */
fun JsonBlockList.toEntityList(): List<TrackerEntity> {
  return domains.asSequence()
    .map { TrackerEntity(it.key, it.value) }
    .toList()
}
