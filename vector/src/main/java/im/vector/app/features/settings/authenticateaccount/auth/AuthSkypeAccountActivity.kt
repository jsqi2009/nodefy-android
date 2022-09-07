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
import im.vector.app.databinding.ActivitySimpleBinding

@AndroidEntryPoint
class AuthSkypeAccountActivity : VectorBaseActivity<ActivityAuthSkypeAccountBinding>(), View.OnClickListener {

    override fun getBinding() = ActivityAuthSkypeAccountBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        setupToolbar(views.authToolbar).allowBack()
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        views.authSkypeView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
             R.id.authSkypeView -> {
                 skypeAuth()
             }
            else                -> {}
        }
    }

    private fun skypeAuth() {
        val userName = views.usernameField.text.toString()
        val password = views.passwordField.text.toString()

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            showToast(getString(R.string.auth_skype_account_input_tips))
            return
        }
    }
}
