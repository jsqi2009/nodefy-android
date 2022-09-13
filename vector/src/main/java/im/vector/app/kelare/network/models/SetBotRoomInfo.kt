package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class SetBotRoomInfo: Serializable {

    var user_id: String? = null
    var data: RoomInfo? = RoomInfo()

    inner class RoomInfo : Serializable {
        var key: String? = null   //bridge bot ID
        var value: String? = null  //Room ID
        override fun toString(): String {
            return "RoomInfo(key=$key, value=$value)"
        }
    }

}
