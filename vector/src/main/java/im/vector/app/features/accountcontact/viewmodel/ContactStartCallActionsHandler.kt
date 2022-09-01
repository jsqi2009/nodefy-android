/*
 * Copyright (c) 2021 New Vector Ltd
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

package im.vector.app.features.accountcontact.viewmodel

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.withState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import im.vector.app.R
import im.vector.app.core.utils.PERMISSIONS_FOR_AUDIO_IP_CALL
import im.vector.app.core.utils.PERMISSIONS_FOR_VIDEO_IP_CALL
import im.vector.app.core.utils.checkPermissions
import im.vector.app.features.call.webrtc.WebRtcCallManager
import im.vector.app.features.home.room.detail.RoomDetailAction
import im.vector.app.features.home.room.detail.TimelineViewModel
import im.vector.app.features.settings.VectorPreferences

class ContactStartCallActionsHandler(
        private val roomId: String,
        private val mContext: Context,
        private val callManager: WebRtcCallManager,
        private val vectorPreferences: VectorPreferences,
        private val contactCallViewModel: ContactCallViewModel,
        private val startCallActivityResultLauncher: ActivityResultLauncher<Array<String>>,
        private val showDialogWithMessage: (String) -> Unit,
        private val onTapToReturnToCall: () -> Unit) {

    fun onVoiceCallClicked() {
        handleCallRequest(false)
    }

    private fun handleCallRequest(isVideoCall: Boolean) = withState(contactCallViewModel) { state ->
        val roomSummary = state.asyncRoomSummary.invoke() ?: return@withState
        when (roomSummary.joinedMembersCount) {
            1    -> {
                val pendingInvite = roomSummary.invitedMembersCount ?: 0 > 0
                if (pendingInvite) {
                    // wait for other to join
                    showDialogWithMessage(mContext.getString(R.string.cannot_call_yourself_with_invite))
                } else {
                    // You cannot place a call with yourself.
                    showDialogWithMessage(mContext.getString(R.string.cannot_call_yourself))
                }
            }
            2    -> {
                val currentCall = callManager.getCurrentCall()
                if (currentCall?.signalingRoomId == roomId) {
                    onTapToReturnToCall()
                } else if (!state.isAllowedToStartWebRTCCall) {
                    showDialogWithMessage(
                            mContext.getString(
                                    if (state.isDm()) {
                                        R.string.no_permissions_to_start_webrtc_call_in_direct_room
                                    } else {
                                        R.string.no_permissions_to_start_webrtc_call
                                    }
                            )
                    )
                } else {
                    if (state.isDm()) {
                        safeStartCall(isVideoCall)
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun safeStartCall(isVideoCall: Boolean) {
        if (vectorPreferences.preventAccidentalCall()) {
            MaterialAlertDialogBuilder(mContext)
                    .setMessage(if (isVideoCall) R.string.start_video_call_prompt_msg else R.string.start_voice_call_prompt_msg)
                    .setPositiveButton(if (isVideoCall) R.string.start_video_call else R.string.start_voice_call) { _, _ ->
                        safeStartCall2(isVideoCall)
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        } else {
            safeStartCall2(isVideoCall)
        }
    }

    private fun safeStartCall2(isVideoCall: Boolean) {
        val startCallAction = RoomDetailAction.StartCall(isVideoCall)
        contactCallViewModel.pendingAction = startCallAction
        if (isVideoCall) {
            if (checkPermissions(
                            PERMISSIONS_FOR_VIDEO_IP_CALL,
                            mContext as Activity,
                            startCallActivityResultLauncher,
                            R.string.permissions_rationale_msg_camera_and_audio
                    )) {
                contactCallViewModel.pendingAction = null
                contactCallViewModel.handle(startCallAction)
            }
        } else {
            if (checkPermissions(
                            PERMISSIONS_FOR_AUDIO_IP_CALL,
                            mContext as Activity,
                            startCallActivityResultLauncher,
                            R.string.permissions_rationale_msg_record_audio
                    )) {
                contactCallViewModel.pendingAction = null
                contactCallViewModel.handle(startCallAction)
            }
        }
    }
}
