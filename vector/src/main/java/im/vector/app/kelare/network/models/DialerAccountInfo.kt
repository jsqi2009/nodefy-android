package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * author : Jason
 *  date   : 4/12/22 2:55 PM
 *  desc   :
 */
class DialerAccountInfo:Serializable {

    var id: Int? = null
    var account_name: String? = null
    var enabled: Boolean = false
    var is_default: Boolean = false
    var display_as: String? = null
    var username: String? = null
    var password: String? = null
    var domain: String? = null
    var url: String? = null
    var voice_mail: String? = null
    var type_value: String? = null
    var create_ts: String? = null
    var update_ts: String? = null
    var outproxy: String? = null
    var is_upload: Boolean = false
    var extension: Extension = Extension()

    inner class Extension : Serializable {

        //Universal
        var domain: String? = null
        var password: String? = null
        var enable: Boolean = false
        var username: String? = null
        var accountName: String? = null
        var isDefault: Boolean = false
        var interval: String? = null


        //XMPP
        var proxy: String? = null
        var verifyCert: Boolean = false
        var usePing: Boolean = false
        var isConnected: Boolean = false

        //SIP
        var rtpAudioPortEnd: String? = null
        var displayAs: String? = null
        var voiceNumber: String? = null
        var refreshInterval: String? = null
        var tlsEnable: Boolean = false
        var sipTransport: String? = null
        var forwardBusy: String? = null
        var forwardBusyToNumber: String? = null
        var encryptMedia: String? = null
        var authName: String? = null
        var rtpVideoPortEnd: String? = null
        var rtpVideoPortStart: String? = null
        var rtpAudioPortStart: String? = null
        var outProxy: String? = null
        var sipPortEnd: String? = null
        var forwardAlways: Boolean = false
        var forwardAlwaysToNumber: String? = null
        var sipPortStart: String? = null
        var forwardNoAnswer: String? = null
        var forwardNoAnswerToNumber: String? = null
        var incomingCalls: Boolean = false
        var delay: String? = null

        override fun toString(): String {
            return "Extension(domain=$domain, password=$password, enable=$enable, username=$username, accountName=$accountName, isDefault=$isDefault, interval=$interval, proxy=$proxy, verifyCert=$verifyCert, usePing=$usePing, isConnected=$isConnected, rtpAudioPortEnd=$rtpAudioPortEnd, displayAs=$displayAs, voiceNumber=$voiceNumber, refreshInterval=$refreshInterval, tlsEnable=$tlsEnable, sipTransport=$sipTransport, forwardBusy=$forwardBusy, forwardBusyToNumber=$forwardBusyToNumber, encryptMedia=$encryptMedia, authName=$authName, rtpVideoPortEnd=$rtpVideoPortEnd, rtpVideoPortStart=$rtpVideoPortStart, rtpAudioPortStart=$rtpAudioPortStart, outProxy=$outProxy, sipPortEnd=$sipPortEnd, forwardAlways=$forwardAlways, forwardAlwaysToNumber=$forwardAlwaysToNumber, sipPortStart=$sipPortStart, forwardNoAnswer=$forwardNoAnswer, forwardNoAnswerToNumber=$forwardNoAnswerToNumber, incomingCalls=$incomingCalls, delay=$delay)"
        }
    }

    override fun toString(): String {
        return "DialerAccountInfo(id=$id, account_name=$account_name, enabled=$enabled, is_default=$is_default, display_as=$display_as, username=$username, password=$password, domain=$domain, url=$url, voice_mail=$voice_mail, type_value=$type_value, create_ts=$create_ts, update_ts=$update_ts, outproxy=$outproxy, is_upload=$is_upload, extension=$extension)"
    }


}
