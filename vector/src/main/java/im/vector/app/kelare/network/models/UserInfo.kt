package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class UserInfo: Serializable {

    var id: String? = null
    var reles: ArrayList<RolesInfo>? = null
    var name: String? = null
    var nickname: String? = null
    var phone: String? = null
    var email: String? = null
    var mob: String? = null
    var password: String? = null
    var pw_level: String? = null
    var corp: String? = null
    var addr: String? = null
    var trade: String? = null
    var tmp: String? = null
    var lang: String? = null
    var conf_sms: Int = 0
    var conf_push: Int = 0
    var conf_voice: Int = 0
    var conf_email: Int = 0
    var identity_type: String? = null
    var conf_disc_push: String? = null
    var conf_disc_sms: String? = null
    var is_agent: Int = 0
    var is_active: Boolean = false
    var is_superuser: Boolean = false
    var is_admin: Boolean = false
    var is_view: Boolean = false
    var last_login: String? = null
    var date_joined: String? = null

    inner class RolesInfo : Serializable {
        var name: String? = null
        var id: String? = null
        var role_type: String? = null
    }

    override fun toString(): String {
        return "UserInfo(id=$id, reles=$reles, name=$name, nickname=$nickname, phone=$phone, email=$email, mob=$mob, password=$password, pw_level=$pw_level, corp=$corp, addr=$addr, trade=$trade, tmp=$tmp, lang=$lang, conf_sms=$conf_sms, conf_push=$conf_push, conf_voice=$conf_voice, conf_email=$conf_email, identity_type=$identity_type, conf_disc_push=$conf_disc_push, conf_disc_sms=$conf_disc_sms, is_agent=$is_agent, is_active=$is_active, is_superuser=$is_superuser, is_admin=$is_admin, is_view=$is_view, last_login=$last_login, date_joined=$date_joined)"
    }


}
