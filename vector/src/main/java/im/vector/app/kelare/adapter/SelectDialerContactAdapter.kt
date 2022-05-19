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
import com.avatarfirst.avatargenlib.AvatarGenerator
import im.vector.app.R
import im.vector.app.kelare.network.models.DialerContactInfo

/**
 * author : Jason
 *  date   : 2022/4/29
 *  desc   :
 */
class SelectDialerContactAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<DialerContactInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_select_dialer_contact_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        holder.tvContactUsername.text = info.last_name + " " + info.first_name

        if (info.isSelected!!) {
            holder.ivChecked.visibility = View.VISIBLE
            holder.ivUncheck.visibility = View.GONE
        } else {
            holder.ivChecked.visibility = View.GONE
            holder.ivUncheck.visibility = View.VISIBLE
        }

        holder.ivAvatar.setImageDrawable(
            AvatarGenerator.AvatarBuilder(context)
                .setLabel(info.last_name!!)
                .setAvatarSize(120)
                .setTextSize(30)
                .toSquare()
                .toCircle()
                .setBackgroundColor(mContext.resources.getColor(R.color.room_avatar_color, null))
                .build()
        )

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvContactUsername: TextView
        var ivAvatar: ImageView
        var ivChecked: ImageView
        var ivUncheck: ImageView

        init {
            tvContactUsername = itemView.findViewById(R.id.tv_contact_username)
            ivAvatar = itemView.findViewById(R.id.iv_avatar)
            ivChecked = itemView.findViewById(R.id.iv_checked)
            ivUncheck = itemView.findViewById(R.id.iv_uncheck)
        }

    }
}
