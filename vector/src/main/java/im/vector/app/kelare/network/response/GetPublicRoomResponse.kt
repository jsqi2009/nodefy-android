package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.PublicRoomInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class GetPublicRoomResponse: BResponse() {
    var data: PublicRoomInfo = PublicRoomInfo()
}
