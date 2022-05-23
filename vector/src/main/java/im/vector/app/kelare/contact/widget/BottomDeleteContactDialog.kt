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

package im.vector.app.kelare.contact.widget

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import im.vector.app.R
import im.vector.app.kelare.network.models.DialerContactInfo

/**
 * author : Jason
 *  date   : 2022/5/23 13:50
 *  desc   :
 */
object BottomDeleteContactDialog {
    const val CHOOSE_VALUE = 100
    const val CANCEL = 300

    @SuppressLint("StringFormatInvalid")
    fun showSheet(
            context: Context,
            info: DialerContactInfo,
            itemSelected: OnItemSelected,
            cancelListener: DialogInterface.OnCancelListener?
    ): Dialog {
        val dialog = Dialog(context, R.style.BottomActionSheet)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.dialog_bottom_delete_contact, null) as LinearLayout
        val cFullFillWidth = 10000
        layout.minimumWidth = cFullFillWidth
        val tvName = layout.findViewById<View>(R.id.tv_name) as TextView
        val tvCancel = layout.findViewById<View>(R.id.tv_cancel) as TextView

        val value = context.getString(R.string.dialer_message_delete_group, info.last_name + " " +  info.first_name)
        tvName.text = value

        tvName.setOnClickListener {
            itemSelected.onClick(CHOOSE_VALUE, info)
            dialog.dismiss()
        }

        tvCancel.setOnClickListener {
            itemSelected.onClick(CANCEL, info)
            dialog.dismiss()
        }

        val window = dialog.window
        val lp = window!!.attributes
        lp.x = 0
        val cMakeBottom = -1000
        lp.y = cMakeBottom
        lp.gravity = Gravity.BOTTOM
        dialog.onWindowAttributesChanged(lp)
        dialog.setCanceledOnTouchOutside(true)
        if (cancelListener != null) dialog.setOnCancelListener(cancelListener)
        dialog.setContentView(layout)
        dialog.show()
        return dialog
    }

    interface OnItemSelected {
        fun onClick(whichButton: Int, info: DialerContactInfo)
    }
}
