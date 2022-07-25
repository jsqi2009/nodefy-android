/*
 * Copyright (c) 2020 New Vector Ltd
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

package im.vector.app.features.roomprofile.uploads

import android.os.Build
import androidx.annotation.RequiresApi
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.R
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.core.resources.ColorProvider
import im.vector.app.core.resources.StringProvider
import im.vector.app.features.html.EventHtmlRenderer
import kotlinx.coroutines.launch
import me.gujun.android.span.span
import org.commonmark.node.Document
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessageTextContent
import org.matrix.android.sdk.api.session.room.model.message.MessageType
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import org.matrix.android.sdk.api.session.room.timeline.getTextDisplayableContent
import org.matrix.android.sdk.flow.flow
import org.matrix.android.sdk.flow.unwrap
import timber.log.Timber
import java.sql.Time

class RoomUploadsViewModel @AssistedInject constructor(
        @Assisted initialState: RoomUploadsViewState,
        private val session: Session,
        private val htmlRenderer: Lazy<EventHtmlRenderer>,
        private val stringProvider: StringProvider,
        private val colorProvider: ColorProvider,
) : VectorViewModel<RoomUploadsViewState, RoomUploadsAction, RoomUploadsViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<RoomUploadsViewModel, RoomUploadsViewState> {
        override fun create(initialState: RoomUploadsViewState): RoomUploadsViewModel
    }

    companion object : MavericksViewModelFactory<RoomUploadsViewModel, RoomUploadsViewState> by hiltMavericksViewModelFactory()

    private val room = session.getRoom(initialState.roomId)!!

    init {
        observeRoomSummary()
        // Send a first request
        handleLoadMore()
    }

    private fun observeRoomSummary() {
        room.flow().liveRoomSummary()
                .unwrap()
                .execute { async ->
                    copy(roomSummary = async)
                }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun handleLoadMore() = withState { state ->
        if (state.asyncEventsRequest is Loading) return@withState
        if (!state.hasMore) return@withState

        setState {
            copy(
                    asyncEventsRequest = Loading()
            )
        }

        viewModelScope.launch {
            try {
                val result = room.uploadsService().getUploads(20, token)
                val allEvent = room.timelineService().getAttachmentMessages()
                Timber.e("all event size=======${allEvent.size}")
                allEvent.forEach {
                    eventAction(it)
                }

                token = result.nextToken
                Timber.e("upload events---->${result.uploadEvents}")
                val groupedUploadEvents = result.uploadEvents
                        .groupBy {
                            it.contentWithAttachmentContent.msgType == MessageType.MSGTYPE_IMAGE ||
                                    it.contentWithAttachmentContent.msgType == MessageType.MSGTYPE_VIDEO
                        }

                setState {
                    copy(
                            asyncEventsRequest = Success(Unit),
                            mediaEvents = this.mediaEvents + groupedUploadEvents[true].orEmpty(),
                            fileEvents = this.fileEvents + groupedUploadEvents[false].orEmpty(),
                            hasMore = result.hasMore
                    )
                }
            } catch (failure: Throwable) {
                _viewEvents.post(RoomUploadsViewEvents.Failure(failure))
                setState {
                    copy(
                            asyncEventsRequest = Fail(failure)
                    )
                }
            }
        }
    }

    private var token: String? = null

    override fun handle(action: RoomUploadsAction) {
        when (action) {
            is RoomUploadsAction.Download -> handleDownload(action)
            is RoomUploadsAction.Share    -> handleShare(action)
            RoomUploadsAction.Retry       -> handleLoadMore()
            RoomUploadsAction.LoadMore    -> handleLoadMore()
        }
    }

    private fun handleShare(action: RoomUploadsAction.Share) {
        viewModelScope.launch {
            val event = try {
                val file = session.fileService().downloadFile(
                        messageContent = action.uploadEvent.contentWithAttachmentContent
                )
                RoomUploadsViewEvents.FileReadyForSharing(file)
            } catch (failure: Throwable) {
                RoomUploadsViewEvents.Failure(failure)
            }
            _viewEvents.post(event)
        }
    }

    private fun handleDownload(action: RoomUploadsAction.Download) {
        viewModelScope.launch {
            val event = try {
                val file = session.fileService().downloadFile(
                        messageContent = action.uploadEvent.contentWithAttachmentContent
                )
                RoomUploadsViewEvents.FileReadyForSaving(file, action.uploadEvent.contentWithAttachmentContent.body)
            } catch (failure: Throwable) {
                RoomUploadsViewEvents.Failure(failure)
            }
            _viewEvents.post(event)
        }
    }

    private fun eventAction(timelineEvent : TimelineEvent) {

        val senderName = timelineEvent.senderInfo.disambiguatedDisplayName
        val appendAuthor = false

        timelineEvent.getLastMessageContent()?.let { messageContent ->
            when (messageContent.msgType) {
                MessageType.MSGTYPE_TEXT                 -> {
                    val body = messageContent.getTextDisplayableContent()
                    if (messageContent is MessageTextContent && messageContent.matrixFormattedBody.isNullOrBlank().not()) {
                        val localFormattedBody = htmlRenderer.get().parse(body) as Document
                        val renderedBody = htmlRenderer.get().render(localFormattedBody) ?: body
                        simpleFormat(senderName, renderedBody, appendAuthor)
                        Timber.e("messageContent------>$renderedBody")
                    } else {
                        simpleFormat(senderName, body, appendAuthor)
                        Timber.e("messageContent------>$body")
                    }
                }
                MessageType.MSGTYPE_VERIFICATION_REQUEST -> {
                    simpleFormat(senderName, stringProvider.getString(R.string.verification_request), appendAuthor)
                }
                MessageType.MSGTYPE_IMAGE                -> {
                    simpleFormat(senderName, stringProvider.getString(R.string.sent_an_image), appendAuthor)
                }
                MessageType.MSGTYPE_AUDIO                -> {
                    if ((messageContent as? MessageAudioContent)?.voiceMessageIndicator != null) {
                        simpleFormat(senderName, stringProvider.getString(R.string.sent_a_voice_message), appendAuthor)
                    } else {
                        simpleFormat(senderName, stringProvider.getString(R.string.sent_an_audio_file), appendAuthor)
                    }
                }
                MessageType.MSGTYPE_VIDEO                -> {
                    simpleFormat(senderName, stringProvider.getString(R.string.sent_a_video), appendAuthor)
                }
                MessageType.MSGTYPE_FILE                 -> {
                    simpleFormat(senderName, stringProvider.getString(R.string.sent_a_file), appendAuthor)
                }
                MessageType.MSGTYPE_LOCATION             -> {
                    simpleFormat(senderName, stringProvider.getString(R.string.sent_location), appendAuthor)
                }
                else                                     -> {
                    simpleFormat(senderName, messageContent.body, appendAuthor)
                    Timber.e("messageContent------>${messageContent.body}")
                }
            }
        } ?: span { }
    }

    private fun simpleFormat(senderName: String, body: CharSequence, appendAuthor: Boolean): CharSequence {
        return if (appendAuthor) {
            span {
                text = senderName
                textColor = colorProvider.getColorFromAttribute(R.attr.vctr_content_primary)
            }
                    .append(": ")
                    .append(body)
        } else {
            body
        }
    }
}
