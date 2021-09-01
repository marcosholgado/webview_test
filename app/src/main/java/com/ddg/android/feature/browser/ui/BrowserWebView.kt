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
package com.ddg.android.feature.browser.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

class BrowserWebView : WebView, NestedScrollingChild {

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    isNestedScrollingEnabled = true
  }

  private val childHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)

  private var nestedOffsetY: Int = 0
  private var lastY: Int = 0
  private val scrollOffset = IntArray(2)
  private val scrollConsumed = IntArray(2)

  override fun onTouchEvent(ev: MotionEvent?): Boolean {
    var consumed = false

    val event = MotionEvent.obtain(ev)
    val action = event.action
    if (action == ACTION_DOWN) {
      nestedOffsetY = 0
    }
    event.offsetLocation(0f, nestedOffsetY.toFloat())
    when (action) {
      ACTION_MOVE -> {
        var deltaY = lastY - event.y

        if (dispatchNestedPreScroll(0, deltaY.toInt(), scrollConsumed, scrollOffset)) {
          deltaY -= scrollConsumed[1]
          lastY = (event.y - scrollOffset[1]).toInt()
          event.offsetLocation(0f, -scrollOffset[1].toFloat())
          nestedOffsetY += scrollOffset[1]
        }

        consumed = super.onTouchEvent(event)

        if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY.toInt(), scrollOffset)) {
          event.offsetLocation(0f, scrollOffset[1].toFloat())
          nestedOffsetY += scrollOffset[1]
          lastY -= scrollOffset[1]
        }
      }
      ACTION_DOWN -> {
        consumed = super.onTouchEvent(event)

        lastY = event.y.toInt()
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
      }
      ACTION_UP, ACTION_CANCEL -> {
        consumed = super.onTouchEvent(event)

        stopNestedScroll()
      }
    }

    return consumed
  }

  override fun setNestedScrollingEnabled(enabled: Boolean) {
    childHelper.isNestedScrollingEnabled = enabled
  }

  override fun isNestedScrollingEnabled(): Boolean {
    return childHelper.isNestedScrollingEnabled
  }

  override fun startNestedScroll(axes: Int): Boolean {
    return childHelper.startNestedScroll(axes)
  }

  override fun stopNestedScroll() {
    childHelper.stopNestedScroll()
  }

  override fun hasNestedScrollingParent(): Boolean {
    return childHelper.hasNestedScrollingParent()
  }

  override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
    return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
  }

  override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
    return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
  }

  override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
    return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
  }

  override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
    return childHelper.dispatchNestedPreFling(velocityX, velocityY)
  }
}
