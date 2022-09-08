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

package im.vector.app.features.crypto.authaccount

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.hbb20.CountryCodePicker
import com.pedaily.yc.ycdialoglib.toast.ToastUtils.showToast
import im.vector.app.R
import timber.log.Timber

/**
 * author : Jason
 *  date   : 2022/9/8 11:21
 *  desc   :
 */
class AuthenticateTelegramAccountBottomSheet (val mContext: Context) : BottomSheetDialogFragment(), View.OnClickListener {


    private var authView: TextView? = null
    private var phoneView: TextInputEditText? = null
    private var codePicker: CountryCodePicker? = null

    interface InteractionListener {
        fun onSave()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) return super.onCreateDialog(savedInstanceState)
        val bottomDialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialog)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_authenticate_telegram_account, null)
        bottomDialog.setContentView(rootView)
        bottomDialog.setCanceledOnTouchOutside(false)
        //set height
        val params = rootView.layoutParams
        params.height = (0.85 * resources.displayMetrics.heightPixels).toInt()
        rootView.layoutParams = params

        initView(rootView)

        return bottomDialog
    }

    private fun initView(rootView: View) {

        authView = rootView.findViewById(R.id.authTelegramView)
        phoneView = rootView.findViewById(R.id.phoneField)
        codePicker = rootView.findViewById(R.id.codePicker)

        codePicker!!.registerCarrierNumberEditText(phoneView)
        codePicker!!.setOnCountryChangeListener {
            phoneView!!.setText("")
        }

        authView!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.authTelegramView -> {
                telegramAuth()
            }
            else -> {}
        }
    }

    private fun telegramAuth() {
        val phoneNumber = phoneView!!.text.toString()

        if (TextUtils.isEmpty(phoneNumber)) {
            showToast(getString(R.string.auth_telegram_phone_input_tips))
            return
        }

        Timber.e("country code1---->${codePicker!!.fullNumber}")
        Timber.e("country code2---->${codePicker!!.fullNumberWithPlus}")
        Timber.e("country code3---->${codePicker!!.formattedFullNumber}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val view: FrameLayout = dialog?.findViewById(R.id.design_bottom_sheet)!!
        val behavior = BottomSheetBehavior.from(view)
        behavior.peekHeight = 3000
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
