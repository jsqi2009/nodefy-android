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

package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 2022/3/16 15:51
 *  desc   :
 */
class SipChatRoom : Serializable{

    var localUserName: String? = null
    var localDomain: String? = null
    var peerUserName: String? = null
    var peerDomain: String? = null
    var lastMessage: String? = null
    override fun toString(): String {
        return "SipChatRoom(localUserName=$localUserName, localDomain=$localDomain, peerUserName=$peerUserName, peerDomain=$peerDomain, lastMessage=$lastMessage)"
    }
}
