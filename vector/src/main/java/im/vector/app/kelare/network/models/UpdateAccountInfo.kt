package im.vector.app.kelare.network.models

import im.vector.app.kelare.network.models.DialerAccountInfo
import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/13/22 2:13 PM
 *  desc   :
 */
class UpdateAccountInfo:Serializable {

    var primary_user_id: String? = null
    var sip_account: DialerAccountInfo? = null

}
