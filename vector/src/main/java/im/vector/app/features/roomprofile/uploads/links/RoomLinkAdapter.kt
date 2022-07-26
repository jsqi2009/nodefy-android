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

package im.vector.app.features.roomprofile.uploads.links

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.adapter.AbstractBaseRecyclerAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener

class RoomLinkAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<String>(mContext){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = LayoutInflater.from(mContext)
        val view = context.inflate(R.layout.item_room_link_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        holder.tvName.text = info

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvName: TextView
        var lineView: View

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            lineView = itemView.findViewById(R.id.line)
        }
    }
}
