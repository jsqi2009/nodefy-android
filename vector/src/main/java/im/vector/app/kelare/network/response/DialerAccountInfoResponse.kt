package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class DialerAccountInfoResponse: BResponse() {

    var sip_accounts: ArrayList<DialerAccountInfo>? = null
}
