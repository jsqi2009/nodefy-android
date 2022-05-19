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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import org.linphone.core.CallLog
import java.text.SimpleDateFormat
import java.util.*

/**
 * author : Jason
 *  date   : 2022/5/6
 *  desc   :
 */
class CallHistoryAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<CallLog>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_call_history_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date: Date = Date(info.startDate * 1000)
        val normalTime = sdf.format(date)

        holder.tvNumber.text = info.fromAddress.username
        holder.tvTime.text = normalTime

        if (info.status.toInt() == 2) {
            holder.ivCallIn.visibility = View.GONE
            holder.ivCallOut.visibility = View.GONE
            holder.ivCallMisssed.visibility = View.VISIBLE

            holder.tvDir.text = "Missed"
            holder.tvDir.setTextColor(mContext.resources.getColor(R.color.red, null))
        } else {
            if (info.dir.toInt() == 0) {   //0:outgoing   1:incoming
                holder.ivCallIn.visibility = View.GONE
                holder.ivCallOut.visibility = View.VISIBLE
                holder.ivCallMisssed.visibility = View.GONE

                holder.tvDir.text = "Outbound Call"
            }else if (info.dir.toInt() == 1) {
                holder.ivCallIn.visibility = View.VISIBLE
                holder.ivCallOut.visibility = View.GONE
                holder.ivCallMisssed.visibility = View.GONE

                holder.tvDir.text = "Inbound Call"
            }
            holder.tvDir.setTextColor(mContext.resources.getColor(R.color.text_color_black1, null))
        }

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvNumber: TextView
        var tvTime: TextView
        var tvDir: TextView
        var ivCallIn: ImageView
        var ivCallOut: ImageView
        var ivCallMisssed: ImageView
        var lineView: View

        init {
            tvNumber = itemView.findViewById(R.id.tv_number)
            tvTime = itemView.findViewById(R.id.tv_time)
            tvDir = itemView.findViewById(R.id.tv_dir)
            ivCallIn = itemView.findViewById(R.id.iv_call_in)
            ivCallOut = itemView.findViewById(R.id.iv_call_out)
            ivCallMisssed = itemView.findViewById(R.id.iv_call_missed)
            lineView = itemView.findViewById(R.id.line)
        }

    }
}
