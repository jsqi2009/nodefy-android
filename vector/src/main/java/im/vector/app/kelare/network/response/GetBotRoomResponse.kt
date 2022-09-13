package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.BotRoomInfo
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class GetBotRoomResponse: BResponse() {

    var data: BotRoomInfo? = null
}
