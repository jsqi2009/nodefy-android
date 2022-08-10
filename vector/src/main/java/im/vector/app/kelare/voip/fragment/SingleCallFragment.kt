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

package im.vector.app.kelare.voip.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.navigation.navGraphViewModels
import im.vector.app.R
import im.vector.app.VectorApplication.Companion.coreContext
import im.vector.app.databinding.FragmentSingleCallBinding
import im.vector.app.kelare.voip.viewmodels.CallsViewModel
import im.vector.app.kelare.voip.viewmodels.ConferenceViewModel
import im.vector.app.kelare.voip.viewmodels.ControlsViewModel
import im.vector.app.kelare.voip.viewmodels.StatisticsListViewModel
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.tools.Log



class SingleCallFragment : GenericFragment<FragmentSingleCallBinding>(), View.OnClickListener {
    private val controlsViewModel: ControlsViewModel by navGraphViewModels(R.id.call_nav_graph)
    private val callsViewModel: CallsViewModel by navGraphViewModels(R.id.call_nav_graph)
    private val conferenceViewModel: ConferenceViewModel by navGraphViewModels(R.id.call_nav_graph)
    private val statsViewModel: StatisticsListViewModel by navGraphViewModels(R.id.call_nav_graph)

    private var dialog: Dialog? = null

    private var fullAccount = ""

    private var isMute = false
    private var isSpeaker = false
    private var isHold = false

    override fun getLayoutId(): Int = R.layout.fragment_single_call

    override fun onStart() {
        useMaterialSharedAxisXForwardAnimation = false

        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controlsViewModel.hideCallStats() // In case it was toggled on during incoming/outgoing fragment was visible
        binding.lifecycleOwner = viewLifecycleOwner
        binding.controlsViewModel = controlsViewModel
        binding.callsViewModel = callsViewModel
        binding.conferenceViewModel = conferenceViewModel
        binding.statsViewModel = statsViewModel

        callsViewModel.currentCallData.observe(
                viewLifecycleOwner
        ) {
            if (it != null) {
                val timer = binding.root.findViewById<Chronometer>(R.id.active_call_timer)
                timer.base =
                        SystemClock.elapsedRealtime() - (1000 * it.call.duration) // Linphone timestamps are in seconds
                timer.start()
            }
        }

        callsViewModel.callUpdateEvent.observe(
                viewLifecycleOwner
        ) {
            it.consume { call ->
                if (call.state == Call.State.StreamsRunning) {
                    dialog?.dismiss()
                } else if (call.state == Call.State.UpdatedByRemote) {
                    if (coreContext.core.isVideoEnabled) {
                        val remoteVideo = call.remoteParams?.isVideoEnabled ?: false
                        val localVideo = call.currentParams.isVideoEnabled
                        if (remoteVideo && !localVideo) {
                            //showCallVideoUpdateDialog(call)
                        }
                    } else {
                        Log.w("[Single Call] Video display & capture are disabled, don't show video dialog")
                    }
                }
            }
        }

        initView()
    }

