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

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.navigation.navGraphViewModels
import im.vector.app.R
import im.vector.app.VectorApplication.Companion.coreContext
import im.vector.app.databinding.FragmentComingCallBinding
import im.vector.app.kelare.voip.navigateToActiveCall
import im.vector.app.kelare.voip.viewmodels.CallsViewModel
import im.vector.app.kelare.voip.viewmodels.ControlsViewModel
import org.linphone.core.tools.Log

class ComingCallFragment : GenericFragment<FragmentComingCallBinding>(){
    private val controlsViewModel: ControlsViewModel by navGraphViewModels(R.id.call_nav_graph)
    private val callsViewModel: CallsViewModel by navGraphViewModels(R.id.call_nav_graph)

    override fun getLayoutId(): Int = R.layout.fragment_coming_call

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.controlsViewModel = controlsViewModel

        binding.callsViewModel = callsViewModel

        callsViewModel.callConnectedEvent.observe(
                viewLifecycleOwner
        ) {
            it.consume {
                Log.e("connect qrrrrr")
                navigateToActiveCall()
            }
        }

        callsViewModel.callEndedEvent.observe(
                viewLifecycleOwner
        ) {
            it.consume {
                Log.e("connect 2")
                navigateToActiveCall()
            }
        }

        callsViewModel.currentCallData.observe(
                viewLifecycleOwner
        ) {
            if (it != null) {
                val timer = binding.root.findViewById<Chronometer>(R.id.incoming_call_timer)
                timer.base =
                        SystemClock.elapsedRealtime() - (1000 * it.call.duration) // Linphone timestamps are in seconds
                timer.start()
            }
        }

        val earlyMediaVideo = arguments?.getBoolean("earlyMediaVideo") ?: false
        if (earlyMediaVideo) {
            Log.i("[Incoming Call] Video early media detected, setting native window id")
            coreContext.core.nativeVideoWindowId = binding.remoteVideoSurface
        }
    }
}
