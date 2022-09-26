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

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityDialerCallBinding
import im.vector.app.databinding.ActivityDialerContactDetailBinding
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import timber.log.Timber
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class DialerCallActivity : VectorBaseActivity<ActivityDialerCallBinding>(), View.OnClickListener {

    override fun getBinding() = ActivityDialerCallBinding.inflate(layoutInflater)

    private var executor: ScheduledThreadPoolExecutor? = null
    private var seconds = 0
    private var remoteUser = ""
    private var localUser = ""
    private var domain = ""
    private var index = -1
    private var fullAccount = ""
    private var lastValue = ""

    private var isMute = false
    private var isSpeaker = false
    private var isHold = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }

    private fun initView() {
        index = intent.extras!!.getInt("index")
        localUser = intent.extras!!.getString("local_user").toString()
        remoteUser = intent.extras!!.getString("remote_user").toString()
        domain = intent.extras!!.getString("domain").toString()


        views.tvCallNumber.text = remoteUser

        views.llMute.setOnClickListener(this)
        views.llKeypad.setOnClickListener(this)
        views.llSpeaker.setOnClickListener(this)
        views.llHold.setOnClickListener(this)
        views.ivDecline.setOnClickListener(this)
        views.tvHide.setOnClickListener(this)
        views.ll1.setOnClickListener(this)
        views.ll2.setOnClickListener(this)
        views.ll3.setOnClickListener(this)
        views.ll4.setOnClickListener(this)
        views.ll5.setOnClickListener(this)
        views.ll6.setOnClickListener(this)
        views.ll7.setOnClickListener(this)
        views.ll8.setOnClickListener(this)
        views.ll9.setOnClickListener(this)
        views.ll10.setOnClickListener(this)
        views.ll11.setOnClickListener(this)
        views.ll12.setOnClickListener(this)

//        core.enableVideoCapture(true)
//        core.enableVideoDisplay(true)
        core.addListener(callCoreListener)

    }

    override fun onResume() {
        super.onResume()

        if (index == 1) {
            outgoingCall()
        } else {
            acceptCall()
        }

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ll_mute   -> {
                toggleMute()
            }
            R.id.ll_keypad -> {
                views.llShowKeypad.visibility = View.VISIBLE
                views.llMuteSpeaker.visibility = View.GONE
                views.llHold.visibility = View.GONE
                views.tvHide.visibility = View.VISIBLE
                views.etDtmf.visibility = View.VISIBLE
                views.tvCallNumber.visibility = View.GONE

                //lastValue = fullAccount
            }
            R.id.ll_speaker -> {
                toggleSpeaker()
            }
            R.id.ll_hold -> {
                pauseOrResume()
            }
            R.id.iv_decline -> {
                hangUp()
                finish()
            }
            R.id.tv_hide -> {
                views.llShowKeypad.visibility = View.GONE
                views.llMuteSpeaker.visibility = View.VISIBLE
                views.llHold.visibility = View.VISIBLE
                views.tvHide.visibility = View.GONE
                views.etDtmf.visibility = View.GONE
                views.tvCallNumber.visibility = View.VISIBLE

                views.etDtmf.setText("")
                sendDTMF()
            }
            R.id.ll_1-> {
                fullAccount += views.tv1Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_2-> {
                fullAccount += views.tv2Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_3-> {
                fullAccount += views.tv3Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_4-> {
                fullAccount += views.tv4Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_5-> {
                fullAccount += views.tv5Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_6-> {
                fullAccount += views.tv6Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_7-> {
                fullAccount += views.tv7Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_8-> {
                fullAccount += views.tv8Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_9-> {
                fullAccount += views.tv9Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_0-> {
                fullAccount += views.tv10Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_11-> {
                fullAccount += views.tv11Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            R.id.ll_12-> {
                fullAccount += views.tv12Num.text.toString()
                views.etDtmf.setText(fullAccount)
            }
            else -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun toggleMute() {
        if (!isMute) {
            views.llMute.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            views.tvMute.text = "unmute"
            isMute = true
        } else {
            views.llMute.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            views.tvMute.text = "mute"
            isMute = false
        }
        core.enableMic(!core.micEnabled())
    }

    private fun sendDTMF() {
        /*if (lastValue != fullAccount) {
            Timber.e("send call dtms")
            core.currentCall?.sendDtmfs(fullAccount)
        }*/

        if (!TextUtils.isEmpty(fullAccount)) {
            Timber.e("send call dtms, $fullAccount")
            core.currentCall?.sendDtmfs(fullAccount)

            fullAccount = ""
        }

    }

    private fun acceptCall() {
        core.currentCall?.accept()
    }

    private fun outgoingCall() {

        /*var proxy = ""
        if (TextUtils.isEmpty(selectedAccount.extension.outProxy)) {
            proxy = selectedAccount.domain!!
        } else {
            proxy = selectedAccount.extension.outProxy!!
        }*/

        // As for everything we need to get the SIP URI of the remote and convert it to an Address
        //val remoteSipUri = "sip:" + binding.etAccount.text.toString() + "@" + Contants.Proxy_Domain
        val remoteSipUri = "sip:$remoteUser@$domain"
        Timber.tag("sip uri").e(remoteSipUri)
        val remoteAddress = Factory.instance().createAddress(remoteSipUri)
        remoteAddress ?: return // If address parsing fails, we can't continue with outgoing call process

        // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
        val params = core.createCallParams(null)
        params ?: return // Same for params

        // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
        params.mediaEncryption = MediaEncryption.SRTP
        // If we wanted to start the call with video directly
        params.enableVideo(true)

        //core.interpretUrl(remoteUser)

        // Finally we start the call
        core.inviteAddressWithParams(remoteAddress, params)
        // Call process can be followed in onCallStateChanged callback from core listener

    }

    private fun hangUp() {

        if (core.callsNb == 0) return

        // If the call state isn't paused, we can get it using core.currentCall
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        // Terminating a call is quite simple
        call.terminate()
    }

    @SuppressLint("SetTextI18n")
    private fun pauseOrResume() {
        if (core.callsNb == 0) return
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        if (!isHold) {
            views.llHold.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            views.tvHold.text = "unhold"
            isHold = true
        } else {
            views.llHold.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            views.tvHold.text = "hold"
            isHold = false
        }

        if (call.state != Call.State.Paused && call.state != Call.State.Pausing) {
            // If our call isn't paused, let's pause it
            call.pause()
        } else if (call.state != Call.State.Resuming) {
            // Otherwise let's resume it
            call.resume()
        }
    }

    private fun toggleSpeaker() {

        if (!isSpeaker) {
            views.llSpeaker.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            isSpeaker = true
        } else {
            views.llSpeaker.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            isSpeaker = false
        }

        // Get the currently used audio device
        val currentAudioDevice = core.currentCall?.outputAudioDevice
        val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker

        // We can get a list of all available audio devices using
        // Note that on tablets for example, there may be no Earpiece device
        for (audioDevice in core.audioDevices) {
            if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                core.currentCall?.outputAudioDevice = audioDevice
                return
            }
        }
    }

    private fun startExecutorTimer() {
        if (executor == null) {
            executor = ScheduledThreadPoolExecutor(2,
                    BasicThreadFactory.Builder().namingPattern("timer thread").daemon(true).build())
            executor!!.scheduleAtFixedRate(Runnable {
                val hour = seconds / 3600 % 24
                val minute = seconds % 3600 / 60
                val time = String.format("%02d:%02d:%02d", hour, minute, seconds % 60)

                runOnUiThread {
                    views.tvDuration.text = time
                }
                seconds++
            }, 0, 1000, TimeUnit.MILLISECONDS)
        }
    }

    private val callCoreListener = object: CoreListenerStub() {

        @SuppressLint("SetTextI18n")
        override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
            //super.onCallStateChanged(core, call, state, message)
            Timber.e("call state: $state")
            when (state) {
                Call.State.IncomingReceived     -> {

                }
                Call.State.OutgoingInit     -> {
                    // First state an outgoing call will go through
                    views.tvStatus.text = "Calling..."
                }
                Call.State.OutgoingProgress -> {
                    // Right after outgoing init
                    views.tvStatus.text = "Calling..."
                }
                Call.State.OutgoingRinging  -> {
                    // This state will be reached upon reception of the 180 RINGING
                    views.tvStatus.text = "Calling..."
                }
                Call.State.Connected        -> {
                    // When the 200 OK has been received
                    //bindView.tvStatus.text = "Connected"

                    /*views.tvStatus.visibility = View.GONE
                    views.llDuration.visibility = View.VISIBLE
                    startExecutorTimer()*/

                    views.tvStatus.visibility = View.GONE
                    views.llDuration.visibility = View.VISIBLE
                    finish()
                }
                Call.State.StreamsRunning   -> {
                    // This state indicates the call is active.
                    // You may reach this state multiple times, for example after a pause/resume
                    // or after the ICE negotiation completes
                    // Wait for the call to be connected before allowing a call update
                }
                Call.State.Paused           -> {
                    // When you put a call in pause, it will became Paused

                }
                Call.State.PausedByRemote   -> {
                    // When the remote end of the call pauses it, it will be PausedByRemote
                }
                Call.State.Updating         -> {
                    // When we request a call update, for example when toggling video
                }
                Call.State.UpdatedByRemote  -> {
                    // When the remote requests a call update
                }
                Call.State.Released         -> {
                    stopExecutorTimer()
                    finish()
                }
                Call.State.Error            -> {

                    views.tvStatus.text = "Temporarily Unavailable"
                    finish()

                    /*stopExecutorTimer()
                    finish()*/
                }else                       -> {

            }

            }
        }
    }

    private fun stopExecutorTimer() {
        try {
            if (executor != null) {
                executor!!.shutdownNow()
                executor = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            core.removeListener(callCoreListener)
            stopExecutorTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            core.removeListener(callCoreListener)
            stopExecutorTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
