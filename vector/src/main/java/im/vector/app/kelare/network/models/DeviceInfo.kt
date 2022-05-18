package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class DeviceInfo: Serializable {

    var id: String? = null
    var name: String? = null
    var number: String? = null
    var ip_address: String? = null
    var cabinet_type: String? = null
    var position_info: String? = null
    var active: String? = null
    var create_time: String? = null
    var modify_time: String? = null
    var token: String? = null
    var area: String? = null
    var status: Int = 0


    inner class BrandInfo : Serializable {
        var id: String? = null
        var name: String? = null

    }

    inner class SeriesInfo : Serializable {
        var id: String? = null
        var name: String? = null

    }

    inner class ModelInfo : Serializable {
        var id: String? = null
        var name: String? = null

    }

    inner class PortInfo : Serializable {
        var id: String? = null
        var name: String? = null

    }

    inner class DeviceTypeInfo : Serializable {
        var type: String? = null
        var id: String? = null
        var name: String? = null
        var name_zh: String? = null

    }

    override fun toString(): String {
        return "DeviceInfo(id=$id, name=$name, number=$number, ip_address=$ip_address, cabinet_type=$cabinet_type, position_info=$position_info, active=$active, create_time=$create_time, modify_time=$modify_time, token=$token, area=$area, status=$status)"
    }


}
