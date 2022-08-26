package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  desc   :
 */
class ContactChannelInfo:Serializable {

    var contacts_id: String? = null
    var contacts_type: String? = null
    var displayType: String? = null
    var isDefault: Boolean = false
    override fun toString(): String {
        return "ContactChannelInfo(contacts_id=$contacts_id, contacts_type=$contacts_type, displayType=$displayType, isDefault=$isDefault)"
    }
}
