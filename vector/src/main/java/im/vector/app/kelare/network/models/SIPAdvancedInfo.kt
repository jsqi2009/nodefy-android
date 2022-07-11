package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/11/22 3:59 PM
 *  desc   :
 */
class SIPAdvancedInfo:Serializable {

    //SIP
    var outProxy: String? = null
    var authName: String? = null
    var incomingCalls: Boolean = false
    var refreshInterval: String? = null
    var interval: String? = null
    var sipTransport: String? = null
    var encryptMedia: String? = null
    var sipPortStart: String? = null
    var sipPortEnd: String? = null
    var rtpVideoPortEnd: String? = null
    var rtpVideoPortStart: String? = null
    var rtpAudioPortStart: String? = null
    var rtpAudioPortEnd: String? = null
    var tlsEnable: Boolean = false
    var displayAs : String? = null
    override fun toString(): String {
        return "SIPAdvancedInfo(outProxy=$outProxy, authName=$authName, incomingCalls=$incomingCalls, refreshInterval=$refreshInterval, interval=$interval, sipTransport=$sipTransport, encryptMedia=$encryptMedia, sipPortStart=$sipPortStart, sipPortEnd=$sipPortEnd, rtpVideoPortEnd=$rtpVideoPortEnd, rtpVideoPortStart=$rtpVideoPortStart, rtpAudioPortStart=$rtpAudioPortStart, rtpAudioPortEnd=$rtpAudioPortEnd, tlsEnable=$tlsEnable, displayAs=$displayAs)"
    }
}
