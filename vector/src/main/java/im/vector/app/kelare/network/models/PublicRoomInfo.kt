package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class PublicRoomInfo: Serializable {

    var room_id: String? = null
    var initial_room_alias: String? = null

    override fun toString(): String {
        return "PublicRoomInfo(room_id=$room_id, initial_room_alias=$initial_room_alias)"
    }
}
