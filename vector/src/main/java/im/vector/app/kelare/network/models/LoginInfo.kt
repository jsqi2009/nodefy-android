package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class LoginInfo: Serializable {

    var token: String? = null

    //error message
    var non_field_errors : ArrayList<String>? = null

    override fun toString(): String {
        return "LoginInfo(token=$token)"
    }



}
