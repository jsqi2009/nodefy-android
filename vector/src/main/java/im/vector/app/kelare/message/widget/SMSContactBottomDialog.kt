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

import SelectedDialerContactEvent
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SelectDialerContactAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.network.models.DialerContactInfo
import timber.log.Timber

/**
 * author : Jason
 *  date   : 2022/5/24 14:41
 *  desc   :
 */
class SMSContactBottomDialog(context: Context, private val mBus: AndroidBus, private val contactList: ArrayList<DialerContactInfo>) : BottomSheetDialogFragment(), RecyclerItemClickListener {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: SelectDialerContactAdapter? = null

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) return super.onCreateDialog(savedInstanceState)
        val bottomDialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialog)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_add_sip_contact, null)
        bottomDialog.setContentView(rootView)
        bottomDialog.setCanceledOnTouchOutside(false)
        //核心代码 解决了无法去除遮罩问题
        //bottomDialog.window!!.setDimAmount(0f)
        //设置宽度
        val params = rootView.layoutParams
        params.height = (0.85 * resources.displayMetrics.heightPixels).toInt()
        rootView.layoutParams = params

        initView(rootView)

        return bottomDialog
    }

    private fun initView(rootView: View) {
        //do something
        rootView.findViewById<View>(R.id.title).setOnClickListener { dismiss() }
        rootView.findViewById<View>(R.id.tv_done).setOnClickListener {
            val list = formatData()
            Timber.e("selected contact: $list")
            mBus.post(SelectedDialerContactEvent(list))
            dismiss()
        }

        recyclerView = rootView.findViewById<View>(R.id.rv_contact) as RecyclerView?
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = SelectDialerContactAdapter(context as Activity, this)
        recyclerView!!.adapter = mAdapter

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(contactList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        val item = mAdapter!!.getDataList()!![position]
        for (selectGroupContact in mAdapter!!.getDataList()!!) {
            if (item.id == selectGroupContact.id) {
                selectGroupContact.isSelected = !selectGroupContact.isSelected!!
            }
        }
        mAdapter!!.notifyDataSetChanged()
    }

    private fun formatData():  ArrayList<String>{
        val numberList: ArrayList<String> = ArrayList()
        mAdapter!!.getDataList()!!.forEachIndexed { index, item ->
            if (item.isSelected!!) {

                item.online_phone!!.forEach {
                    if (it.isDefault!!) {
                        numberList.add(it.number!!)
                    }
                }

                item.phone!!.forEach {
                    if (it.isDefault!!) {
                        numberList.add(it.number!!)
                    }
                }
            }
        }
        return numberList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)

        mBus.register(this)
    }

    override fun onStart() {
        super.onStart()

        //拿到系统的 bottom_sheet
        val view: FrameLayout = dialog?.findViewById(R.id.design_bottom_sheet)!!
        //获取behavior
        val behavior = BottomSheetBehavior.from(view)
        //设置弹出高度
        behavior.peekHeight = 3000
        //设置展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}

