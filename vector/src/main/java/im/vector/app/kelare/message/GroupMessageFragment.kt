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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.mylhyl.circledialog.CircleDialog
import com.mylhyl.circledialog.view.listener.OnButtonClickListener
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentGroupMessageBinding
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.kelare.adapter.GroupChatRoomAdapter
import im.vector.app.kelare.adapter.OnItemLongClickListener
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.dialer.widget.BottomActionSheet
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.message.group.GroupChatMessageActivity
import im.vector.app.kelare.message.widget.BottomDeleteDialog
import im.vector.app.kelare.network.models.GroupRoom
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.muc.InvitationListener
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.muc.packet.MUCUser
import org.jxmpp.jid.EntityJid
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GroupMessageFragment : VectorBaseFragment<FragmentGroupMessageBinding>(), RecyclerItemClickListener, OnItemLongClickListener, BottomDeleteDialog.OnItemSelected {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentGroupMessageBinding.inflate(inflater, container, false)

    private var connection: XMPPTCPConnection? = null
    private var mAdapter: GroupChatRoomAdapter? = null
    private var groupRoomList: ArrayList<String> = ArrayList()
    private var roomList: ArrayList<GroupRoom> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        //getJoinedRoom()
        getGroupRoom()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initMultiChat()
    }

    private fun initView() {

        if (mConnectionList.isNotEmpty()) {
            connection = mConnectionList[0]
        }

        views!!.recyclerGroupRoom.layoutManager = LinearLayoutManager(activity)
        mAdapter = GroupChatRoomAdapter(requireActivity(),this, this)
        views!!.recyclerGroupRoom.adapter = mAdapter

    }

    private fun getJoinedRoom() {
        groupRoomList.clear()

        if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
            for (joinedRoom in MultiUserChatManager.getInstanceFor(connection).joinedRooms) {
                Timber.e("joinedRoom: ${joinedRoom}")
                groupRoomList.add(joinedRoom.asBareJid().toString())
            }
        }

        mAdapter!!.clearDataList()
        //mAdapter!!.addDataList(groupRoomList)
        mAdapter!!.notifyDataSetChanged()
    }

    private fun getGroupRoom() {
        roomList.clear()

        val groupRoom = DaoUtils.queryAllGroupRoom(daoSession)
        Timber.e("group room: ${Gson().toJson(groupRoom)}")
        groupRoom.forEach {
            val filterSipList = DaoUtils.queryGroupMsgById(daoSession, it.group_room_id)
            Timber.e("filter group message, ${Gson().toJson(filterSipList)}")
            if (filterSipList.isNotEmpty()) {
                it.latest_message = filterSipList[filterSipList.size - 1].message
            } else {
                it.latest_message = ""
            }

            roomList.add(it)
        }

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(roomList)
        mAdapter!!.notifyDataSetChanged()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onRecyclerViewItemClick(view: View, position: Int) {

        val intent = Intent(activity, GroupChatMessageActivity::class.java)
        intent.putExtra("room_id", roomList[position].group_room_id)
        activity!!.startActivity(intent)
    }

    //long click delete
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onItemLongClick(view: View, position: Int): Boolean {
        //Toast.makeText(activity, "test", Toast.LENGTH_SHORT).show()

        BottomDeleteDialog.showSheet(activity!!, roomList[position].group_room_id, this, null)
        return true
    }

    private fun initMultiChat(){

        if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {

            //监听收到的加群邀请
            MultiUserChatManager.getInstanceFor(connection).addInvitationListener(object : InvitationListener {
                override fun invitationReceived(
                        conn: XMPPConnection?,
                        room: MultiUserChat?,
                        inviter: EntityJid?,
                        reason: String?,
                        password: String?,
                        message: Message?,
                        invitation: MUCUser.Invite?
                ) {
                    Timber.e("invitation: ${room!!.room}")
                    //room.join(Resourcepart.from(connection!!.user.asBareJid().toString()))
                    //需要将房间名字插入本地数据库，再次登录时创建或者加入该房间
                }
            })
        }
    }

    override fun onClick(whichButton: Int, roomID: String) {
        when (whichButton) {
            BottomDeleteDialog.CHOOSE_ROOM -> {

                confirmDeleteDialog(roomID)
            }
            BottomActionSheet.CANCEL       -> { }
            else                           -> {
            }
        }
    }

    private fun confirmDeleteDialog(groupRoomID: String) {

        CircleDialog.Builder()
                .setTitle("Tips")
                .setTitleColor(resources.getColor(R.color.black, null))
                .configTitle() {params ->
                    params.textSize = 14
                }
                .setWidth(0.75f)
                .setText("Are you sure to delete ${groupRoomID.split("@")[0]}?")
                .setTextColor(resources.getColor(R.color.text_color_black, null))
                .configText {params ->
                    params.textSize = 12
                }
                .setPositive("Delete", object : OnButtonClickListener {
                    override fun onClick(v: View?): Boolean {
                        deleteGroupRoom(groupRoomID)
                        return true
                    }
                })
                .configPositive { params ->
                    params!!.textColor = resources.getColor(R.color.red, null)
                    params!!.textSize = 14
                }
                .setNegative("Cancel") { true }
                .configNegative { params ->
                    params!!.textColor = resources.getColor(R.color.colorPrimary, null)
                    params!!.textSize = 14
                }
                .show(fragmentManager)
    }

    private fun deleteGroupRoom(groupRoomID: String) {

        DaoUtils.deleteGroupChatRoom(daoSession, groupRoomID)

        getGroupRoom()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                GroupMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
