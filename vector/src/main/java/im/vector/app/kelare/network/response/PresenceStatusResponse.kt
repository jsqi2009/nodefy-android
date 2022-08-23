package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class PresenceStatusResponse: BResponse() {
    var presence: String = ""
}
