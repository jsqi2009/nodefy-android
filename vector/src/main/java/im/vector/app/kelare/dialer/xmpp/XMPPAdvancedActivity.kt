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

package im.vector.app.kelare.dialer.xmpp

import AccountInfoEvent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityXmppAdvancedBinding
import im.vector.app.databinding.ActivityXmppLoginBinding
import timber.log.Timber

@AndroidEntryPoint
class XMPPAdvancedActivity : VectorBaseActivity<ActivityXmppAdvancedBinding>(), View.OnClickListener {

    override fun getBinding() = ActivityXmppAdvancedBinding.inflate(layoutInflater)

    private var isPing = false
    private var isVerify = false
    private var isEdit = false
    private var isEnable = false
    private var isUpload = false
    private var proxy:String? = null
    private var interval:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        isEdit = intent.getBooleanExtra("isEdit", false)
        isEnable = intent.getBooleanExtra("isEnable", false)
        isPing = intent.getBooleanExtra("isPing", false)
        isVerify = intent.getBooleanExtra("isVerify", false)
        isUpload = intent.getBooleanExtra("isUpload", false)
        proxy = intent.extras!!.getString("proxy")
        interval = intent.extras!!.getString("interval")

        initView()
    }

    private fun initView() {

        views.rlBack.setOnClickListener(this)

        views.sbPing.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isPing = isChecked
                Timber.e("ping: $isPing")
            }
        })

        views.sbVerify.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isVerify = isChecked
                Timber.e("verify: $isVerify")
            }
        })

        views.sbPing.isChecked = isPing
        views.sbVerify.isChecked = isVerify
        views.etProxy.setText(proxy)
        views.etInterval.setText(interval)

        resetViewStatus()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                mBus.post(AccountInfoEvent(views.etProxy.text.toString(), views.etInterval.text.toString(), isPing, isVerify))
                finish()
            }
            else         -> {
            }
        }
    }

    private fun resetViewStatus() {

        if (isUpload) {
            views.etProxy.isEnabled = false
            views.etInterval.isEnabled = false
            views.sbVerify.isEnabled = false
            views.sbPing.isEnabled = false
        } else {
            views.etProxy.isEnabled = !isEnable
            views.etInterval.isEnabled = !isEnable
            views.sbVerify.isEnabled = !isEnable
            views.sbPing.isEnabled = !isEnable
        }
    }
}
