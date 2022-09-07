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

package im.vector.app.features.settings.authenticateaccount.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAuthSkypeAccountBinding
import im.vector.app.databinding.ActivityAuthTelegramAccountBinding
import timber.log.Timber

@AndroidEntryPoint
class AuthTelegramAccountActivity : VectorBaseActivity<ActivityAuthTelegramAccountBinding>(), View.OnClickListener {
    override fun getBinding() = ActivityAuthTelegramAccountBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        setupToolbar(views.authToolbar).allowBack()
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        views.codePicker.registerCarrierNumberEditText(views.phoneField)
        views.codePicker.setOnCountryChangeListener {
            views.phoneField.setText("")
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.authTelegramView-> {
                telegramAuth()
            }
            else               -> {}
        }
    }

    private fun telegramAuth() {
        val phoneNumber = views.phoneField.text.toString()

        if (TextUtils.isEmpty(phoneNumber)) {
            showToast(getString(R.string.auth_telegram_phone_input_tips))
            return
        }

        Timber.e("country code1---->${views.codePicker.fullNumber}")
        Timber.e("country code2---->${views.codePicker.fullNumberWithPlus}")
        Timber.e("country code3---->${views.codePicker.formattedFullNumber}")
    }
}
