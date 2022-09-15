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

import CreateGroupNameEvent
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import im.vector.app.R
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.DialerAccountInfo
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * author : Jason
 *  date   : 2022/3/30 15:29
 *  desc   :
 */
class CreateXMPPGroupDialog(private val mContext: Activity, private val mBus: AndroidBus, private val account: DialerAccountInfo) :
        Dialog(mContext, R.style.Dialog_Fullscreen), View.OnClickListener{

    private var mSession: DialerSession? = null
    private var etGroupName: EditText? = null
    private var tvCancel: TextView? = null
    private var tvCreate: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_create_xmpp_group)
        mSession = DialerSession(mContext)
        mBus.register(this)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {

        tvCancel = findViewById(R.id.tv_cancel)
        tvCreate = findViewById(R.id.tv_create)
        etGroupName = findViewById(R.id.et_name)

        tvCancel!!.setOnClickListener(this)
        tvCreate!!.setOnClickListener(this)

        setEditTextInputSpeChat()
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.tv_cancel -> {
                mBus.unregister(this)
                dismiss()
            }
            R.id.tv_create -> {
                createGroup()
            }
            else -> {
            }
        }
    }

    private fun createGroup(){
        if (TextUtils.isEmpty(etGroupName!!.text.toString())) {
            return
        }
        mBus.post(CreateGroupNameEvent(account, etGroupName!!.text.toString()))
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        mBus.unregister(this)
    }

    private fun setEditTextInputSpeChat() {
        val filter: InputFilter = object : InputFilter {
            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                val speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
                val pattern: Pattern = Pattern.compile(speChat)
                val matcher: Matcher = pattern.matcher(source.toString())
                return if (matcher.find()) {
                    ""
                } else {
                    null
                }
            }
        }
        val emojiFilter: InputFilter = object : InputFilter {
            var emoji: Pattern = Pattern.compile(
                    "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE or Pattern.CASE_INSENSITIVE
            )
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                val emojiMatcher: Matcher = emoji.matcher(source)
                return if (emojiMatcher.find()) {
                    ""
                } else null
            }
        }
        etGroupName!!.filters = arrayOf<InputFilter>(filter, emojiFilter)
    }




}
