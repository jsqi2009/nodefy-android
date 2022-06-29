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
 *  date   : 2022/3/9 13:49
 *  desc   :
 */
class SipContactAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener, private val mLongClickListener: OnItemLongClickListener) : AbstractBaseRecyclerAdapter<DialerContactInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_dialer_contact_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        holder.tvUsername.text = info.first_name + " " + info.last_name

        holder.ivAvatar.setImageDrawable(
            AvatarGenerator.AvatarBuilder(context)
                .setLabel(info.first_name!!)
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

        holder.itemView.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                return mLongClickListener.onItemLongClick(holder.itemView, position)
            }
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvUsername: TextView
        var ivAvatar: ImageView

        init {
            tvUsername = itemView.findViewById(R.id.tv_username)
            ivAvatar = itemView.findViewById(R.id.iv_avatar)
        }

    }
}
