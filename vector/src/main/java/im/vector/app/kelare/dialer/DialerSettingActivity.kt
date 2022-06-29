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

package im.vector.app.kelare.dialer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.otto.Subscribe
import com.yanzhenjie.recyclerview.OnItemClickListener
import com.yanzhenjie.recyclerview.OnItemMenuClickListener
import com.yanzhenjie.recyclerview.SwipeMenuCreator
import com.yanzhenjie.recyclerview.SwipeMenuItem
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityDialerContactDetailBinding
import im.vector.app.databinding.ActivityDialerSettingBinding
import im.vector.app.kelare.adapter.SettingSipAccountAdapter
import im.vector.app.kelare.adapter.SettingXMPPAccountAdapter
import im.vector.app.kelare.dialer.sip.SipLoginActivity
import im.vector.app.kelare.dialer.widget.BottomActionSheet
import im.vector.app.kelare.dialer.xmpp.XMPPLoginActivity
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.DeleteAccountInfoResponseEvent
import im.vector.app.kelare.network.event.DialerAccountInfoResponseEvent
import im.vector.app.kelare.network.models.DeleteAccountInfo
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.ItemInfo
import im.vector.app.kelare.utils.UIUtils
import timber.log.Timber

@AndroidEntryPoint
class DialerSettingActivity : VectorBaseActivity<ActivityDialerSettingBinding>(), View.OnClickListener,BottomActionSheet.OnActionSheetSelected {

    override fun getBinding() = ActivityDialerSettingBinding.inflate(layoutInflater)

