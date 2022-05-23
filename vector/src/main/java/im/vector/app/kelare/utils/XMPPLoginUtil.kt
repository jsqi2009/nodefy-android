package im.vector.app.kelare.utils

import android.content.Context
import android.text.TextUtils
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.greendao.DaoSession
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.GroupMessage
import im.vector.app.kelare.network.models.GroupRoom
import im.vector.app.kelare.network.models.PeopleMessage
import im.vector.app.kelare.network.models.PeopleRoom
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.ReconnectionManager
import org.jivesoftware.smack.StanzaListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.filter.StanzaFilter
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.muc.InvitationListener
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.muc.packet.MUCUser
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.EntityJid
import org.jxmpp.jid.parts.Resourcepart
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * author : Jason
 * date   : 4/8/22 1:41 PM
 * desc   :
 */
class XMPPLoginUtil(val context: Context, private val mDaoSession: DaoSession, val mConnectionList: ArrayList<XMPPTCPConnection>) {

    private var xmppConnection: XMPPTCPConnection? = null

    fun initXMPPTCPConnection(domain: String, proxyAdd: String?, userName: String, pwd: String) {
        xmppConnection = XmppHelper().initXmppConfig(domain, proxyAdd)
        xmppConnection!!.addConnectionListener(connectListener)
        //roster.addRosterListener(this)

        val mReconnectionManager = ReconnectionManager.getInstanceFor(xmppConnection)
        mReconnectionManager.enableAutomaticReconnection()

        xmppConnection!!.setParsingExceptionCallback {
            //Timber.e("Exception---${it.content}")
            Timber.e("Exception message---${it.parsingException.message}")
        }

        loginExecutor(domain, proxyAdd, userName, pwd)

    }

    private fun loginExecutor(domain: String, proxyAdd: String?, userName: String, pwd: String) {
        var executor: ScheduledThreadPoolExecutor? = null

        if (executor == null) {
            executor = ScheduledThreadPoolExecutor(
                3,
                BasicThreadFactory.Builder().namingPattern("XMPP Login Thread").daemon(true)
                    .build()
            )
            executor.schedule({
                loginOpenFire(domain, proxyAdd, userName, pwd)
            }, 0, TimeUnit.MILLISECONDS)
        }
    }

    private fun loginOpenFire(domain: String, proxyAdd: String?, userName: String, pwd: String){
        if (xmppConnection == null) {
            return
        }
        try {
            //如果没有连接openfire服务器，则连接；若已连接openfire服务器则跳过。
            if (xmppConnection!!.isConnected) {
                xmppConnection!!.login(userName, pwd)
            } else {
                connectOpenFire(domain, proxyAdd, userName, pwd)
                xmppConnection!!.login(userName, pwd)
            }
        } catch (e: Exception) {
            Timber.e("login Exception: ${e.message}")
        }
    }

    private fun connectOpenFire(domain: String, proxyAdd: String?, userName: String, pwd: String){
        try {
            if (xmppConnection != null) {
                if (!xmppConnection!!.isConnected) {
                    xmppConnection!!.connect()
                }
            } else {
                xmppConnection = XmppHelper().initXmppConfig(domain, proxyAdd)
                xmppConnection!!.connect()
            }
        } catch (e: Exception) {
            Timber.e("connect Exception: ${e.message}")
        }
    }

