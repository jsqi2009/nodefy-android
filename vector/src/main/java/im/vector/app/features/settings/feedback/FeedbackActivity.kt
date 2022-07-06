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

package im.vector.app.features.settings.feedback

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFeedbackBinding
import im.vector.app.databinding.ActivitySipTransportBinding
import timber.log.Timber

@AndroidEntryPoint
class FeedbackActivity : VectorBaseActivity<ActivityFeedbackBinding>(){

    override fun getBinding() = ActivityFeedbackBinding.inflate(layoutInflater)

    private var localUrl: String = "file:///android_asset/ReportAnIssue.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {

        views.rlBack.setOnClickListener {
            finish()
        }

        views.webView.settings.javaScriptEnabled = true
        views.webView.settings.useWideViewPort = true
        views.webView.settings.loadWithOverviewMode = true
        views.webView.settings.loadsImagesAutomatically = true
        views.webView.settings.blockNetworkImage = true
        views.webView.settings.allowFileAccess = true
        views.webView.settings.domStorageEnabled = true
        views.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        views.webView.settings.databaseEnabled = true
        views.webView.fitsSystemWindows = true

        views.webView.addJavascriptInterface(this, "bridge")

        views.webView.loadUrl(localUrl)
    }

    @JavascriptInterface
    fun nativeMethod(content: String) {
        Timber.e("js value------$content")
    }
}
