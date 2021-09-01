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
package com.ddg.android.feature.browser.db

import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import androidx.room.*
import com.ddg.android.extension.dropSubdomain
import javax.inject.Inject

@Entity(tableName = "tracker_entities")
data class TrackerEntity(
  @PrimaryKey val domain: String,
  val name: String
)

@Dao
abstract class TrackerEntityDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insert(entity: TrackerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insert(entity: List<TrackerEntity>)

  @Query("select * from tracker_entities where domain = :domain")
  abstract fun forDomain(domain: String): TrackerEntity?

  @Query("select count(*) from tracker_entities")
  abstract fun count(): Int

  @Query("select * from tracker_entities where name = :name")
  abstract fun forName(name: String): List<TrackerEntity>
}

class EntityLookupTable @Inject constructor(
  private val trackerEntityDao: TrackerEntityDao
) {

  @WorkerThread
  fun entityForDomain(domain: String): TrackerEntity? {
    val entity = trackerEntityDao.forDomain(domain)
    if (entity != null) return entity

    val subdomain = domain.toUri().dropSubdomain() ?: return null
    return entityForDomain(subdomain)
  }

  @WorkerThread
  fun entitiesForName(name: String): List<TrackerEntity> {
    return trackerEntityDao.forName(name)
  }
}
