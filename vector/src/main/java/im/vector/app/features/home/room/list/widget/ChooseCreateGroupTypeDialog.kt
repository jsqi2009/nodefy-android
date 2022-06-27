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

package im.vector.app.features.home.room.list.widget

import SelectedNumberEvent
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import im.vector.app.R
import im.vector.app.features.home.event.ChooseGroupTypeEvent
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession

class ChooseCreateGroupTypeDialog (private val mContext: Activity, private val mBus: AndroidBus) :
        Dialog(mContext, R.style.Dialog_Fullscreen2), View.OnClickListener{

    private var mSession: DialerSession? = null
    private var tvCancel: TextView? = null
    private var tvCreate: TextView? = null
    private var tvJoin: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_create_group_type)
        mSession = DialerSession(mContext)
        mBus.register(this)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {

        tvCancel = findViewById(R.id.tv_cancel)
        tvCreate = findViewById(R.id.tv_create_group)
        tvJoin = findViewById(R.id.tv_join_group)

        tvCancel!!.setOnClickListener(this)
        tvCreate!!.setOnClickListener(this)
        tvJoin!!.setOnClickListener(this)

    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.tv_cancel -> {
                mBus.unregister(this)
                dismiss()
            }
            R.id.tv_create_group -> {
                mBus.post(ChooseGroupTypeEvent(1))
                dismiss()
            }
            R.id.tv_join_group -> {
                mBus.post(ChooseGroupTypeEvent(2))
                mBus.unregister(this)
                dismiss()
            }
            else           -> {
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        mBus.unregister(this)
    }

}
