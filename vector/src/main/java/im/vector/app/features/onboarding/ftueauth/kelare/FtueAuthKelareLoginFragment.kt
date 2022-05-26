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
import android.widget.ArrayAdapter
import androidx.core.text.isDigitsOnly
import androidx.core.view.isInvisible
import com.google.android.material.textfield.TextInputLayout
import im.vector.app.BuildConfig
import im.vector.app.R
import im.vector.app.core.utils.ensureProtocol
import im.vector.app.databinding.FragmentFtueAuthKelareLoginBinding
import im.vector.app.features.onboarding.OnboardingAction
import im.vector.app.features.onboarding.OnboardingViewState
import im.vector.app.features.onboarding.ftueauth.AbstractSSOFtueAuthFragment
import javax.inject.Inject


class FtueAuthKelareLoginFragment @Inject constructor(): AbstractSSOFtueAuthFragment<FragmentFtueAuthKelareLoginBinding>(), View.OnClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueAuthKelareLoginBinding {
        return FragmentFtueAuthKelareLoginBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        views.passwordEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        views.loginTv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.loginTv    -> {
                submit()
            }
            else -> {}
        }
    }

    private fun submit() {

        val userName = views.userNameEt.text.toString()
        val password = views.passwordEt.text.toString()



        // This can be called by the IME action, so deal with empty cases
        var error = 0
        if (userName.isEmpty()) {
            error++
        }
        if (password.isEmpty()) {
            error++
        }

        if (error == 0) {
            loginWithHomeServer(userName, password)
        }
    }

    private fun loginWithHomeServer(username: String, password: String) {
        val serverUrl = views.serverEt.text.toString().trim().ensureProtocol()
        viewModel.handle(OnboardingAction.KelareLoginWithHomeServer(serverUrl, username, password, getString(R.string.login_default_session_public_name)))
    }

    private fun setupUi(state: OnboardingViewState){
        val completions = state.knownCustomHomeServersUrls + if (BuildConfig.DEBUG) listOf("User name") else emptyList()
        views.loginServerUrlFormHomeServerUrl.setAdapter(
                ArrayAdapter(
                        requireContext(),
                        R.layout.item_completion_sign_in_type,
                        completions
                )
        )
        views.loginServerUrlFormHomeServerUrlTil.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                .takeIf { completions.isNotEmpty() }
                ?: TextInputLayout.END_ICON_NONE
    }

    override fun resetViewModel() {

    }

    override fun updateWithState(state: OnboardingViewState) {
        setupUi(state)
    }
}