    private fun initView() {

        binding.llMute.setOnClickListener(this)
        binding.llKeypad.setOnClickListener(this)
        binding.llSpeaker.setOnClickListener(this)
        binding.llHold.setOnClickListener(this)
        binding.ivDecline.setOnClickListener(this)
        binding.tvHide.setOnClickListener(this)
        binding.ll1.setOnClickListener(this)
        binding.ll2.setOnClickListener(this)
        binding.ll3.setOnClickListener(this)
        binding.ll4.setOnClickListener(this)
        binding.ll5.setOnClickListener(this)
        binding.ll6.setOnClickListener(this)
        binding.ll7.setOnClickListener(this)
        binding.ll8.setOnClickListener(this)
        binding.ll9.setOnClickListener(this)
        binding.ll10.setOnClickListener(this)
        binding.ll11.setOnClickListener(this)
        binding.ll12.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()

        controlsViewModel.hideExtraButtons(true)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ll_mute -> {
                toggleMute()
            }
            R.id.ll_keypad -> {
                binding.llShowKeypad.visibility = View.VISIBLE
                binding.llMuteSpeaker.visibility = View.GONE
                binding.llHold.visibility = View.GONE
                binding.tvHide.visibility = View.VISIBLE
                binding.etDtmf.visibility = View.VISIBLE
                binding.remoteName.visibility = View.GONE

                //lastValue = fullAccount
            }
            R.id.ll_speaker -> {
                toggleSpeaker()
            }
            R.id.ll_hold -> {
                pauseOrResume()
            }
            R.id.tv_hide -> {
                binding.llShowKeypad.visibility = View.GONE
                binding.llMuteSpeaker.visibility = View.VISIBLE
                binding.llHold.visibility = View.VISIBLE
                binding.tvHide.visibility = View.GONE
                binding.etDtmf.visibility = View.GONE
                binding.remoteName.visibility = View.VISIBLE

                binding.etDtmf.setText("")
                sendDTMF()
            }
            R.id.ll_1-> {
                fullAccount += binding.tv1Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_2-> {
                fullAccount += binding.tv2Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_3-> {
                fullAccount += binding.tv3Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_4-> {
                fullAccount += binding.tv4Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_5-> {
                fullAccount += binding.tv5Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_6-> {
                fullAccount += binding.tv6Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_7-> {
                fullAccount += binding.tv7Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_8-> {
                fullAccount += binding.tv8Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_9-> {
                fullAccount += binding.tv9Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_10-> {
                fullAccount += binding.tv10Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_11-> {
                fullAccount += binding.tv11Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            R.id.ll_12-> {
                fullAccount += binding.tv12Num.text.toString()
                binding.etDtmf.setText(fullAccount)
            }
            else -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun toggleMute() {
        if (!isMute) {
            binding.llMute.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            binding.tvMute.text = "unmute"
            isMute = true
        } else {
            binding.llMute.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            binding.tvMute.text = "mute"
            isMute = false
        }
        coreContext.core.isMicEnabled = !coreContext.core.isMicEnabled
    }

    private fun sendDTMF() {
        if (!TextUtils.isEmpty(fullAccount)) {
            //Timber.e("send call dtms, $fullAccount")
            coreContext.core.currentCall?.sendDtmfs(fullAccount)

            fullAccount = ""
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pauseOrResume() {
        if (coreContext.core.callsNb == 0) return
        val call = if (coreContext.core.currentCall != null) coreContext.core.currentCall else coreContext.core.calls[0]
        call ?: return

        if (!isHold) {
            binding.llHold.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            binding.tvHold.text = "unhold"
            isHold = true
        } else {
            binding.llHold.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            binding.tvHold.text = "hold"
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
            binding.llSpeaker.setBackgroundResource(R.drawable.shape_dialer_call_action_bg)
            isSpeaker = true
        } else {
            binding.llSpeaker.setBackgroundResource(R.drawable.shape_dialer_call_bg)
            isSpeaker = false
        }

        // Get the currently used audio device
        val currentAudioDevice = coreContext.core.currentCall?.outputAudioDevice
        val speakerEnabled = currentAudioDevice?.type == AudioDevice.Type.Speaker

        // We can get a list of all available audio devices using
        // Note that on tablets for example, there may be no Earpiece device
        for (audioDevice in coreContext.core.audioDevices) {
            if (speakerEnabled && audioDevice.type == AudioDevice.Type.Earpiece) {
                coreContext.core.currentCall?.outputAudioDevice = audioDevice
                return
            } else if (!speakerEnabled && audioDevice.type == AudioDevice.Type.Speaker) {
                coreContext.core.currentCall?.outputAudioDevice = audioDevice
                return
            }
        }
    }

    private fun isCallPaused(): Boolean {
        return when ( coreContext.core.currentCall?.state) {
            Call.State.Paused, Call.State.Pausing -> true
            else                                  -> false
        }
    }
}
