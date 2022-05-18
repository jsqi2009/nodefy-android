package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/13/22 2:13 PM
 *  desc   :
 */
class DeleteAccountInfo:Serializable {

    var primary_user_id: String? = null
    var sip_accounts: ArrayList<ItemInfo>? = ArrayList()

}

class ItemInfo:Serializable {

    var username: String? = null
    var domain: String? = null
    var type_value: String? = null

}
