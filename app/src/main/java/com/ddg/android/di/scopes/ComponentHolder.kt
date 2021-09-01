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
package com.ddg.android.di.scopes

object ComponentHolder {
  val components = mutableMapOf<Any, Any>()

  // this could also be `components[T::class.java] as T` but I rather
  // iterate over the real values and filter by instance
  inline fun <reified T> component(): T = components.values
    .filterIsInstance<T>()
    .single()
}
