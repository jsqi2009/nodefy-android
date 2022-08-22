package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  desc   :
 */
class AccountContactInfo:Serializable {

    var contacts_id: String? = null
    var contacts_type: String? = null
    var displayname: String? = null
    var avatar_url: String? = null

    override fun toString(): String {
        return "AccountContactInfo(contacts_id=$contacts_id, contacts_type=$contacts_type, displayname=$displayname, avatar_url=$avatar_url)"
    }
}
