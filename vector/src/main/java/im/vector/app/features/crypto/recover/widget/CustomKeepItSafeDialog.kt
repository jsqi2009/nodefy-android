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

package im.vector.app.features.crypto.recover.widget

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import im.vector.app.R
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.DialerAccountInfo

/**
 * author : Jason
 *  date   : 2022/6/7 11:29
 *  desc   :
 */
class CustomKeepItSafeDialog(private val mContext: Activity) : Dialog(mContext, R.style.Dialog_Fullscreen), View.OnClickListener {

    private var tvContinue: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_custom_keep_it_safe)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {

        tvContinue = findViewById(R.id.tv_continue)

        tvContinue!!.setOnClickListener(this)
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.tv_continue -> {
                dismiss()
            }
            else -> {
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}
