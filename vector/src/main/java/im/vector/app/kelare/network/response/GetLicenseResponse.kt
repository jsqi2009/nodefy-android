package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.LicenseInfo

/**
 * @author jsqi
 */
class GetLicenseResponse: BResponse() {

    var license: ArrayList<LicenseInfo>? = null
}
