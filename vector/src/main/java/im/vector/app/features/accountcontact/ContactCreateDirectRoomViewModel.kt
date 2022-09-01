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

package im.vector.app.features.accountcontact

import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.mvrx.runCatchingToAsync
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.features.analytics.AnalyticsTracker
import im.vector.app.features.analytics.plan.CreatedRoom
import im.vector.app.features.createdirect.CreateDirectRoomAction
import im.vector.app.features.createdirect.CreateDirectRoomViewEvents
import im.vector.app.features.createdirect.CreateDirectRoomViewState
import im.vector.app.features.raw.wellknown.getElementWellknown
import im.vector.app.features.raw.wellknown.isE2EByDefault
import im.vector.app.features.userdirectory.PendingSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.extensions.orFalse
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.getUser
import org.matrix.android.sdk.api.session.permalinks.PermalinkData
import org.matrix.android.sdk.api.session.permalinks.PermalinkParser
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.user.model.User

/**
 * author : Jason
 *  date   : 2022/8/30 16:34
 *  desc   :
 */
class ContactCreateDirectRoomViewModel @AssistedInject constructor(
        @Assisted initialState: CreateDirectRoomViewState,
        private val rawService: RawService,
        val session: Session,
        val analyticsTracker: AnalyticsTracker
) : VectorViewModel<CreateDirectRoomViewState, CreateDirectRoomAction, CreateDirectRoomViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<ContactCreateDirectRoomViewModel, CreateDirectRoomViewState> {
        override fun create(initialState: CreateDirectRoomViewState): ContactCreateDirectRoomViewModel
    }

    companion object : MavericksViewModelFactory<ContactCreateDirectRoomViewModel, CreateDirectRoomViewState> by hiltMavericksViewModelFactory()

    /**
     * If users already have a DM room then navigate to it instead of creating a new room.
     */
    fun onSubmitInvitees(userId: String) {
        val existingRoomId = session.roomService().getExistingDirectRoomWithUser(userId)
        if (existingRoomId != null) {
            // Do not create a new DM, just tell that the creation is successful by passing the existing roomId
            setState {
                copy(createAndInviteState = Success(existingRoomId))
            }
        } else {
            // Create the DM
            createRoomAndInviteSelectedUsers(userId)
        }
    }

    private fun createRoomAndInviteSelectedUsers(userId: String) {
        setState { copy(createAndInviteState = Loading()) }

        viewModelScope.launch(Dispatchers.IO) {
            val adminE2EByDefault = rawService.getElementWellknown(session.sessionParams)
                    ?.isE2EByDefault()
                    ?: true

            val roomParams = CreateRoomParams()
                    .apply {
                        invitedUserIds.add(userId)
                        setDirectMessage()
                        enableEncryptionIfInvitedUsersSupportIt = adminE2EByDefault
                    }

            val result = runCatchingToAsync {
                session.roomService().createRoom(roomParams)
            }
            analyticsTracker.capture(CreatedRoom(isDM = roomParams.isDirect.orFalse()))

            setState {
                copy(
                        createAndInviteState = result
                )
            }
        }
    }

    override fun handle(action: CreateDirectRoomAction) {

    }
}
