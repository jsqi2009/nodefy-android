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

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.header.MaterialHeader
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
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.databinding.FragmentSmsMessageBinding
import im.vector.app.kelare.adapter.OnItemLongClickListener
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SipChatRoomAdapter
import im.vector.app.kelare.dialer.widget.BottomActionSheet
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.message.widget.BottomDeleteSMSDialog
import im.vector.app.kelare.network.models.SipChatRoom
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SMSMessageFragment : VectorBaseFragment<FragmentSmsMessageBinding>(), RecyclerItemClickListener, View.OnClickListener,
        OnItemLongClickListener, BottomDeleteSMSDialog.OnItemSelected {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentSmsMessageBinding.inflate(inflater, container, false)

    private var mAdapter: SipChatRoomAdapter? = null
    private var materialHeader  : MaterialHeader? = null
    private var chatRoomList: ArrayList<SipChatRoom> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        getChatRooms()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun getChatRooms(){

        chatRoomList.clear()

        /*for (allColumn in daoSession.sipMessageDao.allColumns) {
            Timber.e("dao column, $allColumn")
        }*/

        val sipList = DaoUtils.queryAllSipMsg(daoSession)

        /*Timber.e("sip message, ${Gson().toJson(sipList)}")

        //按照条件查找
        val filterSipList = DaoUtils.querySipMsgByName(daoSession, "小明")
        Timber.e("filter sip message, ${Gson().toJson(filterSipList)}")*/

        if (sipList.isNotEmpty()) {
            Timber.e("sip message, ${Gson().toJson(sipList)}")
            val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date: Date = Date(sipList[0].timestamp)
            val normalTime = simpleDateFormat.format(date)
            Timber.e("create time： $normalTime")
        }

        val chatRooms =  core.chatRooms
        if (chatRooms.isNotEmpty()) {
            for (chatRoom in chatRooms) {
                val room = SipChatRoom()
                room.localDomain = chatRoom.localAddress.domain
                room.localUserName = chatRoom.localAddress.username
                room.peerDomain = chatRoom.peerAddress.domain
                room.peerUserName = chatRoom.peerAddress.username

                val chatRoomID = room.localUserName + room.localDomain + room.peerUserName + room.peerDomain
                val filterSipList = DaoUtils.querySipMsgByName(daoSession, chatRoomID!!)
                Timber.e("filter sip message, ${Gson().toJson(filterSipList)}")
                if (filterSipList.isNotEmpty()) {
                    room.lastMessage = filterSipList[filterSipList.size - 1].message_text
                }

                chatRoomList.add(room)
            }
            Timber.e("chat room, ${Gson().toJson(chatRoomList)}")
        }

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(chatRoomList)
        mAdapter!!.notifyDataSetChanged()

    }

    private fun initView(){
        views!!.sipSmsRecycler.layoutManager = LinearLayoutManager(activity)
        mAdapter = SipChatRoomAdapter(requireActivity(),this, this)
        views!!.sipSmsRecycler.adapter = mAdapter

        setRefreshHeader()
        views!!.ptrFrameRefresh.headerView = materialHeader
        views!!.ptrFrameRefresh.addPtrUIHandler(materialHeader)
        pullToRefresh()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

        val chatRoom = chatRoomList[position]
        val chatRoomID = chatRoom.localUserName + chatRoom.peerDomain + chatRoom.peerUserName + chatRoom.peerDomain

        val intent = Intent(activity, SendMessageActivity::class.java)
        intent.putExtra("chat_room", chatRoom)
        startActivity(intent)

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onItemLongClick(view: View, position: Int): Boolean {
        val chatRoom = chatRoomList[position]
        BottomDeleteSMSDialog.showSheet(activity!!, chatRoom, this, null)
        return true
    }

    override fun onClick(view: View?) {

    }

    private fun pullToRefresh() {
        views!!.ptrFrameRefresh.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                frame!!.postDelayed({
                    //get data
                    views!!.ptrFrameRefresh.refreshComplete()
                }, 1000)
            }

            override fun checkCanDoRefresh(frame: PtrFrameLayout?, content: View?, header: View?): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }

        })
    }

    private fun setRefreshHeader() {
        materialHeader  = MaterialHeader(activity)
        materialHeader!!.setColorSchemeColors(intArrayOf(requireActivity().resources.getColor(R.color.app_color)))
    }

    override fun onClick(whichButton: Int, info: SipChatRoom) {
        when (whichButton) {
            BottomDeleteSMSDialog.CHOOSE_VALUE -> {
                confirmDeleteDialog(info)
            }
            BottomActionSheet.CANCEL           -> { }
            else                               -> {
            }
        }
    }

    private fun confirmDeleteDialog(roomInfo: SipChatRoom) {

        CircleDialog.Builder()
                .setTitle("Tips")
                .setTitleColor(resources.getColor(R.color.black, null))
                .configTitle() {params ->
                    params.textSize = 14
                }
                .setWidth(0.75f)
                .setText("Are you sure to delete ${roomInfo.peerUserName}?")
                .setTextColor(resources.getColor(R.color.text_color_black, null))
                .configText {params ->
                    params.textSize = 12
                }
                .setPositive("Delete", object : OnButtonClickListener {
                    override fun onClick(v: View?): Boolean {
                        deleteRoom(roomInfo)
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

    private fun deleteRoom(roomInfo: SipChatRoom) {

        val chatRooms =  core.chatRooms
        if (chatRooms.isNotEmpty()) {
            for (chatRoom in chatRooms) {

                if (chatRoom.localAddress.domain == roomInfo.localDomain && chatRoom.localAddress.username == roomInfo.localUserName
                        && chatRoom.peerAddress.domain == roomInfo.peerDomain && chatRoom.peerAddress.username == roomInfo.peerUserName
                ) {
                    core.deleteChatRoom(chatRoom)
                    break
                }
            }
        }

//        val chatRoomID = roomInfo.localUserName + roomInfo.peerDomain + roomInfo.peerUserName + roomInfo.peerDomain
        val chatRoomID = roomInfo.localUserName + roomInfo.localDomain + roomInfo.peerUserName + roomInfo.peerDomain
        Timber.e("chat room ID: $chatRoomID")
        DaoUtils.deleteSipMsgByID(daoSession, chatRoomID)

        getChatRooms()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SMSMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
