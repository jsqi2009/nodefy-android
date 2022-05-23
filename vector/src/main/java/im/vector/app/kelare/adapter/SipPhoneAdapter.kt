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

package im.vector.app.kelare.adapter

import UpdateDefaultNumberEvent
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.models.PhoneInfo

/**
 * author : Jason
 *  date   : 2022/4/21 13:49
 *  desc   :
 */
class SipPhoneAdapter(private val mContext: Activity, val mSession: DialerSession, val mBus: AndroidBus, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<PhoneInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = LayoutInflater.from(mContext)
        val view = context.inflate(R.layout.item_sip_phone_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        //holder.tvUserName.text = info.name

        if (getDataList()!!.size == 1) {
            holder.topLine.visibility = View.VISIBLE
            holder.bottomLine.visibility = View.VISIBLE
            holder.line.visibility = View.GONE
        } else if (getDataList()!!.size > 1) {
            if (position == 0) {
                holder.topLine.visibility = View.VISIBLE
                holder.bottomLine.visibility = View.GONE
                holder.line.visibility = View.VISIBLE
            } else if (position == getDataList()!!.size - 1) {
                holder.topLine.visibility = View.GONE
                holder.bottomLine.visibility = View.VISIBLE
                holder.line.visibility = View.GONE
            } else {
                holder.topLine.visibility = View.GONE
                holder.bottomLine.visibility = View.GONE
                holder.line.visibility = View.VISIBLE
            }
        }

        holder.tvUserName.text = "Phone" + (position + 1)

        if (holder.etPhone.tag != null && holder.etPhone.tag is TextWatcher) {
            holder.etPhone.removeTextChangedListener(holder.etPhone.tag as TextWatcher)
            holder.etPhone.clearFocus()
        }
        holder.etPhone.setText(info.number.toString())
        val textWatcher = object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                getDataList()!![position].number = s.toString()
                mBus.post(UpdateDefaultNumberEvent())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.etPhone.addTextChangedListener(textWatcher)
        holder.etPhone.tag = textWatcher

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvUserName: TextView
        var etPhone: EditText
        var topLine: View
        var bottomLine: View
        var line: View

        init {
            tvUserName = itemView.findViewById(R.id.tv_username)
            etPhone = itemView.findViewById(R.id.et_phone)
            topLine = itemView.findViewById(R.id.top_line)
            bottomLine = itemView.findViewById(R.id.bottom_line)
            line = itemView.findViewById(R.id.line)
        }

    }
}
