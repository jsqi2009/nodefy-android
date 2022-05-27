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

import UpdateLanguageEvent
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.text.isDigitsOnly
import androidx.core.view.isInvisible
import com.google.android.material.textfield.TextInputLayout
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.squareup.otto.Subscribe
import im.vector.app.BuildConfig
import im.vector.app.R
import im.vector.app.core.utils.ensureProtocol
import im.vector.app.databinding.FragmentFtueAuthKelareLoginBinding
import im.vector.app.features.onboarding.OnboardingAction
import im.vector.app.features.onboarding.OnboardingViewState
import im.vector.app.features.onboarding.ftueauth.AbstractSSOFtueAuthFragment
import timber.log.Timber
import javax.inject.Inject


class FtueAuthKelareLoginFragment: AbstractSSOFtueAuthFragment<FragmentFtueAuthKelareLoginBinding>(), View.OnClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueAuthKelareLoginBinding {
        return FragmentFtueAuthKelareLoginBinding.inflate(inflater, container, false)
    }

    private val languageList = arrayListOf("English(US)", "中文")
    private val defaultType = "User name"
    private val defaultLanguage = languageList.first()

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusBarColor(activity!!)
        setupViews()
    }

    private fun setupViews() {

        views.tvType.text = defaultType
        views.tvLanguage.text = defaultLanguage
        views.passwordEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        views.loginTv.setOnClickListener(this)
        views.forgotPasswordTv.setOnClickListener(this)
        views.createAccountTv.setOnClickListener(this)
        views.llSignInType.setOnClickListener(this)
        views.llLanguage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.loginTv    -> {
                submit()
            }
            R.id.forgotPasswordTv    -> {

            }
            R.id.createAccountTv    -> {

            }
            R.id.ll_sign_in_type    -> {
                switchSignInType()
            }
            R.id.ll_language    -> {
                switchLanguage()
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

    @SuppressLint("UseRequireInsteadOfGet")
    private fun switchSignInType() {
        views.ivArrow.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_up_arrow, null))
        val typeList = arrayListOf(defaultType)
        val signInTypePopup = SignInTypePopup(activity!!, mBus, views.tvType.text.toString(), typeList)
        signInTypePopup!!.showOnAnchor(views.llSignInType, RelativePopupWindow.VerticalPosition.BELOW,
                RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, 0, 5,true)
        signInTypePopup.setOnDismissListener {
            views.ivArrow.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_down_arrow, null))
        }
    }

    @SuppressLint("UseRequireInsteadOfGet", "UseCompatLoadingForDrawables")
    private fun switchLanguage() {
        views.ivLanguageArrow.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_up_arrow_white, null))
        val languagePopup = SwitchLanguagePopup(activity!!, mBus, views.tvLanguage.text.toString(), languageList)
        languagePopup!!.showOnAnchor(views.llLanguage, RelativePopupWindow.VerticalPosition.ABOVE,
                RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, 0, 5,true)
        languagePopup.setOnDismissListener {
            views.ivLanguageArrow.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_down_arrow_white, null))
        }
    }

    @Subscribe
    fun onUpdateLanguageEvent(event: UpdateLanguageEvent) {
        views.tvLanguage.text = event.value
    }

    private fun loginWithHomeServer(username: String, password: String) {
        val serverUrl = views.serverEt.text.toString().trim().ensureProtocol()
        viewModel.handle(OnboardingAction.KelareLoginWithHomeServer(serverUrl, username, password, getString(R.string.login_default_session_public_name)))
    }

    override fun resetViewModel() {

    }

    override fun updateWithState(state: OnboardingViewState) {

    }
}
