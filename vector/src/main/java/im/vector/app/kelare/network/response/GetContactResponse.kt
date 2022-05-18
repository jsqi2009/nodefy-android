package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class GetContactResponse: BResponse() {
    var data: ArrayList<DialerContactInfo> = ArrayList()
}
