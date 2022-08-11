/*
 * Copyright (c) 2010-2021 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package im.vector.app.kelare.voip.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import im.vector.app.VectorApplication.Companion.coreContext
import org.linphone.core.*
import org.linphone.core.tools.Log
import im.vector.app.VectorApplication.Companion.corePreferences
import im.vector.app.kelare.utils.AppUtils
import im.vector.app.kelare.utils.SipCallEvent
import im.vector.app.kelare.voip.data.ConferenceParticipantData
import im.vector.app.kelare.voip.data.ConferenceParticipantDeviceData
import im.vector.app.R
import im.vector.app.kelare.utils.SipUtils

class ConferenceViewModel : ViewModel() {
    val conferenceExists = MutableLiveData<Boolean>()
    val subject = MutableLiveData<String>()
    val isConferenceLocallyPaused = MutableLiveData<Boolean>()
    val isVideoConference = MutableLiveData<Boolean>()
    val isMeAdmin = MutableLiveData<Boolean>()

    val conference = MutableLiveData<Conference>()
    val conferenceCreationPending = MutableLiveData<Boolean>()
    val conferenceParticipants = MutableLiveData<List<ConferenceParticipantData>>()
    val conferenceParticipantDevices = MutableLiveData<List<ConferenceParticipantDeviceData>>()
    val conferenceDisplayMode = MutableLiveData<ConferenceDisplayMode>()

    val isRecording = MutableLiveData<Boolean>()
    val isRemotelyRecorded = MutableLiveData<Boolean>()

    val maxParticipantsForMosaicLayout = corePreferences.maxConferenceParticipantsForMosaicLayout

    val speakingParticipant = MutableLiveData<ConferenceParticipantDeviceData>()

    val participantAdminStatusChangedEvent: MutableLiveData<SipCallEvent<ConferenceParticipantData>> by lazy {
        MutableLiveData<SipCallEvent<ConferenceParticipantData>>()
    }

    val firstToJoinEvent: MutableLiveData<SipCallEvent<Boolean>> by lazy {
        MutableLiveData<SipCallEvent<Boolean>>()
    }

    val allParticipantsLeftEvent: MutableLiveData<SipCallEvent<Boolean>> by lazy {
        MutableLiveData<SipCallEvent<Boolean>>()
    }

    private val conferenceListener = object : ConferenceListenerStub() {
        override fun onParticipantAdded(conference: Conference, participant: Participant) {
            Log.i("[Conference] Participant added: ${participant.address.asStringUriOnly()}")
            updateParticipantsList(conference)
        }

        override fun onParticipantRemoved(conference: Conference, participant: Participant) {
            Log.i("[Conference] Participant removed: ${participant.address.asStringUriOnly()}")
            updateParticipantsList(conference)

            if (conferenceParticipants.value.orEmpty().isEmpty()) {
                allParticipantsLeftEvent.value = SipCallEvent(true)
                // TODO: FIXME: Temporary workaround when alone in a conference in active speaker layout
                val meDeviceData = conferenceParticipantDevices.value.orEmpty().firstOrNull()
                if (meDeviceData != null) {
                    speakingParticipant.value = meDeviceData!!
                }
            }
        }

        override fun onParticipantDeviceAdded(
                conference: Conference,
                participantDevice: ParticipantDevice
        ) {
            Log.i("[Conference] Participant device added: ${participantDevice.address.asStringUriOnly()}")
            addParticipantDevice(participantDevice)
        }

        override fun onParticipantDeviceRemoved(
                conference: Conference,
                participantDevice: ParticipantDevice
        ) {
            Log.i("[Conference] Participant device removed: ${participantDevice.address.asStringUriOnly()}")
            removeParticipantDevice(participantDevice)
        }

        override fun onParticipantAdminStatusChanged(
                conference: Conference,
                participant: Participant
        ) {
            Log.i("[Conference] Participant admin status changed [${participant.address.asStringUriOnly()}] is ${if (participant.isAdmin) "now admin" else "no longer admin"}")
            isMeAdmin.value = conference.me.isAdmin
            updateParticipantsList(conference)

            if (conference.me.address.weakEqual(participant.address)) {
                Log.i("[Conference] Found me participant [${participant.address.asStringUriOnly()}]")
                val participantData = ConferenceParticipantData(conference, participant)
                participantAdminStatusChangedEvent.value = SipCallEvent(participantData)
                return
            }

            val participantData = conferenceParticipants.value.orEmpty().find { data -> data.participant.address.weakEqual(participant.address) }
            if (participantData != null) {
                participantAdminStatusChangedEvent.value = SipCallEvent(participantData)
            } else {
                Log.w("[Conference] Failed to find participant [${participant.address.asStringUriOnly()}] in conferenceParticipants list")
            }
        }

        override fun onSubjectChanged(conference: Conference, subject: String) {
            Log.i("[Conference] Subject changed: $subject")
            this@ConferenceViewModel.subject.value = subject
        }

        override fun onStateChanged(conference: Conference, state: Conference.State) {
            Log.i("[Conference] State changed: $state")
            isVideoConference.value = conference.currentParams.isVideoEnabled

            when (state) {
                Conference.State.Created -> {
                    configureConference(conference)
                    conferenceCreationPending.value = false
                }
                Conference.State.TerminationPending -> {
                    terminateConference(conference)
                }
                else -> {}
            }
        }
    }

    private val listener = object : CoreListenerStub() {
        override fun onConferenceStateChanged(
                core: Core,
                conference: Conference,
                state: Conference.State
        ) {
            Log.i("[Conference] Conference state changed: $state")
            if (state == Conference.State.Instantiated) {
                conferenceCreationPending.value = true
                initConference(conference)
            }
        }
    }

    init {
        coreContext.core.addListener(listener)

        conferenceParticipants.value = arrayListOf()
        conferenceParticipantDevices.value = arrayListOf()

        subject.value = AppUtils.getString(R.string.conference_default_title)

        var conference = coreContext.core.conference ?: coreContext.core.currentCall?.conference
        if (conference == null) {
            for (call in coreContext.core.calls) {
                if (call.conference != null) {
                    conference = call.conference
                    break
                }
            }
        }
    }

    override fun onCleared() {
        coreContext.core.removeListener(listener)
        conference.value?.removeListener(conferenceListener)

        conferenceParticipants.value.orEmpty().forEach(ConferenceParticipantData::destroy)
        conferenceParticipantDevices.value.orEmpty().forEach(ConferenceParticipantDeviceData::destroy)

        super.onCleared()
    }

    fun pauseConference() {
        Log.i("[Conference] Leaving conference temporarily")
        conference.value?.leave()
    }

    fun resumeConference() {
        Log.i("[Conference] Entering conference again")
        conference.value?.enter()
    }

    fun initConference(conference: Conference) {
        conferenceExists.value = true

        this@ConferenceViewModel.conference.value = conference
        conference.addListener(conferenceListener)

        isRecording.value = conference.isRecording
        subject.value = SipUtils.getConferenceSubject(conference)
    }

    fun configureConference(conference: Conference) {
        updateParticipantsList(conference)
        if (conferenceParticipants.value.orEmpty().isEmpty()) {
            firstToJoinEvent.value = SipCallEvent(true)
        }
        updateParticipantsDevicesList(conference)

        isConferenceLocallyPaused.value = !conference.isIn
        isMeAdmin.value = conference.me.isAdmin
        isVideoConference.value = conference.currentParams.isVideoEnabled
        subject.value = SipUtils.getConferenceSubject(conference)
    }

    private fun terminateConference(conference: Conference) {
        conferenceExists.value = false
        isVideoConference.value = false

        conference.removeListener(conferenceListener)

        conferenceParticipants.value.orEmpty().forEach(ConferenceParticipantData::destroy)
        conferenceParticipantDevices.value.orEmpty().forEach(ConferenceParticipantDeviceData::destroy)
        conferenceParticipants.value = arrayListOf()
        conferenceParticipantDevices.value = arrayListOf()
    }

    private fun updateParticipantsList(conference: Conference) {
        conferenceParticipants.value.orEmpty().forEach(ConferenceParticipantData::destroy)
        val participants = arrayListOf<ConferenceParticipantData>()

        val participantsList = conference.participantList
        Log.i("[Conference] Conference has ${participantsList.size} participants")
        for (participant in participantsList) {
            val participantDevices = participant.devices
            Log.i("[Conference] Participant found: ${participant.address.asStringUriOnly()} with ${participantDevices.size} device(s)")

            val participantData = ConferenceParticipantData(conference, participant)
            participants.add(participantData)
        }

        conferenceParticipants.value = participants
    }

    private fun updateParticipantsDevicesList(conference: Conference) {
        conferenceParticipantDevices.value.orEmpty().forEach(ConferenceParticipantDeviceData::destroy)
        val devices = arrayListOf<ConferenceParticipantDeviceData>()

        val participantsList = conference.participantList
        Log.i("[Conference] Conference has ${participantsList.size} participants")
        for (participant in participantsList) {
            val participantDevices = participant.devices
            Log.i("[Conference] Participant found: ${participant.address.asStringUriOnly()} with ${participantDevices.size} device(s)")

            for (device in participantDevices) {
                Log.i("[Conference] Participant device found: ${device.name} (${device.address.asStringUriOnly()})")
                val deviceData = ConferenceParticipantDeviceData(device, false)
                devices.add(deviceData)
            }
        }
        if (devices.isNotEmpty()) {
            speakingParticipant.value = devices.first()
        }

        for (device in conference.me.devices) {
            Log.i("[Conference] Participant device for myself found: ${device.name} (${device.address.asStringUriOnly()})")
            val deviceData = ConferenceParticipantDeviceData(device, true)
            if (devices.isEmpty()) {
                // TODO: FIXME: Temporary workaround when alone in a conference in active speaker layout
                speakingParticipant.value = deviceData
            }
            devices.add(deviceData)
        }

        conferenceParticipantDevices.value = devices
    }

    private fun addParticipantDevice(device: ParticipantDevice) {
        val devices = arrayListOf<ConferenceParticipantDeviceData>()
        devices.addAll(conferenceParticipantDevices.value.orEmpty())

        val existingDevice = devices.find {
            it.participantDevice.address.weakEqual(device.address)
        }
        if (existingDevice != null) {
            Log.e("[Conference] Participant is already in devices list: ${device.name} (${device.address.asStringUriOnly()})")
            return
        }

        Log.i("[Conference] New participant device found: ${device.name} (${device.address.asStringUriOnly()})")
        val deviceData = ConferenceParticipantDeviceData(device, false)
        devices.add(deviceData)

        val sortedDevices = sortDevicesDataList(devices)

        if (speakingParticipant.value == null) {
            speakingParticipant.value = deviceData
        }

        conferenceParticipantDevices.value = sortedDevices
    }

    private fun removeParticipantDevice(device: ParticipantDevice) {
        val devices = arrayListOf<ConferenceParticipantDeviceData>()

        for (participantDevice in conferenceParticipantDevices.value.orEmpty()) {
            if (participantDevice.participantDevice.address.asStringUriOnly() != device.address.asStringUriOnly()) {
                devices.add(participantDevice)
            }
        }
        if (devices.size == conferenceParticipantDevices.value.orEmpty().size) {
            Log.e("[Conference] Failed to remove participant device: ${device.name} (${device.address.asStringUriOnly()})")
        } else {
            Log.i("[Conference] Participant device removed: ${device.name} (${device.address.asStringUriOnly()})")
        }

        conferenceParticipantDevices.value = devices
    }

    private fun sortDevicesDataList(devices: List<ConferenceParticipantDeviceData>): ArrayList<ConferenceParticipantDeviceData> {
        val sortedList = arrayListOf<ConferenceParticipantDeviceData>()
        sortedList.addAll(devices)

        val meDeviceData = sortedList.find {
            it.isMe
        }
        if (meDeviceData != null) {
            val index = sortedList.indexOf(meDeviceData)
            val expectedIndex = if (conferenceDisplayMode.value == ConferenceDisplayMode.ACTIVE_SPEAKER) {
                0
            } else {
                sortedList.size - 1
            }
            if (index != expectedIndex) {
                Log.i("[Conference] Me device data is at index $index, moving it to index $expectedIndex")
                sortedList.removeAt(index)
                sortedList.add(expectedIndex, meDeviceData)
            }
        }

        return sortedList
    }
}
