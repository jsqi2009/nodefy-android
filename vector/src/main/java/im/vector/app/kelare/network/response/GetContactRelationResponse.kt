package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.response.BResponse

/**
 * @author jsqi
 */
class GetContactRelationResponse: BResponse() {
    var children_users: ArrayList<ContactRelationInfo> = ArrayList()
}
