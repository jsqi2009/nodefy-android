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

package im.vector.app.kelare.message.widget

import SelectedXMPPAccountEvent
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.XMPPAccountAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.DialerAccountInfo

/**
 * author : Jason
 *  date   : 2022/3/30 15:29
 *  desc   :
 */
class ChooseXMPPAccountDialog(private val mContext: Activity, private val mBus: AndroidBus, private val accountList: ArrayList<DialerAccountInfo>?) :
        Dialog(mContext, R.style.Dialog_Fullscreen2), View.OnClickListener, RecyclerItemClickListener {

    private var mSession: DialerSession? = null
    private var tvCancel: TextView? = null
    private var mAdapter: XMPPAccountAdapter? = null
    private var xmppList: ArrayList<DialerAccountInfo>? = ArrayList()
    private var mListView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_xmpp_account)
        mSession = DialerSession(mContext)
        mBus.register(this)
        xmppList = accountList
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {

        tvCancel = findViewById(R.id.tv_cancel)
        mListView = findViewById(R.id.mListView)

        tvCancel!!.setOnClickListener(this)

        mAdapter = XMPPAccountAdapter(mContext, this)
        mListView!!.adapter = mAdapter
        mListView!!.layoutManager = LinearLayoutManager(mContext)

        mAdapter!!.addDataList(xmppList)
        mAdapter!!.notifyDataSetChanged()

    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.tv_cancel -> {
                mBus.unregister(this)
                dismiss()
            }
            else -> {
            }
        }
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        mBus.post(SelectedXMPPAccountEvent(xmppList!![position]))
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        mBus.unregister(this)
    }

}
