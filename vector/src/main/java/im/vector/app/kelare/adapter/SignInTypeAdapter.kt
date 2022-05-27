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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import java.util.*

/**
 * author : Jason
 *  date   : 2022/5/6
 *  desc   :
 */
class SignInTypeAdapter(private val mContext: Activity, private val  selectedType: String, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<String>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_sign_in_type_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        holder.tvType.text = info

        if (selectedType == info) {
//            holder.llRoot.setBackgroundColor(mContext.resources.getColor(R.color.text_color_black1, null))
            holder.llRoot.setBackgroundColor(R.color.text_color_black1)
            holder.ivChecked.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvType: TextView
        var ivChecked: ImageView
        var llRoot: LinearLayout

        init {
            tvType = itemView.findViewById(R.id.tv_type)
            ivChecked = itemView.findViewById(R.id.iv_checked)
            llRoot = itemView.findViewById(R.id.root)
        }

    }
}
