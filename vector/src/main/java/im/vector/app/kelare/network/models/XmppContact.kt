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

import org.jxmpp.jid.BareJid
import java.io.Serializable

/**
 * author : Jason
 *  date   : 2022/3/9 13:51
 *  desc   :
 */
class XmppContact : Serializable{

    var jid: BareJid? = null
    var login_user_jid: String? = null
    var login_user: String? = null
    var login_account: String? = null
    var isAvailable: Boolean? = null
    override fun toString(): String {
        return "XmppContact(jid=$jid, login_user_jid=$login_user_jid, login_user=$login_user, login_account=$login_account, isAvailable=$isAvailable)"
    }
}
