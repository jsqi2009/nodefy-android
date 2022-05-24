/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.kelare.message

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityPeopleChatMessageBinding
import im.vector.app.kelare.adapter.PeopleMessageAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.PeopleMessage
import im.vector.app.kelare.network.models.PeopleRoom
import im.vector.app.kelare.network.models.XmppContact
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jxmpp.jid.EntityBareJid
import timber.log.Timber

@AndroidEntryPoint
class PeopleChatMessageActivity : VectorBaseActivity<ActivityPeopleChatMessageBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityPeopleChatMessageBinding.inflate(layoutInflater)

    private var xmppContact: XmppContact? = null
    private var manager: ChatManager? = null
    private var connection: XMPPTCPConnection? = null
    private var mChat: Chat? = null
    private var index: Int? = null   //1:from xmpp contact   2:from message people
    private var mAdapter: PeopleMessageAdapter? = null
    private var messageList : List<PeopleMessage> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        index = intent.extras!!.getInt("index")
        xmppContact = intent.extras!!.getSerializable("contact") as XmppContact?

        initView()
    }

    override fun onResume() {
        super.onResume()

        getHistoryMessage()
    }

    private fun initView(){

        if (index == 1) {
            verifyChartRoom()
        }

        views.tvTitle.text = xmppContact!!.jid!!.split("@")[0]

        views.rlBack.setOnClickListener(this)
        views.tvSend.setOnClickListener(this)

        views.messageRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = PeopleMessageAdapter(this,this)
        views.messageRecycler.adapter = mAdapter

        initChat()
    }

    private fun initChat(){
        val loginUserJid = xmppContact!!.login_user_jid
        if (mConnectionList.isEmpty()) {
            return
        }

        for (xmpptcpConnection in mConnectionList) {
            if (loginUserJid == xmpptcpConnection.user.asBareJid().toString()) {
                connection = xmpptcpConnection
                break
            }
        }
        manager = ChatManager.getInstanceFor(connection)
        manager!!.addIncomingListener(incomingChatMessageListener)
        manager!!.addOutgoingListener(outgoingChatMessageListener)
        mChat = manager!!.chatWith(xmppContact!!.jid as EntityBareJid?)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_send -> {
                sendMsg()
            }
            else -> {

            }
        }
    }

    private fun getHistoryMessage(){
        //find by roomID
        val roomID = xmppContact!!.login_user_jid + xmppContact!!.jid!!.asBareJid().toString()
        val filterSipList = DaoUtils.queryPeopleMsgById(daoSession, roomID)
        Timber.e("filter sip message, ${Gson().toJson(filterSipList)}")

        messageList = filterSipList

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(messageList)
        mAdapter!!.notifyDataSetChanged()
        //auto scroll to bottom
        views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    private fun sendMsg(){
        val content = views.etMessage.text.toString()
        if (TextUtils.isEmpty(content)) {
            return
        }

        mChat!!.send(content)
        insertChatMessage(content, true)
        views.etMessage.setText("")
    }

    /**
     * verify room if exist
     */
    private fun verifyChartRoom(){

        val roomList = DaoUtils.queryAllPeopleRoom(daoSession)
        if (roomList.isNotEmpty()) {
            var flag = false
            for (peopleRoom in roomList) {
                if (peopleRoom.people_room_id == (xmppContact!!.login_user_jid + xmppContact!!.jid!!.asBareJid().toString())) {
                    flag = true
                    break
                }
            }
            if (!flag) {
                insertChatRoom()
            }
        } else {
            insertChatRoom()
        }
    }

    private fun insertChatRoom(){

        val peopleRoom = PeopleRoom()

        peopleRoom.people_room_id = xmppContact!!.login_user_jid + xmppContact!!.jid!!.asBareJid().toString()
        peopleRoom.login_account_jid = xmppContact!!.login_user_jid
        peopleRoom.login_name = xmppContact!!.login_user
        peopleRoom.login_account = xmppContact!!.login_account
        peopleRoom.chat_with_jid = xmppContact!!.jid!!.asBareJid().toString()
        peopleRoom.latest_message = ""

        DaoUtils.insertPeopleChatRoom(daoSession, peopleRoom)
    }

    private val incomingChatMessageListener = object : IncomingChatMessageListener {
        override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
            Timber.e("chat incoming message: ${message!!.body}")
            runOnUiThread {
                //insertChatMessage(message.body, false)

                getHistoryMessage()
            }
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

    private fun insertChatMessage(msg:String, isSend:Boolean){
        val message = PeopleMessage()

        message.people_room_id = xmppContact!!.login_user_jid + xmppContact!!.jid!!.asBareJid().toString()
        message.login_account_jid = xmppContact!!.login_user_jid
        message.login_name = xmppContact!!.login_user
        message.chat_with_jid = xmppContact!!.jid!!.asBareJid().toString()
        message.message = msg
        message.isSend = isSend
        message.timestamp = System.currentTimeMillis()

        DaoUtils.insertPeopleChatMessage(daoSession, message)

        mAdapter!!.addDataList(message)
        mAdapter!!.notifyDataSetChanged()
        views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    override fun onPause() {
        super.onPause()

        manager!!.removeIncomingListener(incomingChatMessageListener)
        manager!!.removeOutgoingListener(outgoingChatMessageListener)
        connection = null
    }

    override fun onStop() {
        super.onStop()

        manager!!.removeIncomingListener(incomingChatMessageListener)
        manager!!.removeOutgoingListener(outgoingChatMessageListener)
        connection = null
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

    }
}
