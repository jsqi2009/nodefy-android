package im.vector.app.kelare.network.models

import java.io.Serializable

/**
 * @author jsqi
 */
class BotRoomInfo: Serializable {

    var slack_bot_room_id: RoomInfo? = null
    var skype_bot_room_id: RoomInfo? = null
    var telegram_bot_room_id: RoomInfo? = null
    var whatsapp_bot_room_id: RoomInfo? = null


    inner class RoomInfo : Serializable {
        var key: String? = null
        var value: String? = null
        override fun toString(): String {
            return "RoomInfo(key=$key, value=$value)"
        }
    }

    override fun toString(): String {
        return "BotRoomInfo(slack_bot_room_id=$slack_bot_room_id, skype_bot_room_id=$skype_bot_room_id, telegram_bot_room_id=$telegram_bot_room_id, whatsapp_bot_room_id=$whatsapp_bot_room_id)"
    }
}
