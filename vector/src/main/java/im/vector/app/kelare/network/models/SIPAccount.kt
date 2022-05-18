package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/11/22 3:59 PM
 *  desc   :
 */
class SIPAccount:Serializable {

    var account_name: String? = null
    var status: Int? = null

    override fun toString(): String {
        return "SIPAccount(account_name=$account_name, status=$status)"
    }
}
