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

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.network.models.PeopleMessage
import java.text.SimpleDateFormat
import java.util.*

/**
 * author : Jason
 *  date   : 2022/3/25 13:49
 *  desc   :
 */
class PeopleMessageAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<PeopleMessage>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_people_message_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = Date(info.timestamp)
        val normalTime = sdf.format(date)

        holder.tvTime.text = normalTime
        if (info.isSend) {
            holder.llSend.visibility = View.VISIBLE
            holder.llReceived.visibility = View.GONE
            holder.tvSend.text = info.message
        } else {
            holder.llReceived.visibility = View.VISIBLE
            holder.llSend.visibility = View.GONE
            holder.tvReceived.text = info.message
        }

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTime: TextView
        var tvReceived: TextView
        var tvSend: TextView
        var lineView: View
        var root: LinearLayout
        var llReceived: LinearLayout
        var llSend: LinearLayout

        init {
            tvTime = itemView.findViewById(R.id.tv_time)
            tvReceived = itemView.findViewById(R.id.tv_received_msg)
            tvSend = itemView.findViewById(R.id.tv_send_msg)
            lineView = itemView.findViewById(R.id.line)
            root = itemView.findViewById(R.id.root)
            llReceived = itemView.findViewById(R.id.ll_received)
            llSend = itemView.findViewById(R.id.ll_send)
        }

    }
}
