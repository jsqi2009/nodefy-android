package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class SipTransportInfo: Serializable {

    var name: String? = null
    var isCheck: Boolean? = false

    constructor(name: String?, isCheck: Boolean?) {
        this.name = name
        this.isCheck = isCheck
    }

    override fun toString(): String {
        return "SipTransportInfo(name=$name, isCheck=$isCheck)"
    }


}
