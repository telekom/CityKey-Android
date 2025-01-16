/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.utils.extensions

import android.view.animation.*

inline fun Animation.listen(init: KAnimationListener.() -> Unit) =
    setAnimationListener(KAnimationListener().apply(init))

class KAnimationListener : Animation.AnimationListener {
    private var _onAnimationRepeat: ((Animation) -> Unit)? = null
    private var _onAnimationEnd: ((Animation) -> Unit)? = null
    private var _onAnimationStart: ((Animation) -> Unit)? = null

    override fun onAnimationRepeat(animation: Animation) {
        _onAnimationRepeat?.invoke(animation)
    }

    override fun onAnimationEnd(animation: Animation) {
        _onAnimationEnd?.invoke(animation)
    }

    override fun onAnimationStart(animation: Animation) {
        _onAnimationStart?.invoke(animation)
    }

    fun onAnimationRepeat(listener: (Animation) -> Unit) {
        _onAnimationRepeat = listener
    }

    fun onAnimationEnd(listener: (Animation) -> Unit) {
        _onAnimationEnd = listener
    }

    fun onAnimationStart(listener: (Animation) -> Unit) {
        _onAnimationStart = listener
    }
}
