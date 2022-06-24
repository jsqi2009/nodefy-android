/*
 * Copyright (c) 2021 New Vector Ltd
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

package im.vector.app.features.home.room.list

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.core.epoxy.ClickListener
import im.vector.app.core.epoxy.onClick
import im.vector.app.databinding.ItemRoomCategoryBinding
import im.vector.app.features.home.event.CreateGroupRoomEvent
import im.vector.app.features.home.event.ToPublicDetailsEvent
import im.vector.app.features.themes.ThemeUtils
import im.vector.app.kelare.content.AndroidBus

class SectionHeaderAdapter(
        roomsSectionData: RoomsSectionData,
        mBus: AndroidBus,
        private val onClickAction: ClickListener
) : RecyclerView.Adapter<SectionHeaderAdapter.VH>() {

    data class RoomsSectionData(
            val name: String,
            val itemCount: Int = 0,
            val isExpanded: Boolean = true,
            val notificationCount: Int = 0,
            val isHighlighted: Boolean = false,
            val isHidden: Boolean = true,
            // This will be false until real data has been submitted once
            val isLoading: Boolean = true,
            val isCollapsable: Boolean = true
    )

    private val eventBus: AndroidBus = mBus
    var roomsSectionData: RoomsSectionData = roomsSectionData
        private set

    fun updateSection(block: (RoomsSectionData) -> RoomsSectionData) {
        val newRoomsSectionData = block(roomsSectionData)
        if (roomsSectionData != newRoomsSectionData) {
            roomsSectionData = newRoomsSectionData
            notifyDataSetChanged()
        }
    }

    fun updateSectionData(block: (RoomsSectionData) -> RoomsSectionData) {
        notifyDataSetChanged()
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = roomsSectionData.hashCode().toLong()

    override fun getItemViewType(position: Int) = R.layout.item_room_category

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH.create(parent, onClickAction)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(roomsSectionData, eventBus)
    }

    override fun getItemCount(): Int = if (roomsSectionData.isHidden) 0 else 1

    class VH constructor(
            private val binding: ItemRoomCategoryBinding,
            onClickAction: ClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.onClick(onClickAction)
        }

        fun bind(roomsSectionData: RoomsSectionData, mBus: AndroidBus) {
            binding.roomCategoryTitleView.text = roomsSectionData.name
            //val tintColor = ThemeUtils.getColor(binding.root.context, R.attr.vctr_content_secondary)
            val tintColor = binding.root.context.getColor(R.color.keep_it_save_continue_color)
            val collapsableArrowDrawable: Drawable? = if (roomsSectionData.isCollapsable) {
                val expandedArrowDrawableRes = if (roomsSectionData.isExpanded) R.drawable.ic_expand_more else R.drawable.ic_expand_less
                ContextCompat.getDrawable(binding.root.context, expandedArrowDrawableRes)?.also {
                    DrawableCompat.setTint(it, tintColor)
                }
            } else {
                null
            }
            binding.root.isClickable = roomsSectionData.isCollapsable
            binding.roomCategoryCounterView.setCompoundDrawablesWithIntrinsicBounds(null, null, collapsableArrowDrawable, null)
            //binding.roomCategoryCounterView.text = roomsSectionData.itemCount.takeIf { it > 0 }?.toString().orEmpty()
            binding.roomCategoryUnreadCounterBadgeView.render(UnreadCounterBadgeView.State(roomsSectionData.notificationCount, roomsSectionData.isHighlighted))

            //public   favorite     direct messages   group   low priority
            if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms_public).lowercase()) {
                binding.typeImageView.setImageResource(R.drawable.ic_public_group)
            } else if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_favourites).lowercase()){
                binding.typeImageView.setImageResource(R.drawable.ic_favorite)
            }else if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_people_x).lowercase()){
                binding.typeImageView.setImageResource(R.drawable.ic_message_section_icon)
            }else if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms2).lowercase()){
                binding.typeImageView.setImageResource(R.drawable.ic_group_section_icon)
            } else if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.low_priority_header).lowercase()){
                binding.typeImageView.setImageResource(R.drawable.ic_low_priority)
            }else {
                binding.typeImageView.setImageResource(R.drawable.ic_message_section_icon)
            }

            if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms_public).lowercase()) {
                binding.roomCategoryCounterView.visibility = View.INVISIBLE
            } else {
                binding.roomCategoryCounterView.visibility = View.VISIBLE
            }

            if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms_public).lowercase()
                    || roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_favourites).lowercase()
                    || roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.low_priority_header).lowercase()) {
                binding.roomAddImageView.visibility = View.GONE
            }

            binding.roomAddImageView.setOnClickListener {
                mBus.post(CreateGroupRoomEvent(roomsSectionData.name))
            }

            binding.roomCategoryTitleView.setOnClickListener {
                if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms_public).lowercase()) {
                    mBus.post(ToPublicDetailsEvent())
                }
            }

            if (roomsSectionData.itemCount == 0) {
                if (roomsSectionData.isExpanded) {
                    if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_people_x).lowercase()) {
                        binding.roomNoDataTips.text = binding.root.context.getString(R.string.home_no_conversations)
                        binding.roomNoDataTips.visibility = View.VISIBLE
                    } else if (roomsSectionData.name.lowercase() == binding.root.context.getString(R.string.bottom_action_rooms2).lowercase()) {
                        binding.roomNoDataTips.text = binding.root.context.getString(R.string.home_no_groups)
                        binding.roomNoDataTips.visibility = View.VISIBLE
                    }
                } else {
                    binding.roomNoDataTips.visibility = View.GONE
                }
            } else {
                binding.roomNoDataTips.visibility = View.GONE
            }
        }

        companion object {
            fun create(parent: ViewGroup, onClickAction: ClickListener): VH {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_room_category, parent, false)
                val binding = ItemRoomCategoryBinding.bind(view)
                return VH(binding, onClickAction)
            }
        }
    }
}
