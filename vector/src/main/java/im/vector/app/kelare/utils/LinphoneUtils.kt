package im.vector.app.kelare.utils

import android.annotation.SuppressLint
import im.vector.app.kelare.greendao.DaoSession
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.SipMessage
import org.linphone.core.*

/**
 * author : Jason
 *  date   : 3/22/22 1:41 PM
 *  desc   :
 */
object LinphoneUtils {

    fun getSipAccount(message: ChatMessage, core: Core) : Account? {

        var mRoom:ChatRoom? = null
        var mAccount: Account? = null

        val receivedLocalUser = message.localAddress.username
        val receivedPeerUser = message.fromAddress.username!!.replace("<sip:", "")
        val chatRooms =  core.chatRooms
        if (chatRooms.isNotEmpty()) {
            for (chatRoom in chatRooms) {
                if (chatRoom.localAddress.username == receivedLocalUser && chatRoom.peerAddress.username == receivedPeerUser) {
                    mRoom = chatRoom
                    break
                }
            }
        }

        if (mRoom != null) {
            val accountList = core.accountList
            for (account in accountList) {
                val identityAdd = account.params.identityAddress
                if (mRoom.localAddress.domain == identityAdd!!.domain && receivedLocalUser == identityAdd.username) {
                    mAccount = account
                    break
                }
            }
        } else {
            mAccount = core.defaultAccount
        }

        if (mAccount == null) {
            mAccount = core.defaultAccount
        }

        return mAccount
    }

    @SuppressLint("TimberArgCount")
    fun createBasicChatRoom(message: ChatMessage, localAccount:Account, core: Core) {
        val params = core.createDefaultChatRoomParams()
        params.backend = ChatRoomBackend.Basic
        params.isEncryptionEnabled = false
        params.isGroupEnabled = false
//        params.isEncryptionEnabled = false
//        params.isGroupEnabled = false

        if (params.isValid) {
            // We also need the SIP address of the person we will chat with
            //val remoteSipAccount = message.fromAddress.username
            /**
             * 去掉<sip:
             */
            val remoteSipAccount = message.fromAddress.username!!.replace("<sip:", "")
            //val remoteSipUri = "sip:$remoteSipAccount@${message.fromAddress.domain}"

            val remoteSipUri = "sip:$remoteSipAccount@${localAccount.params.serverAddress!!.domain}"
            val remoteAddress = Factory.instance().createAddress(remoteSipUri)

            if (remoteAddress != null) {
                // And finally we will need our local SIP address
                val localAddress = localAccount.params.identityAddress
                val room = core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
                if (room != null) {
                    val chatMessage = room.createEmptyMessage()
                    chatMessage.send()
                }
            }
        }
    }

    private fun checkChatRoom() {
        //判断消息的local user和peer user  与 chat room的localUserName和peer user是否有匹配的，如果则使用匹配的room 的domain
    }

    fun insertSipMessage(content: String, isSend: Boolean, message: ChatMessage, account: Account,daoSession: DaoSession){

        var info = SipMessage()

//        info.chat_room_id = message.localAddress.username + message.fromAddress.domain + message.fromAddress.username + account.params.serverAddress!!.domain
        info.chat_room_id = message.localAddress.username + account.params.domain + message.fromAddress.username!!.replace("<sip:", "") + account.params.serverAddress!!.domain

        info.received_username = message.localAddress.username
        info.received_domain = message.fromAddress.domain
        info.send_username = message.fromAddress.username
        info.send_domain = message.fromAddress.domain
        info.timestamp = System.currentTimeMillis()
        info.isSend = isSend
        info.message_text = content

        DaoUtils.insertSipMsg(daoSession, info)
    }

}
