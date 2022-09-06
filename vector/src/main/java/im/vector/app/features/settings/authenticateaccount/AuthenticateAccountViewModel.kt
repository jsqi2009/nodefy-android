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
package im.vector.app.features.settings.authenticateaccount

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.core.di.MavericksAssistedViewModelFactory
import im.vector.app.core.di.hiltMavericksViewModelFactory
import im.vector.app.core.platform.VectorViewModel
import im.vector.app.features.auth.ReAuthActivity
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.auth.UIABaseAuth
import org.matrix.android.sdk.api.auth.UserInteractiveAuthInterceptor
import org.matrix.android.sdk.api.auth.UserPasswordAuth
import org.matrix.android.sdk.api.auth.registration.RegistrationFlowResponse
import org.matrix.android.sdk.api.failure.isInvalidUIAAuth
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AuthenticateAccountViewState(
        val dummy: Boolean = false
) : MavericksState

class AuthenticateAccountViewModel @AssistedInject constructor(@Assisted private val initialState: AuthenticateAccountViewState,
                                                             private val session: Session) :
        VectorViewModel<AuthenticateAccountViewState, AuthenticateAccountAction, AuthenticateAccountViewEvents>(initialState) {

    @AssistedFactory
    interface Factory : MavericksAssistedViewModelFactory<AuthenticateAccountViewModel, AuthenticateAccountViewState> {
        override fun create(initialState: AuthenticateAccountViewState): AuthenticateAccountViewModel
    }

    companion object : MavericksViewModelFactory<AuthenticateAccountViewModel, AuthenticateAccountViewState> by hiltMavericksViewModelFactory()

    override fun handle(action: AuthenticateAccountAction) {
        when (action) {
            is AuthenticateAccountAction.AuthAccount -> handleAuthAccount(action)
            AuthenticateAccountAction.SsoAuthDone          -> {
                Timber.d("## UIA - FallBack success")
                _viewEvents.post(AuthenticateAccountViewEvents.Loading())
            }
            is AuthenticateAccountAction.PasswordAuthDone  -> {
                _viewEvents.post(AuthenticateAccountViewEvents.Loading())
            }
            AuthenticateAccountAction.ReAuthCancelled      -> {
                Timber.d("## UIA - Reauth cancelled")
            }
        }
    }

    private fun handleAuthAccount(action: AuthenticateAccountAction.AuthAccount) {
        _viewEvents.post(AuthenticateAccountViewEvents.AuthType(action.type))

        /*viewModelScope.launch {
            val event = try {
                session.accountService().deactivateAccount(
                        action.eraseAllData,
                        object : UserInteractiveAuthInterceptor {
                            override fun performStage(flowResponse: RegistrationFlowResponse, errCode: String?, promise: Continuation<UIABaseAuth>) {
                                _viewEvents.post(AuthenticateAccountViewEvents.RequestReAuth(flowResponse, errCode))
                            }
                        }
                )
                AuthenticateAccountViewEvents.Done
            } catch (failure: Throwable) {
                if (failure.isInvalidUIAAuth()) {
                    AuthenticateAccountViewEvents.InvalidAuth
                } else {
                    AuthenticateAccountViewEvents.OtherFailure(failure)
                }
            }

            _viewEvents.post(event)
        }*/
    }


}
