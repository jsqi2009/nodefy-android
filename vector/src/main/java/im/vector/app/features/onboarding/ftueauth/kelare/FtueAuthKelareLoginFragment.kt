/*
 * Copyright (c) 2022 New Vector Ltd
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

package im.vector.app.features.onboarding.ftueauth.kelare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import im.vector.app.databinding.FragmentFtueAuthKelareLoginBinding
import im.vector.app.features.onboarding.ftueauth.AbstractSSOFtueAuthFragment
import javax.inject.Inject


class FtueAuthKelareLoginFragment @Inject constructor(): AbstractSSOFtueAuthFragment<FragmentFtueAuthKelareLoginBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueAuthKelareLoginBinding {
        return FragmentFtueAuthKelareLoginBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.passwordEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun resetViewModel() {

    }
}
