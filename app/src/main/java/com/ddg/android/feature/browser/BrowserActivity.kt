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

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ddg.android.R
import com.ddg.android.extension.extraText
import com.ddg.android.feature.browser.model.Browser
import com.ddg.android.feature.browser.ui.*
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowserActivity : AppCompatActivity(R.layout.activity_main) {

  @Inject lateinit var browserWebViewClient: BrowserWebViewClient
  @Inject lateinit var viewModelFactory: BrowserViewModelModelFactory

  private val viewModelModel: BrowserViewModel by lazy {
    ViewModelProvider(this, viewModelFactory).get(BrowserViewModel::class.java)
  }
  private lateinit var browserView: BrowserView
  private var startUpUrl: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    AndroidInjection.inject(this)

    browserView = findViewById(R.id.browser_view)
    browserView.browserWebViewClient = browserWebViewClient
    browserView.listener = object : BrowserViewListener {
      override fun onDetach() {
        finish()
      }
    }
    lifecycle.addObserver(browserView)

    startUpUrl = if (savedInstanceState == null) intent?.extraText else null
  }

  override fun onStart() {
    super.onStart()

    lifecycleScope.launch {
      browserView.events()
        .flatMapLatest { viewModelModel.reduce(it) }
        .collect(browserView::render)
    }
  }

  override fun onResume() {
    super.onResume()

    if (startUpUrl != null) {
      browserView.action(Browser.Action.NewTab(startUpUrl))
      startUpUrl = null
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    intent?.extraText?.let {
      browserView.action(Browser.Action.NewTab(it))
    }
  }

  override fun onBackPressed() {
    browserView.action(Browser.Action.NavigateBack)
  }
}