    private var connectListener: ConnectionListener = object : ConnectionListener {

        override fun connected(connection: XMPPConnection?) {

        }

        override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
            Timber.tag("authenticated").e("authenticated");
            if (connection!!.isAuthenticated) {
                Timber.e("xmpp authenticated")
                Timber.e("current authenticated user: ${connection!!.user.asBareJid().toString()}")
                if (mConnectionList.isNotEmpty()) {
                    var flag = true
                    for (item in mConnectionList) {
                        if (item.user.asBareJid().toString() == connection!!.user.asBareJid().toString()) {
                            flag = false
                            break
                        }
                    }
                    if (flag) {
                        mConnectionList.add(connection!! as XMPPTCPConnection)
                    }
                } else {
                    mConnectionList.add(connection!! as XMPPTCPConnection)
                }

                if (mConnectionList.isNotEmpty()) {
                    for (item in mConnectionList) {
                        if (item.user.asBareJid().toString() == xmppConnection!!.user.asBareJid().toString()) {
                            //peopleCharInit(item)
                            initMultiChat(item)
                        }
                    }
                }
            }
            if (connection!!.isAuthenticated) {
                Timber.e("xmpp isAuthenticated")
            } else {
                Timber.e("xmpp not Authenticated")
            }
        }
    }

    private fun peopleChatInit(mConn: XMPPTCPConnection){
        var manager: ChatManager? = null
        manager = ChatManager.getInstanceFor(mConn)
        manager!!.addIncomingListener(incomingChatMessageListener)
        manager!!.addOutgoingListener(outgoingChatMessageListener)
    }

    private val incomingChatMessageListener = object : IncomingChatMessageListener {
        override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
            Timber.e("incoming message: ${message!!.body}")

            Timber.e("incoming from: ${message!!.from.asBareJid()}")
            Timber.e("incoming to: ${message!!.to.asBareJid()}")
            insertChatMessage(message!!, false)
        }
    }

    private val outgoingChatMessageListener = object : OutgoingChatMessageListener {
        override fun newOutgoingMessage(
                to: EntityBareJid?,
                messageBuilder: MessageBuilder?,
                chat: Chat?
        ) {
            Timber.e("outgoing message: ${messageBuilder!!.body}")
        }
    }

    private fun insertChatMessage(receivedMsg: Message, isSend:Boolean){
        val message = PeopleMessage()

        message.people_room_id = receivedMsg!!.to.asBareJid().toString() + receivedMsg!!.from.asBareJid().toString()
        message.login_account_jid = receivedMsg!!.to.asBareJid().toString()
        message.login_name = receivedMsg!!.to.asBareJid().toString().split("@")[0]
        message.chat_with_jid = receivedMsg!!.from.asBareJid().toString()
        message.message = receivedMsg!!.body
        message.isSend = isSend
        message.timestamp = System.currentTimeMillis()

        Timber.e("insert message: ${receivedMsg!!.body}")
        DaoUtils.insertPeopleChatMessage(mDaoSession, message)
    }

    fun initMultiChat(mConn: XMPPTCPConnection){
        val multiManager = MultiUserChatManager.getInstanceFor(mConn)
        //监听收到的加群邀请
        multiManager.addInvitationListener(object : InvitationListener {
            override fun invitationReceived(
                conn: XMPPConnection?,
                room: MultiUserChat?,
                inviter: EntityJid?,
                reason: String?,
                password: String?,
                message: Message?,
                invitation: MUCUser.Invite?
            ) {
                Timber.e("room: ${room!!.room}")
                Timber.e("reason: ${reason}")
                Timber.e("message: ${message!!.body}")
                Timber.e("invitation: ${invitation!!.from}")
                Timber.e("inviter: ${inviter!!.asBareJid().toString()}")
                room.join(Resourcepart.from(mConn.user.asBareJid().toString()))
                //room.createOrJoin(Resourcepart.from(mConn.user.asBareJid().toString()))

               /* val muc: MultiUserChat? = MultiUserChatManager.getInstanceFor(mConn).getMultiUserChat(JidCreate.entityBareFrom(room.room))
                muc!!.join(Resourcepart.from(mConn.user.asBareJid().toString()))*/

                //需要将房间名字插入本地数据库，再次登录时创建或者加入该房间
                createGroupRoom(conn, room, inviter, reason, invitation)
            }
        })

        mConn.addSyncStanzaListener(object : StanzaListener{
            override fun processStanza(packet: Stanza?) {
                if (packet is Message) {
                    Timber.e("message body: ${packet.body}")
                    Timber.e("message type: ${packet.type}")

                    when (packet.type.name) {
                        "groupchat" -> {
                            Timber.e("groupchat: ${packet.toXML()}")
                            if (!TextUtils.isEmpty(packet.body)) {
                                handelGroupChat(packet)
                            }
                        }
                        "chat" -> {
                            Timber.e("chat: ${packet.toXML()}")
                            if (!TextUtils.isEmpty(packet.body)) {
                                handlePeopleChat(packet)
                            }

                        }
                        "normal" -> {
                            Timber.e("normal: ${packet.toXML()}")
                        }
                        else -> {
                            Timber.e("other: ${packet.toXML()}")
                        }
                    }

                }else if (packet is Presence) {

                }
            }

        }, object :StanzaFilter{
            override fun accept(stanza: Stanza?): Boolean {
                return true
            }

        })
    }

    private fun createGroupRoom(conn: XMPPConnection?,room: MultiUserChat?, inviter: EntityJid?,reason: String?, invitation: MUCUser.Invite?) {
        val groupRoom = GroupRoom()

        val array = reason!!.split(",") as ArrayList
        array.removeAt(0)
        array.add(invitation!!.from.split("/")[1])
        val users = StringUtils.join(array, ",")

        groupRoom.group_room_id = room!!.room.toString()
        groupRoom.group_room_name = room!!.room.toString().split("@")[0]
        groupRoom.latest_message = ""
        groupRoom.message_from = ""
        groupRoom.participants = users
        groupRoom.room_owner = conn!!.user.asBareJid().toString()

        DaoUtils.insertGroupChatRoom(mDaoSession, groupRoom)
    }

    private fun handlePeopleChat(receivedMsg: Message){

        val room = receivedMsg.from.asBareJid()
        val to = receivedMsg.to.asBareJid()
        val from = receivedMsg.from.split("/")[0]
        val msg = receivedMsg.body

        //insert chat room
        verifyChartRoom(receivedMsg)
        //insert chat message
        insertChatMessage(receivedMsg)
    }

    /**
     * verify room if exist
     */
    private fun verifyChartRoom(receivedMsg: Message){

        val to = receivedMsg.to.asBareJid().toString()
        val from = receivedMsg.from.split("/")[0]

        val roomList = DaoUtils.queryAllPeopleRoom(mDaoSession)
        if (roomList.isNotEmpty()) {
            var flag = false
            for (peopleRoom in roomList) {
                if (peopleRoom.people_room_id == (to + from)) {
                    flag = true
                    break
                }
            }
            if (!flag) {
                insertChatRoom(receivedMsg)
            }
        } else {
            insertChatRoom(receivedMsg)
        }
    }

    private fun insertChatRoom(receivedMsg: Message){
        val to = receivedMsg.to.asBareJid().toString()
        val from = receivedMsg.from.split("/")[0]
        val msg = receivedMsg.body

        val peopleRoom = PeopleRoom()

        peopleRoom.people_room_id = to + from
        peopleRoom.login_account_jid = to
        peopleRoom.login_name = to.split("@")[0]
        peopleRoom.chat_with_jid = from
        peopleRoom.latest_message = msg
        peopleRoom.login_account = getAccountName(to)

        DaoUtils.insertPeopleChatRoom(mDaoSession, peopleRoom)
    }

    private fun insertChatMessage(receivedMsg: Message){

        val to = receivedMsg.to.asBareJid().toString()
        val from = receivedMsg.from.split("/")[0]
        val msg = receivedMsg.body

        val message = PeopleMessage()

        message.people_room_id = to + from
        message.login_account_jid = to
        message.login_name = to.split("@")[0]
        message.chat_with_jid = from
        message.message = msg
        message.isSend = false
        message.timestamp = System.currentTimeMillis()

        DaoUtils.insertPeopleChatMessage(mDaoSession, message)
    }

    private fun handelGroupChat(receivedMsg: Message) {
        val to = receivedMsg.to.asBareJid().toString().split("/")[0]
        val from = receivedMsg.from.split("/")[1]
        val group_room_id = receivedMsg.from.split("/")[0]
        val group_room = group_room_id.split("@")[0]
        val msg = receivedMsg.body

        val groupMsg = GroupMessage()
        groupMsg.group_room_id = group_room_id
        groupMsg.group_room_name = group_room
        groupMsg.isRead = false
        groupMsg.isSend = false
        groupMsg.message = msg
        groupMsg.message_from = from
        groupMsg.timestamp = System.currentTimeMillis()

        if (to != from) {
            if (from.lowercase(Locale.ROOT) != group_room.lowercase(Locale.ROOT)) {
                DaoUtils.insertGroupChatMessage(mDaoSession, groupMsg)
            }
        }
    }

    fun disconnectExecutor(accountInfo : DialerAccountInfo) {
        var position = -1
        if (mConnectionList.isNotEmpty()) {
            for (index in mConnectionList.indices) {
                if (accountInfo.username + "@" + accountInfo.domain == mConnectionList[index].user.asBareJid().toString()) {
                    position = index
                    break
                }
            }
        }

        val mExecutor = ScheduledThreadPoolExecutor(2,
            BasicThreadFactory.Builder().namingPattern("xmpp disconnect").daemon(true)
                .build()
        )
        mExecutor!!.schedule({
            mConnectionList[position].disconnect()

            //update mConnectionList
            mConnectionList.removeAt(position)
        }, 0, TimeUnit.MILLISECONDS)

        //updateServerAccount(false)
    }

    private fun getAccountName(jid: String): String? {
        var accountName = ""
        val mSession = DialerSession(context)
        val accountList = mSession.accountListInfo
        accountList!!.forEach {
            if (it.username + "@" + it.domain == jid) {
                accountName = it.account_name!!
                return accountName
            }
        }
        return accountName
    }
}
