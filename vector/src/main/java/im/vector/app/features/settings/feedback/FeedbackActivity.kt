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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFeedbackBinding
import timber.log.Timber

@AndroidEntryPoint
class FeedbackActivity : VectorBaseActivity<ActivityFeedbackBinding>(){

    override fun getBinding() = ActivityFeedbackBinding.inflate(layoutInflater)

    //private var localUrl: String = "file:///android_asset/ReportAnIssue.html"
    private var localUrl: String = "https://nodefy.me/jira/public/nodand"

    var mUploadMessage: ValueCallback<Uri?>? = null
    private var mUploadCallbackAboveL: ValueCallback<Array<Uri?>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
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
        views.webView.settings.setGeolocationEnabled(true)
        views.webView.settings.databaseEnabled = true
        views.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        views.webView.fitsSystemWindows = true
        views.webView.webViewClient = webViewClient()
        views.webView.setWebChromeClient(webChromeClient())
        views.webView.addJavascriptInterface(this, "androidBridge")

        views.webView.loadUrl(localUrl)
    }

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    fun jsCallAndroid(msg: String) {
        //JS call android method
        Timber.e("js message---%s", msg)
        if (msg.equals("cancel", ignoreCase = true)) {
            finish()
        } else if (msg.equals("submit", ignoreCase = true)) {
            Toast.makeText(this, "submit success", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "submit error, you can try again", Toast.LENGTH_LONG).show()
        }
    }

    inner class webViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }
    }

    inner class webChromeClient : WebChromeClient() {
        // For Android < 3.0
        fun openFileChooser(valueCallback: ValueCallback<Uri?>) {
            mUploadMessage = valueCallback
        }

        //For Android  >= 4.1
        fun openFileChooser(valueCallback: ValueCallback<Uri?>, acceptType: String?, capture: String?) {
            mUploadMessage = valueCallback
        }

        // For Android >= 5.0
        override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri?>>, fileChooserParams: FileChooserParams): Boolean {
            mUploadCallbackAboveL = filePathCallback
            openFileChooserActivity()
            return true
        }
    }

    private fun openFileChooserActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_CHOOSER_RESULT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return
            val result = if (data == null || resultCode != RESULT_OK) null else data.data
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (mUploadMessage != null) {
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null
            }
        }
    }

    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadCallbackAboveL == null) return
        var results: Array<Uri?>? = null
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOfNulls(clipData.itemCount)
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results[i] = item.uri
                    }
                }
                if (dataString != null) results = arrayOf(Uri.parse(dataString))
            }
        }
        mUploadCallbackAboveL!!.onReceiveValue(results)
        mUploadCallbackAboveL = null
    }
}
