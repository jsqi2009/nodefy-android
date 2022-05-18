package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.response.BResponse
import im.vector.app.kelare.network.models.DeviceInfo

/**
 * @author jsqi
 */
class DevicesListResponse: BResponse() {

    var data: ArrayList<DeviceInfo>? = null
}