    private var sipAccountAdapter: SettingSipAccountAdapter? = null
    private var xmppAccountAdapter: SettingXMPPAccountAdapter? = null
    private var sipAccountList : ArrayList<DialerAccountInfo> = ArrayList()
    private var xmppAccountList : ArrayList<DialerAccountInfo> = ArrayList()
    private var accountList: ArrayList<DialerAccountInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }

    override fun onResume() {
        super.onResume()

        accountList = dialerSession.accountListInfo!!
        Timber.e("account info: ${accountList}")
        renderSipAccount()
        renderXMPPAccount()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        views.tvSetting.text = "Setting"

        views.rlBack.setOnClickListener(this)
        views.ivAddAccount.setOnClickListener(this)

        initRecycler()
    }

    private fun initRecycler() {

        views.recyclerXmppAccount.setSwipeMenuCreator(xmppSwipeMenuCreator)
        views.recyclerXmppAccount.setOnItemMenuClickListener(xmppMenuClickListener)
        views.recyclerSipAccount.setSwipeMenuCreator(sipSwipeMenuCreator)
        views.recyclerSipAccount.setOnItemMenuClickListener(sipMenuClickListener)

        views.recyclerSipAccount.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, adapterPosition: Int) {
                val intent = Intent(this@DialerSettingActivity, SipLoginActivity::class.java)
                intent.putExtra("index", "2")
                intent.putExtra("account", sipAccountList[adapterPosition])
                startActivity(intent)
            }
        })

        views.recyclerXmppAccount.setOnItemClickListener(object :OnItemClickListener{
            override fun onItemClick(view: View?, adapterPosition: Int) {
                val intent = Intent(this@DialerSettingActivity, XMPPLoginActivity::class.java)
                intent.putExtra("index", "2")
                intent.putExtra("account", xmppAccountList[adapterPosition])
                startActivity(intent)
            }
        })

        // 默认构造，传入颜色即可。
        val itemDecoration: RecyclerView.ItemDecoration = DefaultItemDecoration(resources.getColor(R.color.line_gray, null), 0, 1)

        views.recyclerSipAccount.layoutManager = LinearLayoutManager(this)
        sipAccountAdapter = SettingSipAccountAdapter(this)
        views.recyclerSipAccount.adapter = sipAccountAdapter
        views.recyclerSipAccount.addItemDecoration(itemDecoration)

        views.recyclerXmppAccount.layoutManager = LinearLayoutManager(this)
        xmppAccountAdapter = SettingXMPPAccountAdapter(this)
        views.recyclerXmppAccount.adapter = xmppAccountAdapter
        views.recyclerXmppAccount.addItemDecoration(itemDecoration)
    }

    private fun renderSipAccount() {
        sipAccountList.clear()
        if (accountList.isNotEmpty()) {
            for (dialerAccountInfo in accountList) {
                if (dialerAccountInfo.type_value == "sip") {
                    sipAccountList.add(dialerAccountInfo)
                }
            }
        }

        sipAccountAdapter!!.clearDataList()
        sipAccountAdapter!!.addDataList(sipAccountList)
        sipAccountAdapter!!.notifyDataSetChanged()
    }

    private fun renderXMPPAccount() {
        xmppAccountList.clear()
        if (accountList.isNotEmpty()) {
            for (dialerAccountInfo in accountList) {
                if (dialerAccountInfo.type_value == "xmpp") {
                    xmppAccountList.add(dialerAccountInfo)
                }
            }
        }

        xmppAccountAdapter!!.clearDataList()
        xmppAccountAdapter!!.addDataList(xmppAccountList)
        xmppAccountAdapter!!.notifyDataSetChanged()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.iv_add_account-> {
                BottomActionSheet.showSheet(this, this, null)
            }
            else -> {
            }
        }
    }

    override fun onClick(whichButton: Int) {
        when (whichButton) {
            BottomActionSheet.CHOOSE_SIP -> {
                val intent = Intent(this, SipLoginActivity::class.java)
                intent.putExtra("index", "1")
                startActivity(intent)
            }
            BottomActionSheet.CHOOSE_XMPP -> {
                val intent = Intent(this, XMPPLoginActivity::class.java)
                intent.putExtra("index", "1")
                startActivity(intent)
            }
            BottomActionSheet.CANCEL -> {

            }
            else -> {
            }
        }
    }

    // create sip menu
    private var sipSwipeMenuCreator = SwipeMenuCreator { leftMenu, rightMenu, position ->
        val deleteItem = SwipeMenuItem(mContext)
        deleteItem.text = "delete"
        deleteItem.width  = 180
        deleteItem.height = UIUtils.dip2px(this, 40)  //40 is item height
        deleteItem.setTextColor(resources.getColor(R.color.white, null))
        deleteItem.setBackgroundColor(resources.getColor(R.color.red, null))
        rightMenu.addMenuItem(deleteItem) // add right menu

    }

    // create xmpp menu
    private var xmppSwipeMenuCreator = SwipeMenuCreator { leftMenu, rightMenu, position ->
        val deleteItem = SwipeMenuItem(mContext)
        deleteItem.text = "delete"
        deleteItem.width  = 180
        deleteItem.height = UIUtils.dip2px(this, 40)  //40 is item height
        deleteItem.setTextColor(resources.getColor(R.color.white, null))
        deleteItem.setBackgroundColor(resources.getColor(R.color.red, null))
        rightMenu.addMenuItem(deleteItem) // add right menu
    }

    //create xmpp menu listener
    private var xmppMenuClickListener: OnItemMenuClickListener = OnItemMenuClickListener{ menuBridge, position ->
        // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
        menuBridge.closeMenu()
        // 左侧还是右侧菜单：
        val direction = menuBridge.direction
        // 菜单在Item中的Position：
        val menuPosition = menuBridge.position

        Timber.e("menu position: $menuPosition")
        Timber.e("item position: $position")

        deleteAccount(xmppAccountList[position], 2)
    }

    //create sip menu listener
    private var sipMenuClickListener:OnItemMenuClickListener = OnItemMenuClickListener{menuBridge, position ->
        // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
        menuBridge.closeMenu()
        // 左侧还是右侧菜单：
        val direction = menuBridge.direction
        // 菜单在Item中的Position：
        val menuPosition = menuBridge.position

        Timber.e("menu position: $menuPosition")
        Timber.e("item position: $position")

        deleteAccount(sipAccountList[position], 1)
    }

    private fun deleteAccount(info: DialerAccountInfo, index: Int) {
        val deleteAccountInfo = DeleteAccountInfo()
        val itemInfo = ItemInfo()

        itemInfo.domain = info.domain
        itemInfo.username = info.username
        itemInfo.type_value = info.type_value

        deleteAccountInfo.sip_accounts!!.add(itemInfo)
        deleteAccountInfo.primary_user_id = dialerSession.userID


        if (index == 1) {
            deleteSipAccount(info)
        } else {
            updateConnectionList(info)
        }

        showLoadingDialog()
        HttpClient.deleteDialerAccountInfo(this, deleteAccountInfo)
    }

    @Subscribe
    fun onDeleteEvent(event: DeleteAccountInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            getDialerAccounts()
        }
    }

    private fun getDialerAccounts() {
        try {
            showLoadingDialog()
            HttpClient.getDialerAccountInfo(this, dialerSession.userID)
        } catch (e: Exception) {
            hideLoadingDialog()
        }
    }

    @Subscribe
    fun onDialerAccountEvent(event: DialerAccountInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            /*accountList = event.model!!.sip_accounts!!
            dialerSession.accountListInfo = event.model!!.sip_accounts!!
            Timber.e("account info: ${event.model!!.sip_accounts}")*/

            accountList.clear()
            event.model!!.sip_accounts!!.forEach {
                if (it.extension.accountName != null) {
                    accountList.add(it)
                }
            }
            dialerSession.accountListInfo = accountList
            Timber.e("account info: $accountList")

            renderSipAccount()
            renderXMPPAccount()
        } else {
        }
    }

    private fun deleteSipAccount(item: DialerAccountInfo) {
        val accountList = core.accountList
        if (accountList.isNotEmpty()) {
            for (account in accountList) {
                val domain = account.findAuthInfo()!!.domain.toString()
                val username = account.findAuthInfo()!!.username
                val info = "$username@$domain"
                if (info == (item.username + "@" + item.domain)) {
                    core.removeAccount(account)
                    break
                }
            }
        }
    }

    /**
     * only need to remove the enabled connection
     */
    private fun updateConnectionList(info: DialerAccountInfo) {
        var position = -1
        if (info.enabled) {
            if (mConnectionList.isNotEmpty()) {
                for (index in mConnectionList.indices) {
                    if (info.username + "@" + info.domain == mConnectionList[index].user.asBareJid().toString()) {
                        position = index
                        break
                    }
                }
            }
            mConnectionList.removeAt(position)
        }
    }
}
