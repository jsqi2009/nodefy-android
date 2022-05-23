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

package im.vector.app.kelare.dialer.call

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityComingCallBinding
import im.vector.app.databinding.ActivityDialerContactDetailBinding
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import timber.log.Timber

@AndroidEntryPoint
class ComingCallActivity : VectorBaseActivity<ActivityComingCallBinding>(), View.OnClickListener {

    override fun getBinding() = ActivityComingCallBinding.inflate(layoutInflater)

    private var remoteUser = ""
    private var localUser = ""
    private var domain = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarColor(this)

        initView()
    }

    private fun initView() {

        localUser = intent.extras!!.getString("local_user").toString()
        remoteUser = intent.extras!!.getString("remote_user").toString()
        domain = intent.extras!!.getString("domain").toString()
        Timber.e(remoteUser)

        views.tvIncomeNumber.text = remoteUser

        views.ivAccept.setOnClickListener(this)
        views.ivDecline.setOnClickListener(this)
        views.rlBack.setOnClickListener(this)

        core.addListener(comingCoreListener)

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back    -> {
                core.currentCall?.terminate()
                finish()
            }
            R.id.iv_decline -> {
                core.currentCall?.terminate()
                finish()
            }
            R.id.iv_accept -> {
                val intent = Intent(this, DialerCallActivity::class.java)
                intent.putExtra("index", 2)
                intent.putExtra("remote_user", remoteUser)
                intent.putExtra("local_user", localUser)
                intent.putExtra("domain", domain)
                startActivity(intent)
                finish()
            }
            else -> {
            }
        }
    }

    private val comingCoreListener = object: CoreListenerStub() {

        override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State?,
                message: String
        ) {
            // When a call is received
            when (state) {
                Call.State.IncomingReceived -> {

                }
                Call.State.Connected -> {

                }
                Call.State.Released -> {
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            core.removeListener(comingCoreListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            core.removeListener(comingCoreListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
