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

package im.vector.app.kelare.voip

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.window.layout.FoldingFeature
import com.gyf.immersionbar.ImmersionBar
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.VectorApplication.Companion.coreContext
import im.vector.app.VectorApplication.Companion.corePreferences
import im.vector.app.databinding.ActivityVoiceCallBinding
import im.vector.app.features.MainActivity
import im.vector.app.features.home.HomeActivity
import im.vector.app.kelare.compatibility.Compatibility
import im.vector.app.kelare.utils.PermissionHelper
import im.vector.app.kelare.voip.viewmodels.CallsViewModel
import im.vector.app.kelare.voip.viewmodels.ConferenceViewModel
import im.vector.app.kelare.voip.viewmodels.ControlsViewModel
import im.vector.app.kelare.voip.viewmodels.StatisticsListViewModel
import org.linphone.core.Call
import org.linphone.core.tools.Log
import org.linphone.mediastream.Version

@AndroidEntryPoint
class VoiceCallActivity : ProximitySensorActivity() {
    //ViewBinding
    private lateinit var binding: ActivityVoiceCallBinding

    private lateinit var controlsViewModel: ControlsViewModel
    private lateinit var callsViewModel: CallsViewModel
    private lateinit var conferenceViewModel: ConferenceViewModel
    private lateinit var statsViewModel: StatisticsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Compatibility.setShowWhenLocked(this, true)
        Compatibility.setTurnScreenOn(this, true)
        // Leaks on API 27+: https://stackoverflow.com/questions/60477120/keyguardmanager-memory-leak
        Compatibility.requestDismissKeyguard(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_voice_call)
        binding.lifecycleOwner = this
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // This can't be done in onCreate(), has to be at least in onPostCreate() !
        val navController = binding.navHostFragment.findNavController()
        val navControllerStoreOwner = navController.getViewModelStoreOwner(R.id.call_nav_graph)

        controlsViewModel = ViewModelProvider(navControllerStoreOwner)[ControlsViewModel::class.java]
        binding.controlsViewModel = controlsViewModel

        callsViewModel = ViewModelProvider(navControllerStoreOwner)[CallsViewModel::class.java]

        conferenceViewModel = ViewModelProvider(navControllerStoreOwner)[ConferenceViewModel::class.java]

        statsViewModel = ViewModelProvider(navControllerStoreOwner)[StatisticsListViewModel::class.java]

        controlsViewModel.askPermissionEvent.observe(
                this
        ) {
            it.consume { permission ->
                Log.i("[Call Activity] Asking for $permission permission")
                requestPermissions(arrayOf(permission), 0)
            }
        }

        controlsViewModel.fullScreenMode.observe(
                this
        ) { hide ->
            Compatibility.hideAndroidSystemUI(hide, window)
        }

        controlsViewModel.proximitySensorEnabled.observe(
                this
        ) { enabled ->
            enableProximitySensor(enabled)
        }

        controlsViewModel.isVideoEnabled.observe(
                this
        ) { enabled ->
            Compatibility.enableAutoEnterPiP(this, enabled, conferenceViewModel.conferenceExists.value == true)
        }

        controlsViewModel.callStatsVisible.observe(
                this
        ) { visible ->
            if (visible) statsViewModel.enable() else statsViewModel.disable()
        }

        callsViewModel.noMoreCallEvent.observe(
                this
        ) {
            it.consume { noMoreCall ->
                if (noMoreCall) {
                    Log.i("[Call Activity] No more call event fired, finishing activity")
                    finish()
                }
            }
        }

