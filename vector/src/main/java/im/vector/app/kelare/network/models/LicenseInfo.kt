package im.vector.app.kelare.network.models

import java.io.Serializable


class LicenseInfo:Serializable {

    var id: Int? = 0
    var action: String? = null
    var create_ts: String? = null
    var update_ts: String? = null
    var delete_ts: String? = null
    var data: LicenseData? = null
    override fun toString(): String {
        return "LicenseInfo(action=$action, id=$id, create_ts=$create_ts, update_ts=$update_ts)"
    }


    class LicenseData:Serializable {

        var id: Int? = 0
        var orderId: String? = null
        var productId: Int? = 0
        var userId: Int? = 0
        var licenseKey: String? = null
        var expiresAt: String? = null
        var validFor: Int? = 0
        var source: Int? = 0
        var status: Int? = 0
        var timesActivated: Int? = 0
        var timesActivatedMax: Int? = 0
        var activatedAt: String? = null
        var deactivatedAt: String? = null
        var createdAt: String? = null
        var createdBy: Int? = 0
        var updatedAt: String? = null
        var updatedBy: Int? = 0
        var usersNumber: Int? = 0
        var info: String? = null
        var homeserver: String? = null
        var productInfo: String? = null
        var currentTime: String? = null
        var isInternal: Boolean? = false
        override fun toString(): String {
            return "LicenseData(id=$id, orderId=$orderId, productId=$productId, userId=$userId, licenseKey=$licenseKey, expiresAt=$expiresAt, validFor=$validFor, source=$source, status=$status, timesActivated=$timesActivated, timesActivatedMax=$timesActivatedMax, activatedAt=$activatedAt, deactivatedAt=$deactivatedAt, createdAt=$createdAt, createdBy=$createdBy, updatedAt=$updatedAt, updatedBy=$updatedBy, usersNumber=$usersNumber, info=$info, homeserver=$homeserver, productInfo=$productInfo, currentTime=$currentTime, isInternal=$isInternal)"
        }
    }
}
