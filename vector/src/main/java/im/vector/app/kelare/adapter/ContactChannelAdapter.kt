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

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import im.vector.app.R
import im.vector.app.core.glide.GlideApp
import im.vector.app.core.glide.GlideRequest
import im.vector.app.core.glide.GlideRequests
import im.vector.app.features.accountcontact.util.AvatarRendererUtil
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ContactChannelInfo
import org.matrix.android.sdk.api.session.content.ContentUrlResolver
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toMatrixItem

/**
 * author : Jason
 *  desc   :
 */
class ContactChannelAdapter(val mContext: Context, data: ArrayList<ContactChannelInfo>) : BaseQuickAdapter<ContactChannelInfo, BaseViewHolder>(
        R.layout.item_contact_channel_list, data
) {

    override fun convert(holder: BaseViewHolder, item: ContactChannelInfo) {

        holder.setText(R.id.tv_username, mContext.getString(R.string.account_contact_set_use) + " " + item.displayType)

        if (item.isDefault) {
            holder.getView<TextView>(R.id.defaultStatus).visibility = View.VISIBLE
        } else {
            holder.getView<TextView>(R.id.defaultStatus).visibility = View.GONE
        }
    }

}
