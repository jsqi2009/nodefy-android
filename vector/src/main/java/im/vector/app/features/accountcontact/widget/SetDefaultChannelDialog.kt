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

package im.vector.app.features.accountcontact.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import im.vector.app.R
import im.vector.app.kelare.adapter.ContactChannelAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.ContactChannelInfo

/**
 * author : Jason
 *  desc   :
 */
class SetDefaultChannelDialog(val mContext: Context, val mBus: AndroidBus, private val contactChannelList: ArrayList<ContactChannelInfo>,
                              val mListener: ChannelListener) :
        Dialog(mContext, R.style.Dialog_Fullscreen), View.OnClickListener, OnItemClickListener {

    private var mSession: DialerSession? = null
    private var tvCancel: TextView? = null
    private var tvSave: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private lateinit var mAdapter: ContactChannelAdapter
    private var channelListener: ChannelListener? = null


    interface ChannelListener {
        fun onDefaultChannel(item: ContactChannelInfo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_default_channel)
        mSession = DialerSession(mContext)
        mBus.register(this)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {
        channelListener = mListener

        tvCancel = findViewById(R.id.tv_cancel)
        tvSave = findViewById(R.id.tv_save)
        mRecyclerView = findViewById(R.id.channelList)

        mAdapter = ContactChannelAdapter(mContext, contactChannelList)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.adapter = mAdapter
        mAdapter.setOnItemClickListener(this)

        tvCancel!!.setOnClickListener(this)
        tvSave!!.setOnClickListener(this)

    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val  item = mAdapter.getItem(position)
        contactChannelList.forEach {
            it.isDefault = item.contacts_id == it.contacts_id
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.tv_cancel -> {
                mBus.unregister(this)
                dismiss()
            }
            R.id.tv_save -> {
                saveChannel()
            }
            else -> {
            }
        }
    }

    private fun saveChannel(){
        contactChannelList.forEach {
            if (it.isDefault && it.displayType!!.lowercase() != mContext.getString(R.string.account_contact_channel_nodefy)) {
                channelListener!!.onDefaultChannel(it)
            }
        }

        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        mBus.unregister(this)
    }


}