        callsViewModel.askWriteExternalStoragePermissionEvent.observe(
                this
        ) {
            it.consume {
                Log.i("[Call Activity] Asking for WRITE_EXTERNAL_STORAGE permission to take snapshot")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        callsViewModel.currentCallData.observe(
                this
        ) { callData ->
            if (callData.call.conference == null) {
                Log.i("[Call Activity] Current call isn't linked to a conference, changing fragment")
                navigateToActiveCall()
            } else {
                Log.i("[Call Activity] Current call is linked to a conference, changing fragment")
                //navigateToConferenceCall()
            }
        }

        callsViewModel.askPermissionEvent.observe(
                this
        ) {
            it.consume { permission ->
                Log.i("[Call Activity] Asking for $permission permission")
                requestPermissions(arrayOf(permission), 0)
            }
        }

        conferenceViewModel.conferenceExists.observe(
                this
        ) { exists ->
            if (exists) {
                Log.i("[Call Activity] Found active conference, changing fragment")
                //navigateToConferenceCall()
            } else if (coreContext.core.callsNb > 0) {
                Log.i("[Call Activity] Conference no longer exists, changing fragment")
                navigateToActiveCall()
            }
        }

        checkPermissions()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (coreContext.core.currentCall?.currentParams?.videoEnabled() == true) {
            Log.i("[Call Activity] Entering PiP mode")
            Compatibility.enterPipMode(this, conferenceViewModel.conferenceExists.value == true)
        }
    }

    override fun onPictureInPictureModeChanged(
            isInPictureInPictureMode: Boolean,
            newConfig: Configuration
    ) {
        Log.i("[Call Activity] Is in PiP mode? $isInPictureInPictureMode")
        if (::controlsViewModel.isInitialized) {
            // To hide UI except for TextureViews
            controlsViewModel.pipMode.value = isInPictureInPictureMode
        }
    }

    override fun onResume() {
        super.onResume()

        if (coreContext.core.callsNb == 0) {
            Log.w("[Call Activity] Resuming but no call found...")
            if (isTaskRoot) {
                // When resuming app from recent tasks make sure MainActivity will be launched if there is no call
                val intent = Intent()
                intent.setClass(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                finish()
            }
            return
        }
        coreContext.removeCallOverlay()

        val currentCall = coreContext.core.currentCall
        when (currentCall?.state) {
            Call.State.OutgoingInit, Call.State.OutgoingEarlyMedia, Call.State.OutgoingProgress, Call.State.OutgoingRinging -> {
                //navigateToOutgoingCall()
            }
            Call.State.IncomingReceived, Call.State.IncomingEarlyMedia -> {
                val earlyMediaVideoEnabled = corePreferences.acceptEarlyMedia &&
                        currentCall.state == Call.State.IncomingEarlyMedia &&
                        currentCall.currentParams.videoEnabled()
                navigateToIncomingCall(earlyMediaVideoEnabled)
            }
            else -> {}
        }
    }

    override fun onPause() {
        val core = coreContext.core
        if (core.callsNb > 0) {
            coreContext.createCallOverlay()
        }

        super.onPause()
    }

    override fun onDestroy() {
        coreContext.core.nativeVideoWindowId = null
        coreContext.core.nativePreviewWindowId = null

        super.onDestroy()
    }

    private fun checkPermissions() {
        val permissionsRequiredList = arrayListOf<String>()

        if (!PermissionHelper.get().hasRecordAudioPermission()) {
            Log.i("[Call Activity] Asking for RECORD_AUDIO permission")
            permissionsRequiredList.add(Manifest.permission.RECORD_AUDIO)
        }

        if (callsViewModel.currentCallData.value?.call?.currentParams?.videoEnabled() == true &&
                !PermissionHelper.get().hasCameraPermission()
        ) {
            Log.i("[Call Activity] Asking for CAMERA permission")
            permissionsRequiredList.add(Manifest.permission.CAMERA)
        }

        if (Version.sdkAboveOrEqual(Version.API31_ANDROID_12) && !PermissionHelper.get().hasBluetoothConnectPermission()) {
            Log.i("[Call Activity] Asking for BLUETOOTH_CONNECT permission")
            permissionsRequiredList.add(Compatibility.BLUETOOTH_CONNECT)
        }

        if (permissionsRequiredList.isNotEmpty()) {
            val permissionsRequired = arrayOfNulls<String>(permissionsRequiredList.size)
            permissionsRequiredList.toArray(permissionsRequired)
            requestPermissions(permissionsRequired, 0)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == 0) {
            for (i in permissions.indices) {
                when (permissions[i]) {
                    Manifest.permission.RECORD_AUDIO -> if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("[Call Activity] RECORD_AUDIO permission has been granted")
                        callsViewModel.updateMicState()
                    }
                    Manifest.permission.CAMERA -> if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("[Call Activity] CAMERA permission has been granted")
                        coreContext.core.reloadVideoDevices()
                        controlsViewModel.toggleVideo()
                    }
                    Compatibility.BLUETOOTH_CONNECT -> if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.i("[Call Activity] BLUETOOTH_CONNECT permission has been granted")
                    }
                }
            }
        } else if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("[Call Activity] WRITE_EXTERNAL_STORAGE permission has been granted, taking snapshot")
                callsViewModel.takeSnapshot()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onLayoutChanges(foldingFeature: FoldingFeature?) {
        foldingFeature ?: return
        Log.i("[Call Activity] Folding feature state changed: ${foldingFeature.state}, orientation is ${foldingFeature.orientation}")

        controlsViewModel.foldingState.value = foldingFeature
    }
}
