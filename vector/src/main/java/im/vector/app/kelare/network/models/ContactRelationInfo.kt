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
 *  date   : 2022/8/23 16:26
 *  desc   :
 */
class ContactRelationInfo: Serializable {

    var user_id: String? = null
    var parent_user_id: String? = null
    var account_type: String? = null
    var is_main: Boolean? = false
}
