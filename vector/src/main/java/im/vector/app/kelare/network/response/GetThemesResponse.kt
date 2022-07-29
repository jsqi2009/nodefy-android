package im.vector.app.kelare.network.response

import im.vector.app.kelare.network.models.LicenseInfo
import im.vector.app.kelare.network.models.ThemeInfo

/**
 * @author jsqi
 */
class GetThemesResponse: BResponse() {

    var themes: ArrayList<ThemeInfo> = ArrayList()
}
