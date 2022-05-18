package im.vector.app.kelare.network.models

import im.vector.app.kelare.network.models.PhoneInfo
import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/24/22
 *  desc   :
 */
class UpdateDialerContactInfo:Serializable {

    var id: String? = null   //the id of dialer contact
    var user_id: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var note: String? = null
    var online_phone: ArrayList<PhoneInfo>? = null   //work Ext
    var phone: ArrayList<PhoneInfo>? = null   //phone number
    override fun toString(): String {
        return "UpdateDialerContactInfo(id=$id, user_id=$user_id, first_name=$first_name, last_name=$last_name, note=$note, online_phone=$online_phone, phone=$phone)"
    }


}
