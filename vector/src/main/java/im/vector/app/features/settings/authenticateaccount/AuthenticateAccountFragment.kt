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

package im.vector.app.features.settings.authenticateaccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.fragmentViewModel
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentAuthenticateAccountBinding
import im.vector.app.databinding.FragmentDeactivateAccountBinding
import im.vector.app.features.MainActivity
import im.vector.app.features.MainActivityArgs
import im.vector.app.features.accountcontact.AccountContactDetailActivity
import im.vector.app.features.auth.ReAuthActivity
import im.vector.app.features.settings.VectorSettingsActivity
import im.vector.app.features.settings.account.deactivation.DeactivateAccountAction
import im.vector.app.features.settings.account.deactivation.DeactivateAccountViewEvents
import im.vector.app.features.settings.account.deactivation.DeactivateAccountViewModel
import im.vector.app.features.settings.authenticateaccount.auth.AuthSkypeAccountActivity
import org.matrix.android.sdk.api.session.uia.exceptions.UiaCancelledException
import java.io.Serializable
import javax.inject.Inject

class AuthenticateAccountFragment @Inject constructor() : VectorBaseFragment<FragmentAuthenticateAccountBinding>()  {

    private val viewModel: AuthenticateAccountViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAuthenticateAccountBinding {
        return FragmentAuthenticateAccountBinding.inflate(inflater, container, false)
    }

    private var settingsActivity: VectorSettingsActivity? = null

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.authenticate_account_title)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        settingsActivity = context as? VectorSettingsActivity
    }

    override fun onDetach() {
        super.onDetach()
        settingsActivity = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewListeners()
        observeViewEvents()
    }

    private fun setupViewListeners() {
        views.skypeAuth.debouncedClicks {
            viewModel.handle(AuthenticateAccountAction.AuthAccount(requireActivity().getString(R.string.authenticate_account_skype_title)))
        }

        views.slackAuth.debouncedClicks {
            viewModel.handle(AuthenticateAccountAction.AuthAccount(requireActivity().getString(R.string.authenticate_account_slack_title)))
        }

        views.telegramAuth.debouncedClicks {
            viewModel.handle(AuthenticateAccountAction.AuthAccount(requireActivity().getString(R.string.authenticate_account_telegram_title)))
        }

        views.whatsappAuth.debouncedClicks {
            viewModel.handle(AuthenticateAccountAction.AuthAccount(requireActivity().getString(R.string.authenticate_account_whatsapp_title)))
        }
    }

    private fun observeViewEvents() {
        viewModel.observeViewEvents {
            when (it) {
                is AuthenticateAccountViewEvents.AuthType       -> {
                    //showLoadingDialog(it.type)
                    if (it.type == requireActivity().getString(R.string.authenticate_account_skype_title)) {
                        val intent = Intent(context, AuthSkypeAccountActivity::class.java)
                        requireActivity().startActivity(intent)
                    }
                }
                is AuthenticateAccountViewEvents.Loading       -> {
                    settingsActivity?.ignoreInvalidTokenError = true
                    showLoadingDialog(it.message)
                }
                AuthenticateAccountViewEvents.InvalidAuth      -> {
                    dismissLoadingDialog()
                    settingsActivity?.ignoreInvalidTokenError = false
                }
                is AuthenticateAccountViewEvents.OtherFailure  -> {
                    settingsActivity?.ignoreInvalidTokenError = false
                    dismissLoadingDialog()
                    if (it.throwable !is UiaCancelledException) {
                        displayErrorDialog(it.throwable)
                    }
                }
                AuthenticateAccountViewEvents.Done             -> {
                    MainActivity.restartApp(requireActivity(), MainActivityArgs(clearCredentials = true, isAccountDeactivated = true))
                }
            }
        }
    }


}
