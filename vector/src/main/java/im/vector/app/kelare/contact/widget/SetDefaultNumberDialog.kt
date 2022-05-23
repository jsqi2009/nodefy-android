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

package im.vector.app.kelare.contact.widget

import SelectedNumberEvent
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SetDefaultNumberAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.PhoneInfo

/**
 * author : Jason
 *  date   : 2022/4/21 15:29
 *  desc   :
 */
class SetDefaultNumberDialog(private val mContext: Activity, private val index: Int, private val mBus: AndroidBus, private val statusList: ArrayList<PhoneInfo>?) :
        Dialog(mContext, R.style.Dialog_Fullscreen2), View.OnClickListener, RecyclerItemClickListener {

    private var mSession: DialerSession? = null
    private var tvCancel: TextView? = null
    private var mAdapter: SetDefaultNumberAdapter? = null
    private var mList: ArrayList<PhoneInfo>? = ArrayList()
    private var mListView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_default_number)
        mSession = DialerSession(mContext)
        mBus.register(this)
        mList = statusList
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {

        tvCancel = findViewById(R.id.tv_cancel)
        mListView = findViewById(R.id.mListView)

        tvCancel!!.setOnClickListener(this)

        mAdapter = SetDefaultNumberAdapter(mContext, this)
        mListView!!.adapter = mAdapter
        mListView!!.layoutManager = LinearLayoutManager(mContext)

        mAdapter!!.addDataList(mList)
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
        mBus.post(SelectedNumberEvent(mList!![position].number.toString(), index))
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        mBus.unregister(this)
    }

}
