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

package im.vector.app.features.accountcontact.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.AnyThread
import androidx.annotation.ColorInt
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import im.vector.app.core.glide.GlideApp
import im.vector.app.core.glide.GlideRequest
import im.vector.app.core.glide.GlideRequests
import im.vector.app.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import im.vector.app.kelare.network.models.AccountContactInfo
import org.matrix.android.sdk.api.extensions.tryOrNull
import java.util.Locale

/**
 * author : Jason
 *  date   : 2022/8/22 15:32
 *  desc   :
 */
object AvatarRendererUtil {

    private val cache = mutableMapOf<String, Int>()

    @UiThread
    fun render(mContext: Context, matrixItem: AccountContactInfo, imageView: ImageView) {
        render(
                mContext,
                GlideApp.with(imageView),
                matrixItem,
                DrawableImageViewTarget(imageView)
        )
    }

    fun clear(imageView: ImageView) {
        // It can be called after recycler view is destroyed, just silently catch
        tryOrNull { GlideApp.with(imageView).clear(imageView) }
    }

    @UiThread
    fun render(mContext: Context,
               glideRequests: GlideRequests,
               matrixItem: AccountContactInfo,
               target: Target<Drawable>) {
        val placeholder = getPlaceholderDrawable(mContext, matrixItem)
        glideRequests.loadResolvedUrl(matrixItem.avatar_url).apply(RequestOptions.circleCropTransform())
                .placeholder(placeholder)
                .into(target)
    }

    @AnyThread
    fun getPlaceholderDrawable(mContext: Context, matrixItem: AccountContactInfo): Drawable {
        val avatarColor = getColor(mContext, matrixItem)
        return TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig().buildRound(firstLetterOfDisplayName(matrixItem), avatarColor)
    }

    // PRIVATE API *********************************************************************************

    private fun GlideRequests.loadResolvedUrl(avatarUrl: String?): GlideRequest<Drawable> {
        return load(avatarUrl)
    }

    @ColorInt
    fun getColor(mContext: Context, matrixItem: AccountContactInfo): Int {
        return cache.getOrPut(matrixItem.contacts_id!!) {
            ContextCompat.getColor(mContext, MatrixItemColorProvider.getColorFromUserId(matrixItem.contacts_id))
        }
    }

    private fun firstLetterOfDisplayName(item: AccountContactInfo): String {
        val displayName = item.displayname
        return (displayName?.takeIf { it.isNotBlank() } ?: item.contacts_id!!)
                .let { dn ->
                    var startIndex = 0
                    val initial = dn[startIndex]

                    if (initial in listOf('@', '#', '+') && dn.length > 1) {
                        startIndex++
                    }

                    var length = 1
                    var first = dn[startIndex]

                    // LEFT-TO-RIGHT MARK
                    if (dn.length >= 2 && 0x200e == first.code) {
                        startIndex++
                        first = dn[startIndex]
                    }

                    // check if it’s the start of a surrogate pair
                    if (first.code in 0xD800..0xDBFF && dn.length > startIndex + 1) {
                        val second = dn[startIndex + 1]
                        if (second.code in 0xDC00..0xDFFF) {
                            length++
                        }
                    }

                    dn.substring(startIndex, startIndex + length)
                }
                .uppercase(Locale.ROOT)
    }
}
