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

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.core.extensions.restart
import im.vector.app.core.utils.ensureProtocol
import im.vector.app.databinding.FragmentFtueAuthKelareLoginBinding
import im.vector.app.features.onboarding.OnboardingAction
import im.vector.app.features.onboarding.OnboardingFlow
import im.vector.app.features.onboarding.OnboardingViewState
import im.vector.app.features.onboarding.ftueauth.AbstractSSOFtueAuthFragment
import im.vector.app.features.settings.VectorLocale
import im.vector.app.kelare.content.Contants
import timber.log.Timber
import java.util.Locale

class FtueAuthKelareLoginFragment : AbstractSSOFtueAuthFragment<FragmentFtueAuthKelareLoginBinding>(), View.OnClickListener, SwitchLanguagePopup.OnLanguageSelected {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueAuthKelareLoginBinding {
        return FragmentFtueAuthKelareLoginBinding.inflate(inflater, container, false)
    }

    private val languageList = arrayListOf("English(US)", "中文")
    private val defaultType = "User name"
    private val defaultLanguage = languageList.first()
    private var allSupportedLocales: ArrayList<Locale> = ArrayList()
    private var supportedLocales: ArrayList<Locale> = ArrayList()

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusBarColor(activity!!)
        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        getSupportLanguage()
        if (dialerSession.homeServer.isNotEmpty()) {
            views.serverEt.setText(dialerSession.homeServer)
        } else {
            views.serverEt.setText("")
        }

        /*views.apply {
            tvType.text  =defaultType
            tvLanguage.text = defaultLanguage
        }*/

        views.tvType.text = defaultType
        views.tvLanguage.text = VectorLocale.applicationLocale.getDisplayLanguage(VectorLocale.applicationLocale)
        views.passwordEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        views.forgotPasswordTv.paint.flags = Paint.UNDERLINE_TEXT_FLAG

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
                forgetPassword()
            }
            R.id.createAccountTv    -> {
                handleRegisterWithHomeServer()
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

    private fun forgetPassword() {
        if (android.text.TextUtils.isEmpty(views.serverEt.text.toString().trim().ensureProtocol())) {
            Toast.makeText(activity, getString(R.string.kelare_please_input_valid_homeserver), Toast.LENGTH_SHORT).show()
            return
        }
        val serverUrl = views.serverEt.text.toString().trim().ensureProtocol()
        viewModel.handle(OnboardingAction.KelareForgetPassword(serverUrl))
    }

    private fun submit() {

        val userName = views.userNameEt.text.toString().trimEnd()
        val password = views.passwordEt.text.toString().trim()

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

    @SuppressLint("UseRequireInsteadOfGet", "UseCompatLoadingForDrawables")
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
        val languagePopup = SwitchLanguagePopup(activity!!, mBus, views.tvLanguage.text.toString(), supportedLocales, this)
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

    private fun handleRegisterWithHomeServer() {

        if (android.text.TextUtils.isEmpty(views.serverEt.text.toString().trim().ensureProtocol())) {
            Toast.makeText(activity, getString(R.string.kelare_please_input_valid_homeserver), Toast.LENGTH_SHORT).show()
            return
        }

        val getStartedFlow = OnboardingFlow.SignUp
        val serverUrl = views.serverEt.text.toString().trim().ensureProtocol()
        viewModel.handle(OnboardingAction.KelareCreateAccountWithHomeServer(serverUrl, false, getStartedFlow))
    }

    override fun resetViewModel() {

    }

    override fun updateWithState(state: OnboardingViewState) {

    }

    private fun getSupportLanguage() {
        allSupportedLocales = KelareLocalUtil.getApplicationLocales(requireActivity())
        allSupportedLocales.forEach {
            if (it.getDisplayCountry(it).lowercase() == Contants.LANGUAGE_COUNTRY_CHINA.lowercase()
                    || it.getDisplayCountry(it).lowercase() == Contants.LANGUAGE_COUNTRY_US.lowercase()) {

                supportedLocales.add(it)

                Timber.e("language------${it.getDisplayLanguage(it)}")
                Timber.e("country------${it.getDisplayCountry(it)}")
            }
        }
    }

    override fun onClick(item: Locale) {
        views.tvLanguage.text = item.getDisplayLanguage(item)
        Timber.e("selected language------${item.getDisplayLanguage(item)}")
        handleSelectLocale(item)
    }

    /**
     * switch language
     */
    private fun handleSelectLocale(selectedLocal: Locale) {
        VectorLocale.saveApplicationLocale(selectedLocal)
        KelareLocalUtil.applyToApplicationContext(requireActivity())
        requireActivity().restart()
    }


}
