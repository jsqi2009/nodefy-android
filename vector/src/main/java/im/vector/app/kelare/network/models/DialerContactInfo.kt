package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/24/22
 *  desc   :
 */
class DialerContactInfo:Serializable {

    var user_id: String? = null
    var id: String? = null
    var first_name: String? = null
    var last_name: String? = null
    var note: String? = null
    var online_phone: ArrayList<PhoneInfo>? = ArrayList()   //work Ext
    var phone: ArrayList<PhoneInfo>? = ArrayList()   //phone number
    var isSelected: Boolean? = false
    override fun toString(): String {
        return "DialerContactInfo(user_id=$user_id, id=$id, first_name=$first_name, last_name=$last_name, note=$note, online_phone=$online_phone, phone=$phone)"
    }


}
