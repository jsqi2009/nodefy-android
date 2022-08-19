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

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.network.models.PhoneInfo

/**
 * author : Jason
 *  date   : 2022/3/9 13:49
 *  desc   :
 */
class DialerPhoneAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<PhoneInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_dialer_phone_list, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        //holder.tvUsername.text = info.number
        if (info.number!!.contains("@")) {
            holder.tvUsername.text = info.number!!.split("@")[0]
        } else {
            holder.tvUsername.text = info.number
        }


        if (position == 0) {
            holder.lineView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvUsername: TextView
        var ivAvatar: ImageView
        var lineView: View

        init {
            tvUsername = itemView.findViewById(R.id.tv_username)
            ivAvatar = itemView.findViewById(R.id.iv_avatar)
            lineView = itemView.findViewById(R.id.line)
        }

    }
}
