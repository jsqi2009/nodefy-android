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

package im.vector.app.kelare.base

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.gyf.immersionbar.ImmersionBar
import com.kaopiz.kprogresshud.KProgressHUD
import im.vector.app.R

/**
 * author : Jason
 *  date   : 2022/5/12 17:55
 *  desc   :
 */
open class BaseActivity: FragmentActivity() {

    private var loadingDialog: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showToast(message: String) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showLoadingDialog() {
        if (this.loadingDialog == null || !this.loadingDialog!!.isShowing) {
            loadingDialog = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setDimAmount(0.5f)
            loadingDialog!!.show()
        }
    }

    fun hideLoadingDialog() {
        if (this.loadingDialog != null && this.loadingDialog!!.isShowing) {
            this.loadingDialog!!.dismiss()
            this.loadingDialog = null
        }
    }

    fun statusBarColor(activity: Activity) {
        ImmersionBar.with(activity)
                .statusBarColor(R.color.app_color)
                .fitsSystemWindows(true)
                .init()
    }

    fun statusBarWhiteColor(activity: Activity) {
        ImmersionBar.with(activity)
                .statusBarColor(R.color.white)
                .statusBarDarkFont(true)
                .fitsSystemWindows(true)
                .init()
    }
}
