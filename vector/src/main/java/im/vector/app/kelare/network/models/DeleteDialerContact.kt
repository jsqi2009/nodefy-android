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

class DeleteDialerContact : Serializable{

    var user_id: String? = null
    var dialer_contacts: ArrayList<ContactID>? = ArrayList()

    override fun toString(): String {
        return "DeleteDialerContact(user_id=$user_id, dialer_contacts=$dialer_contacts)"
    }
}

class ContactID : Serializable{
    var id: String? = null
}
