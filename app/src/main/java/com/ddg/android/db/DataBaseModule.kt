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

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataBaseModule {
  @Singleton
  @Provides
  fun provideAppDataBase(context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
      .build()
  }

  @Singleton
  @Provides
  fun provideTrackerEntityDao(appDatabase: AppDatabase) =
    appDatabase.trackerEntityDao()

  @Singleton
  @Provides
  fun provideTrackerBlockListDao(appDatabase: AppDatabase) =
    appDatabase.trackerBlockListDao()

  @Singleton
  @Provides
  fun provideBrowserTabsDao(appDatabase: AppDatabase) =
    appDatabase.browserTabsDao()
}
