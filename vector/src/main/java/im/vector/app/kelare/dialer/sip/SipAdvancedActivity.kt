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

package im.vector.app.kelare.dialer.sip

import SelectValueEvent
import SipAdvancedInfoEvent
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.squareup.otto.Subscribe
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityDialerSettingBinding
import im.vector.app.databinding.ActivitySipAdvancedBinding
import im.vector.app.kelare.network.models.SIPAdvancedInfo
import timber.log.Timber

@AndroidEntryPoint
class SipAdvancedActivity : VectorBaseActivity<ActivitySipAdvancedBinding>(), View.OnClickListener {

    override fun getBinding() = ActivitySipAdvancedBinding.inflate(layoutInflater)

    private var advancedInfo : SIPAdvancedInfo = SIPAdvancedInfo()
    private var isIncoming = false
    private var isTls = false
    private var isDefault = false
    private var isEnable = false
    private var isUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    private fun initView() {

        advancedInfo = intent.extras!!.getSerializable("advancedInfo") as SIPAdvancedInfo
        isEnable = intent.getBooleanExtra("isEnable", false)
        isDefault = intent.getBooleanExtra("isDefault", false)
        isUpload = intent.getBooleanExtra("isUpload", false)

        views.rlBack.setOnClickListener(this)
        views.llRange.setOnClickListener(this)
        views.llAudio.setOnClickListener(this)
        views.llVideo.setOnClickListener(this)
        views.llEncrypt.setOnClickListener(this)
        views.llTransport.setOnClickListener(this)

        renderData()

        views.sbIncomingCalls.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isIncoming = isChecked
                Timber.e("ping: $isIncoming")
            }
        })

        views.sbVerify.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isTls = isChecked
                Timber.e("verify: $isTls")
            }
        })

        resetViewStatus()

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                updateData()
                mBus.post(SipAdvancedInfoEvent(advancedInfo))
                finish()
            }
            R.id.ll_range -> {
                if (views.llRangeDetail.visibility == View.GONE) {
                    views.llRangeDetail.visibility = View.VISIBLE
                } else {
                    views.llRangeDetail.visibility = View.GONE
                }
            }
            R.id.ll_audio -> {
                if (views.llAudioDetail.visibility == View.GONE) {
                    views.llAudioDetail.visibility = View.VISIBLE
                } else {
                    views.llAudioDetail.visibility = View.GONE
                }
            }
            R.id.ll_video -> {
                if (views.llVideoDetail.visibility == View.GONE) {
                    views.llVideoDetail.visibility = View.VISIBLE
                } else {
                    views.llVideoDetail.visibility = View.GONE
                }
            }
            R.id.ll_transport -> {
                val intent = Intent(this, SipTransportActivity::class.java)
                intent.putExtra("value", views.tvSipTransport.text)
                startActivity(intent)
            }
            R.id.ll_encrypt -> {
                val intent = Intent(this, SipEncryptActivity::class.java)
                intent.putExtra("value", views.tvEncryptMedia.text)
                startActivity(intent)
            }
            else -> {
            }
        }
    }


    @Subscribe
    fun onSelectedValueEvent(event: SelectValueEvent) {
        if (event.index == 1) {
            views.tvSipTransport.text = event.name
        } else {
            views.tvEncryptMedia.text = event.name
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderData() {

        isTls = advancedInfo.tlsEnable
        isIncoming = advancedInfo.incomingCalls

        views.etAuthName.setText(advancedInfo.authName)
        views.etProxy.setText(advancedInfo.outProxy)
        views.sbIncomingCalls.isChecked = isIncoming
        views.etRefreshInterval.setText(advancedInfo.refreshInterval)
        if (TextUtils.isEmpty(advancedInfo.sipTransport)) {
            views.tvSipTransport.text = "UDP"
        } else {
            views.tvSipTransport.text = advancedInfo.sipTransport
        }

        if (TextUtils.isEmpty(advancedInfo.encryptMedia)) {
            views.tvEncryptMedia.text = "NONE"
        } else {
            views.tvEncryptMedia.text = advancedInfo.encryptMedia
        }

        if (!TextUtils.isEmpty(advancedInfo.sipPortStart)) {
            views.etInterval.setText(advancedInfo.interval)
            views.etRangeStart.setText(advancedInfo.sipPortStart)
            views.etRangeEnd.setText(advancedInfo.sipPortEnd)
            views.etAudioStart.setText(advancedInfo.rtpAudioPortStart)
            views.etAudioEnd.setText(advancedInfo.rtpAudioPortEnd)
            views.etVideoStart.setText(advancedInfo.rtpVideoPortStart)
            views.etVideoEnd.setText(advancedInfo.rtpVideoPortEnd)
        }
        views.sbVerify.isChecked = isTls
    }

    private fun updateData() {
        advancedInfo.authName = views.etAuthName.text.toString()
        if (views.etProxy.text.toString().isEmpty()) {
            advancedInfo.outProxy = views.etProxy.text.toString()
        } else {
            advancedInfo.outProxy = views.etProxy.text.toString().replace(" ", "")
        }
        advancedInfo.incomingCalls = isIncoming
        advancedInfo.refreshInterval = views.etRefreshInterval.text.toString()
        advancedInfo.interval = views.etInterval.text.toString()
        advancedInfo.sipTransport = views.tvSipTransport.text.toString()
        advancedInfo.encryptMedia = views.tvEncryptMedia.text.toString()
        advancedInfo.sipPortStart = views.etRangeStart.text.toString()
        advancedInfo.sipPortEnd = views.etRangeEnd.text.toString()
        advancedInfo.rtpAudioPortStart = views.etAudioStart.text.toString()
        advancedInfo.rtpAudioPortEnd = views.etAudioEnd.text.toString()
        advancedInfo.rtpVideoPortStart = views.etVideoStart.text.toString()
        advancedInfo.rtpVideoPortEnd = views.etVideoEnd.text.toString()
        advancedInfo.tlsEnable = isTls
    }

    private fun resetViewStatus() {

        if (isUpload) {
            views.etProxy.isEnabled = false
            views.etAuthName.isEnabled = false
            views.etRefreshInterval.isEnabled = false
            views.etInterval.isEnabled = false
            views.sbVerify.isEnabled = false
            views.sbIncomingCalls.isEnabled = false
            views.llTransport.isEnabled = false
            views.llEncrypt.isEnabled = false
            views.etRangeStart.isEnabled = false
            views.etRangeEnd.isEnabled = false
            views.etAudioStart.isEnabled = false
            views.etAudioEnd.isEnabled = false
            views.etVideoStart.isEnabled = false
            views.etVideoEnd.isEnabled = false
        } else {
            if (isEnable) {
                views.etProxy.isEnabled = false
                views.etAuthName.isEnabled = false
                views.etRefreshInterval.isEnabled = false
                views.etInterval.isEnabled = false
                views.sbVerify.isEnabled = false
                views.sbIncomingCalls.isEnabled = false
                views.llTransport.isEnabled = false
                views.llEncrypt.isEnabled = false
                views.etRangeStart.isEnabled = false
                views.etRangeEnd.isEnabled = false
                views.etAudioStart.isEnabled = false
                views.etAudioEnd.isEnabled = false
                views.etVideoStart.isEnabled = false
                views.etVideoEnd.isEnabled = false
            }
        }
    }
}
