package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class AddSipPhoneBean: Serializable {


    var name: String? = null
    var phoneValue: String? = null

    constructor(name: String?, phoneValue: String?) {
        this.name = name
        this.phoneValue = phoneValue
    }

    override fun toString(): String {
        return "AddSipPhoneBean(name=$name, phoneValue=$phoneValue)"
    }


}
