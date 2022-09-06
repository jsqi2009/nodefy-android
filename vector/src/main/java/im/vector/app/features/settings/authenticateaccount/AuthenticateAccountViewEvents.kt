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

package im.vector.app.features.settings.authenticateaccount

import im.vector.app.core.platform.VectorViewEvents
import org.matrix.android.sdk.api.auth.registration.RegistrationFlowResponse

/**
 * author : Jason
 *  date   : 2022/9/6 18:41
 *  desc   :
 */
sealed class AuthenticateAccountViewEvents : VectorViewEvents {
    data class Loading(val message: CharSequence? = null) : AuthenticateAccountViewEvents()
    object InvalidAuth : AuthenticateAccountViewEvents()
    data class OtherFailure(val throwable: Throwable) : AuthenticateAccountViewEvents()
    object Done : AuthenticateAccountViewEvents()
    data class RequestReAuth(val registrationFlowResponse: RegistrationFlowResponse, val lastErrorCode: String?) : AuthenticateAccountViewEvents()

    data class AuthType(val type: CharSequence? = null) : AuthenticateAccountViewEvents()
}
