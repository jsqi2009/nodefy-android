package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/24/22
 *  desc   :
 */
class PhoneInfo:Serializable {

    var number: String? = null
    var isDefault: Boolean? = false

    constructor(number: String?, isDefault: Boolean?) {
        this.number = number
        this.isDefault = isDefault
    }

    override fun toString(): String {
        return "PhoneInfo(number=$number, isDefault=$isDefault)"
    }


}
