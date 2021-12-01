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
package com.ddg.android.feature.browser.di

import dagger.Module
import dagger.Provides
import com.ddg.android.feature.browser.BrowserActivity
import com.ddg.android.feature.browser.BrowserRequestInterceptor
import com.ddg.android.feature.browser.RealBrowserRequestInterceptor
import com.ddg.android.feature.browser.ui.BrowserView
import com.ddg.android.feature.browser.ui.BrowserViewModelModelFactory
import dagger.android.ContributesAndroidInjector
import javax.inject.Singleton

@Module
abstract class BrowserComponentModule {
  @ContributesAndroidInjector
  abstract fun browserActivity() : BrowserActivity
}

@Module
class BrowserModule {

  @Provides
  @Singleton
  fun provideBrowserRequestInterceptor(): BrowserRequestInterceptor =
    RealBrowserRequestInterceptor()

  @Provides
  @Singleton
  fun provideBrowserViewModelModelFactory(): BrowserViewModelModelFactory {
    return BrowserViewModelModelFactory()
  }

  @Provides
  @Singleton
  fun provideBrowserView(activity: BrowserActivity): BrowserView {
    return BrowserView(activity)
  }
}
