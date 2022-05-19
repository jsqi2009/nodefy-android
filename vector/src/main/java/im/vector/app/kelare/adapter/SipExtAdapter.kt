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
import im.vector.app.kelare.network.models.PhoneInfo

/**
 * author : Jason
 *  date   : 2022/4/21
 *  desc   :
 */
class SipExtAdapter(private val mContext: Activity, val mBus: AndroidBus, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<PhoneInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = LayoutInflater.from(mContext)
        val view = context.inflate(R.layout.item_sip_ext_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

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

        holder.tvUserName.text = "Ext" + (position + 1)

        if (holder.etExt.tag != null && holder.etExt.tag is TextWatcher) {
            holder.etExt.removeTextChangedListener(holder.etExt.tag as TextWatcher)
            holder.etExt.clearFocus()
        }
        holder.etExt.setText(info.number.toString())
        val textWatcher = object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                getDataList()!![position].number = s.toString()
                mBus.post(UpdateDefaultNumberEvent())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        holder.etExt.addTextChangedListener(textWatcher)
        holder.etExt.tag = textWatcher
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvUserName: TextView
        var etExt: EditText
        var topLine: View
        var bottomLine: View
        var line: View

        init {
            tvUserName = itemView.findViewById(R.id.tv_username)
            etExt = itemView.findViewById(R.id.et_ext)
            topLine = itemView.findViewById(R.id.top_line)
            bottomLine = itemView.findViewById(R.id.bottom_line)
            line = itemView.findViewById(R.id.line)
        }

    }
}
