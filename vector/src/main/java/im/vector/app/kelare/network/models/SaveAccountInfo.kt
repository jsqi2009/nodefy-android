package im.vector.app.kelare.network.models

import im.vector.app.kelare.network.models.DialerAccountInfo
import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/13/22 2:13 PM
 *  desc   :
 */
public class SaveAccountInfo:Serializable {

    var sip_accounts: ArrayList<DialerAccountInfo>? = ArrayList()
    var primary_user_id: String? = null
}
