package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/25/22
 *  desc   :
 */
class DeleteDialerContactInfo:Serializable {


    var user_id: String? = null
    var dialer_contacts: ArrayList<DialerContact>? = null

    inner class DialerContact:Serializable {
        var id: String? = null   //the id of dialer contact
    }

    override fun toString(): String {
        return "DeleteDialerContactInfo(user_id=$user_id, dialer_contacts=$dialer_contacts)"
    }

}
