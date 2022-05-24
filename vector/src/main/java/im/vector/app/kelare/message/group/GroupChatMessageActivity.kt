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

package im.vector.app.kelare.message.group

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityGroupChatMessageBinding
import im.vector.app.databinding.ActivityGroupCreateBinding
import im.vector.app.databinding.ActivitySendMessageBinding
import im.vector.app.kelare.adapter.GroupMessageAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.GroupMessage
import im.vector.app.kelare.network.models.GroupRoom
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.impl.JidCreate
import timber.log.Timber

@AndroidEntryPoint
class GroupChatMessageActivity : VectorBaseActivity<ActivityGroupChatMessageBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityGroupChatMessageBinding.inflate(layoutInflater)

    private var connection: XMPPTCPConnection? = null
    private var roomName = ""
    private var roomID = ""
    private var mAdapter: GroupMessageAdapter? = null
    private var messageList : List<GroupMessage> = ArrayList()
    private var roomInfo: GroupRoom? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }
    private fun initView() {

        roomID = intent.extras!!.getString("room_id").toString()
        roomInfo = getGroupRoom()
        views.tvRoom.text = roomInfo!!.group_room_name

        views.rlBack.setOnClickListener(this)
        views.ivParticipantUser.setOnClickListener(this)
        views.ivAddUser.setOnClickListener(this)
        views.tvSend.setOnClickListener(this)

        views.messageRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = GroupMessageAdapter(this,this)
        views.messageRecycler.adapter = mAdapter

        initXmpp()
    }

    override fun onResume() {
        super.onResume()

        getMessage()
    }

    private fun getGroupRoom(): GroupRoom? {

        val groupRoom = DaoUtils.queryAllGroupRoom(daoSession)
        Timber.e("group room: ${Gson().toJson(groupRoom)}")
        for (groupRoom in groupRoom) {
            if (groupRoom.group_room_id == roomID) {
                return groupRoom
            }
        }
        return null
    }

    private fun initXmpp() {

        if (mConnectionList.isEmpty()) {
            return
        }
        for (xmpptcpConnection in mConnectionList) {
            if (xmpptcpConnection.user.asBareJid().toString() == roomInfo!!.room_owner) {
                connection = xmpptcpConnection
                break
            }
        }
        if (connection == null) {
            for (xmpptcpConnection in mConnectionList) {
                if (roomInfo!!.participants.contains(xmpptcpConnection.user.asBareJid().toString())) {
                    connection = xmpptcpConnection
                    break
                }
            }
        }

        val manager = MultiUserChatManager.getInstanceFor(connection)
        val muc2: MultiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(roomID))
        muc2.addMessageListener {

            Timber.e("received group message: ${it.bodies}")
            Timber.e("received group message: ${it.body}")
            Timber.e("received group message: ${it.from}")
            Timber.e("received group message: ${it.to.asBareJid()}")
            Timber.e("received group message: ${it.from.asBareJid()}")
            Timber.e("received group message: ${it.from.split("/")[1]}")

            runOnUiThread {
                getMessage()
            }
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_send -> {
                sendMessage()
            }
            R.id.iv_participant_user -> {
                val intent = Intent(this, GroupParticipantActivity::class.java)
                intent.putExtra("group_name", roomInfo!!.group_room_name)
                intent.putExtra("participant", roomInfo!!.participants)
                startActivity(intent)
            }
            R.id.iv_add_user -> {
                val intent = Intent(this, InviteContactActivity::class.java)
                intent.putExtra("group_name", roomInfo!!.group_room_name)
                intent.putExtra("participant", roomInfo!!.participants)
                intent.putExtra("owner", roomInfo!!.room_owner)
                intent.putExtra("room_id", roomInfo!!.group_room_id)
                startActivity(intent)
            }
            else -> {
            }
        }
    }

    private fun getMessage() {
        //find by roomID
        val filterSipList = DaoUtils.queryGroupMsgById(daoSession, roomID)
        Timber.e("filter group message, ${Gson().toJson(filterSipList)}")

        messageList = filterSipList

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(messageList)
        mAdapter!!.notifyDataSetChanged()
        //auto scroll to bottom
        views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    private fun sendMessage() {
        if (TextUtils.isEmpty(views.etMessage.text.toString())) {
            return
        }
        val manager = MultiUserChatManager.getInstanceFor(connection)
        val muc2: MultiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(roomID))
        muc2.sendMessage(views.etMessage.text.toString())

        insertMessage(views.etMessage.text.toString())

        views.etMessage.setText("")
    }

    private fun insertMessage(message: String) {

        val groupMsg = GroupMessage()
        groupMsg.group_room_id = roomID
        groupMsg.group_room_name = roomName
        groupMsg.isRead = true
        groupMsg.isSend = true
        groupMsg.message = message
        groupMsg.message_from = connection!!.user.asBareJid().toString()
        groupMsg.timestamp = System.currentTimeMillis()

        DaoUtils.insertGroupChatMessage(daoSession, groupMsg)

        mAdapter!!.addDataList(groupMsg)
        mAdapter!!.notifyDataSetChanged()
        views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

    }
}
