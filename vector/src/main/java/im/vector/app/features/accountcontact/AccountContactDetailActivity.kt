package im.vector.app.features.accountcontact

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.mylhyl.circledialog.CircleDialog
import com.mylhyl.circledialog.view.listener.OnButtonClickListener
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.error.ErrorFormatter
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.platform.WaitingViewData
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.features.accountcontact.util.AvatarRendererUtil
import im.vector.app.features.accountcontact.widget.AssociateContactBottomDialog
import im.vector.app.features.accountcontact.widget.SetDefaultChannelDialog
import im.vector.app.features.analytics.plan.ViewRoom
import im.vector.app.features.createdirect.CreateDirectRoomAction
import im.vector.app.features.createdirect.CreateDirectRoomViewModel
import im.vector.app.features.createdirect.CreateDirectRoomViewState
import im.vector.app.kelare.content.Contants
import im.vector.app.kelare.dialer.call.DialerCallActivity
import im.vector.app.kelare.message.PeopleChatMessageActivity
import im.vector.app.kelare.message.SendMessageActivity
import im.vector.app.kelare.message.widget.CreateXMPPGroupDialog
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.DefaultContactRelationResponseEvent
import im.vector.app.kelare.network.event.DeleteContactRelationResponseEvent
import im.vector.app.kelare.network.event.GetContactRelationResponseEvent
import im.vector.app.kelare.network.event.SetDefaultCallAccountEvent
import im.vector.app.kelare.network.event.SetDefaultMessageAccountEvent
import im.vector.app.kelare.network.event.UpdateContactRelationResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ChildrenInfo
import im.vector.app.kelare.network.models.ChildrenUserInfo
import im.vector.app.kelare.network.models.ContactChannelInfo
import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DefaultContactRelationInfo
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.UpdateContactRelationInfo
import im.vector.app.kelare.network.models.XmppContact
import im.vector.app.kelare.widget.SipAccountPopup
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.failure.CreateRoomFailure
import timber.log.Timber
import java.net.HttpURLConnection
import java.util.Locale
import javax.inject.Inject

private const val nodefyType  = "nodefy"

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener,
        AssociateContactBottomDialog.InteractionListener, SetDefaultChannelDialog.ChannelListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    private val viewModel: ContactCreateDirectRoomViewModel by viewModel()

    @Inject lateinit var sessionHolder: ActiveSessionHolder
    @Inject lateinit var errorFormatter: ErrorFormatter

    private var session: Session? = null
    private var loading: KProgressHUD? = null
    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var sipContactList:ArrayList<DialerContactInfo> = ArrayList()
    private var xmppContactList: ArrayList<XmppContact> = ArrayList()
    private var targetContact: AccountContactInfo = AccountContactInfo()
    private var relationsList: ArrayList<ContactRelationInfo> = ArrayList()
    private var defaultRelation: ContactRelationInfo? = ContactRelationInfo()
    private var contactChannelList: ArrayList<ContactChannelInfo> = ArrayList()

    //sip
    private var sipContactInfo:DialerContactInfo = DialerContactInfo()
    private var sipAccountList:ArrayList<DialerAccountInfo> = ArrayList()
    private var selectedAccount: DialerAccountInfo = DialerAccountInfo()
    private var defaultNumber:String? = null
    private var defaultSipNumber:String? = null

    private var isEdit = false
    private var defaultChanelType: String = "nodefy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
        getRelations(true)

        viewModel.onEach(CreateDirectRoomViewState::createAndInviteState) {
            renderCreateAndInviteState(it)
        }
    }

    override fun onResume() {
        super.onResume()
        getRegisterSIPUser()
    }

    private fun renderCreateAndInviteState(state: Async<String>) {
        when (state) {
            is Loading -> renderCreationLoading()
            is Success -> renderCreationSuccess(state())
            is Fail    -> renderCreationFailure(state.error)
            else       -> Unit
        }
    }

    private fun initView() {

        session = sessionHolder.getActiveSession()

        contactList = intent.getSerializableExtra("contactList") as ArrayList<AccountContactInfo>
        sipContactList = intent.getSerializableExtra("sipContactList") as ArrayList<DialerContactInfo>
        xmppContactList = intent.getSerializableExtra("xmppContactList") as ArrayList<XmppContact>
        targetContact = intent.getSerializableExtra("item") as AccountContactInfo

        Timber.d("contact item info---${contactList[0]}")
        Timber.d("sip contact item info---${sipContactList}")
        Timber.d("xmpp contact item info---${xmppContactList}")
        Timber.d("item info---$targetContact")

        if (targetContact.contacts_type!!.lowercase() == nodefyType) {
            views.associateLayout.visibility = View.VISIBLE
        } else {
            views.associateLayout.visibility = View.GONE
            views.tvEdit.visibility = View.GONE
        }

        views.tvUsername.text = targetContact.displayname
        AvatarRendererUtil.render(mContext, targetContact, views.ivAvatar)

        views.rlBack.setOnClickListener(this)
        views.tvEdit.setOnClickListener(this)
        views.setDefaultChannel.setOnClickListener(this)
        views.sipAssociate.setOnClickListener(this)
        views.xmppAssociate.setOnClickListener(this)
        views.skypeAssociate.setOnClickListener(this)
        views.slackAssociate.setOnClickListener(this)
        views.telegramAssociate.setOnClickListener(this)
        views.whatsappAssociate.setOnClickListener(this)
        views.sipDelete.setOnClickListener(this)
        views.xmppDelete.setOnClickListener(this)
        views.skypeDelete.setOnClickListener(this)
        views.slackDelete.setOnClickListener(this)
        views.telegramDelete.setOnClickListener(this)
        views.whatsappDelete.setOnClickListener(this)
        views.llMessage.setOnClickListener(this)
        views.llCall.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_edit -> {
                isEdit = !isEdit
                refreshUI()
            }
            R.id.ll_message -> {
                contactMessage()
            }
            R.id.ll_call -> {
                contactCall()
            }
            R.id.setDefaultChannel -> {
                val channelDialog = SetDefaultChannelDialog(this, mBus, contactChannelList, this)
                channelDialog.show()
            }
            R.id.sipAssociate -> {
                associateContact(getString(R.string.account_contact_sip))
            }
            R.id.xmppAssociate -> {
                associateContact(getString(R.string.account_contact_xmpp))
            }
            R.id.skypeAssociate -> {
                associateContact(getString(R.string.account_contact_skype))
            }
            R.id.slackAssociate -> {
                associateContact(getString(R.string.account_contact_slack))
            }
            R.id.telegramAssociate -> {
                associateContact(getString(R.string.account_contact_telegram))
            }
            R.id.whatsappAssociate -> {
                associateContact(getString(R.string.account_contact_whatsapp))
            }
            R.id.sipDelete -> {
                deleteAction(getString(R.string.account_contact_sip))
            }
            R.id.xmppDelete -> {
                deleteAction(getString(R.string.account_contact_xmpp))
            }
            R.id.skypeDelete -> {
                deleteAction(getString(R.string.account_contact_skype))
            }
            R.id.slackDelete -> {
                deleteAction(getString(R.string.account_contact_slack))
            }
            R.id.telegramDelete -> {
                deleteAction(getString(R.string.account_contact_telegram))
            }
            R.id.whatsappDelete -> {
                deleteAction(getString(R.string.account_contact_whatsapp))
            }
            else         -> {}
        }
    }

    private fun associateContact(accountType: String) {
        if (isEdit) {
            return
        }
        AssociateContactBottomDialog(this, mBus, accountType, session!!.myUserId, mConnectionList, dialerSession,
                contactList, sipContactList, xmppContactList, targetContact, relationsList,this).show(supportFragmentManager, "associate")
    }

    private fun refreshUI() {

        if (!isEdit) {
            views.tvEdit.text = getString(R.string.account_contact_edit)
        } else {
            views.tvEdit.text = getString(R.string.account_contact_sheet_cancel)
        }

        if (isEdit) {
            relationsList.forEach {
                if (it.account_type!!.lowercase() == Contants.SIP_TYPE) {
                    views.sipDelete.visibility = View.VISIBLE
                }
                if (it.account_type!!.lowercase() == Contants.XMPP_TYPE) {
                    views.xmppDelete.visibility = View.VISIBLE
                }
                if (it.account_type!!.lowercase() == Contants.SKYPE_TYPE) {
                    views.skypeDelete.visibility = View.VISIBLE
                }
                if (it.account_type!!.lowercase() == Contants.SLACK_TYPE) {
                    views.slackDelete.visibility = View.VISIBLE
                }
                if (it.account_type!!.lowercase() == Contants.TELEGRAM_TYPE) {
                    views.telegramDelete.visibility = View.VISIBLE
                }
                if (it.account_type!!.lowercase() == Contants.WHATSAPP_TYPE) {
                    views.whatsappDelete.visibility = View.VISIBLE
                }
            }

        } else {
            views.sipDelete.visibility = View.GONE
            views.xmppDelete.visibility = View.GONE
            views.skypeDelete.visibility = View.GONE
            views.slackDelete.visibility = View.GONE
            views.telegramDelete.visibility = View.GONE
            views.whatsappDelete.visibility = View.GONE
        }
    }

    private fun getRelations(isShow: Boolean) {
        if (isShow) {
            showLoading()
        }
        HttpClient.getContactRelations(this@AccountContactDetailActivity, targetContact.contacts_id!!)
    }

    @Subscribe
    fun onRelationsEvent(event: GetContactRelationResponseEvent) {
        hideLoading()
        if (event.isSuccess) {
            relationsList.clear()
            relationsList = event.model!!.children_users
            Timber.e("relations list----->${Gson().toJson(relationsList)}")

            renderAssociateInfo()
            refreshUI()
            setDefaultRelation()
        }
    }

    private fun renderAssociateInfo() {
        relationsList.forEach { item ->
            if (item.account_type!!.lowercase() == Contants.SIP_TYPE) {
                sipContactList.forEach {
                    if (it.id == item.user_id) {
                        views.sipAssociate.text = it.first_name
                        return@forEach
                    }
                }
            }
            if (item.account_type!!.lowercase() == Contants.XMPP_TYPE) {
                xmppContactList.forEach {
                    if (it.jid.toString() == item.user_id) {
                        views.xmppAssociate.text = it.jid
                        return@forEach
                    }
                }
            }

            contactList.forEach {
                if (item.account_type!!.lowercase() == Contants.SLACK_TYPE && it.contacts_id == item.user_id) {
                    views.slackAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == Contants.SKYPE_TYPE && it.contacts_id == item.user_id) {
                    views.skypeAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == Contants.TELEGRAM_TYPE && it.contacts_id == item.user_id) {
                    views.telegramAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == Contants.WHATSAPP_TYPE && it.contacts_id == item.user_id) {
                    views.whatsappAssociate.text = it.displayname
                }
            }
        }
    }

    private fun deleteAction(type: String) {
        relationsList.forEach {
            if (it.account_type!!.lowercase() == type.lowercase()) {
                confirmDeleteDialog(it)
            }
        }
    }

    private fun confirmDeleteDialog(info: ContactRelationInfo) {

        CircleDialog.Builder()
                .setTitle("Tips")
                .setTitleColor(resources.getColor(R.color.black, null))
                .configTitle() { params ->
                    params.textSize = 14
                }
                .setWidth(0.75f)
                .setText("Are you sure to delete?")
                .setTextColor(resources.getColor(R.color.text_color_black, null))
                .configText { params ->
                    params.textSize = 12
                }
                .setPositive("Delete", object : OnButtonClickListener {
                    override fun onClick(v: View?): Boolean {
                        deleteRelation(info)
                        return true
                    }
                })
                .configPositive { params ->
                    params!!.textColor = resources.getColor(R.color.red, null)
                    params!!.textSize = 14
                }
                .setNegative("Cancel") { true }
                .configNegative { params ->
                    params!!.textColor = resources.getColor(R.color.colorPrimary, null)
                    params!!.textSize = 14
                }
                .show(supportFragmentManager)
    }

    private fun deleteRelation(item: ContactRelationInfo) {
        showLoading()

        val childrenInfo = ChildrenUserInfo()
        childrenInfo.user_id = item.user_id
        childrenInfo.account_type = item.account_type
        childrenInfo.is_main = false

        val info: UpdateContactRelationInfo = UpdateContactRelationInfo()
        info.primary_user_id = targetContact.contacts_id
        info.children_users.add(childrenInfo)
        HttpClient.deleteContactRelation(this, info, item.account_type!!.lowercase())
    }

    @Subscribe
    fun onDeleteRelationEvent(event: DeleteContactRelationResponseEvent) {
        hideLoading()
        if (event.isSuccess) {
            resetUI(event.model!!.flag)
            getRelations(false)
        }
    }

    private fun resetUI(type: String) {
        when (type) {
            Contants.SIP_TYPE -> {
                views.sipDelete.visibility = View.GONE
                views.sipAssociate.text = getString(R.string.account_contact_associate)
            }
            Contants.XMPP_TYPE        -> {
                views.xmppDelete.visibility = View.GONE
                views.xmppAssociate.text = getString(R.string.account_contact_associate)
            }
            Contants.SKYPE_TYPE    -> {
                views.skypeDelete.visibility = View.GONE
                views.skypeAssociate.text = getString(R.string.account_contact_associate)
            }
            Contants.SLACK_TYPE    -> {
                views.slackDelete.visibility = View.GONE
                views.slackAssociate.text = getString(R.string.account_contact_associate)
            }
            Contants.TELEGRAM_TYPE -> {
                views.telegramDelete.visibility = View.GONE
                views.telegramAssociate.text = getString(R.string.account_contact_associate)
            }
            Contants.WHATSAPP_TYPE-> {
                views.whatsappDelete.visibility = View.GONE
                views.whatsappAssociate.text = getString(R.string.account_contact_associate)
            }
        }
    }

    private fun setDefaultRelation() {
        defaultRelation = null
        contactChannelList.clear()
        var isHasDefault = false

        //add default nodefy channel
        val item: ContactChannelInfo = ContactChannelInfo()
        item.contacts_id = targetContact.contacts_id
        item.contacts_type = "nodefy"
        item.displayType = getString(R.string.account_contact_channel_nodefy)
        item.isDefault = false
        item.checked = false
        contactChannelList.add(item)

        relationsList.forEach {
            val channelInfo: ContactChannelInfo = ContactChannelInfo()
            channelInfo.contacts_id = it.user_id
            channelInfo.contacts_type = it.account_type
            channelInfo.isDefault = it.is_main!!
            channelInfo.checked = it.is_main!!
            if (it.account_type!!.lowercase() == Contants.SIP_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_sip)
            }else if (it.account_type!!.lowercase() == Contants.XMPP_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_xmpp)
            } else if (it.account_type!!.lowercase() == Contants.SKYPE_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_skype)
            } else if (it.account_type!!.lowercase() == Contants.SLACK_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_slack)
            } else if (it.account_type!!.lowercase() == Contants.TELEGRAM_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_telegram)
            }else if (it.account_type!!.lowercase() == Contants.WHATSAPP_TYPE) {
                channelInfo.displayType = getString(R.string.account_contact_channel_whatsapp)
            }

            if (it.is_main!!) {
                isHasDefault = true
                defaultRelation = it
            }

            contactChannelList.add(channelInfo)
        }

        if (!isHasDefault) {
            contactChannelList.forEach {
                if (it.displayType == getString(R.string.account_contact_channel_nodefy)) {
                    it.isDefault = true
                    it.checked = true
                }
            }
        }

        contactChannelList.forEach {
            if (it.isDefault) {
                defaultChanelType = it.contacts_type!!
                views.tvDefaultChannel.text = it.displayType
            }
        }

        if (defaultChanelType.lowercase() == Contants.XMPP_TYPE) {
            views.llCall.isEnabled = false
            views.tvCall.setTextColor(resources.getColor(R.color.text_color_black1, null))
            views.callIcon.setImageResource(R.drawable.ic_call_gray)
        } else {
            views.llCall.isEnabled = true
            views.tvCall.setTextColor(resources.getColor(R.color.text_color_black, null))
            views.callIcon.setImageResource(R.drawable.ic_dialer_call)
        }

        if (defaultChanelType.lowercase() == Contants.SIP_TYPE) {
            setDefaultNumber()
        }

    }

    override fun onRefreshRelations() {
        Timber.e("call the refresh relation")
        getRelations(false)
    }

    override fun onDefaultChannel(item: ContactChannelInfo, isNodefy: Boolean) {
        val relationInfo: DefaultContactRelationInfo = DefaultContactRelationInfo()

        val childrenInfo: ChildrenInfo = ChildrenInfo()
        childrenInfo.user_id = item.contacts_id
        childrenInfo.account_type = item.contacts_type
        childrenInfo.is_main = !isNodefy

        relationInfo.main_children = childrenInfo
        relationInfo.primary_user_id = targetContact.contacts_id

        showLoading()
        HttpClient.setContactDefaultChannel(this, relationInfo)
        updateRelations(item, isNodefy)
    }

    private fun updateRelations(item: ContactChannelInfo, isNodefy: Boolean) {
        val info: UpdateContactRelationInfo = UpdateContactRelationInfo()
        info.primary_user_id = targetContact.contacts_id
        relationsList.forEach {
            val childrenInfo = ChildrenUserInfo()
            childrenInfo.user_id = it.user_id
            childrenInfo.account_type = it.account_type
            if (isNodefy) {
                childrenInfo.is_main = false
            } else {
                childrenInfo.is_main = it.account_type == item.contacts_type
            }

            info.children_users.add(childrenInfo)
        }
        HttpClient.updateContactRelation(this, info)
    }

    @Subscribe
    fun onAssociateContactEvent(event: UpdateContactRelationResponseEvent) {
        if (event.isSuccess) {
            getRelations(false)
        } else {
            Toast.makeText(mContext, event.model!!.error, Toast.LENGTH_SHORT).show()
        }
    }


    @Subscribe
    fun onChannelEvent(event: DefaultContactRelationResponseEvent) {
        hideLoading()
        if (event.isSuccess) {
            //getRelations(false)
        }
    }

    private fun contactMessage() {
        when (defaultChanelType) {
            Contants.NODEFY_TYPE -> {
                viewModel.onSubmitInvitees(targetContact.contacts_id!!)
            }
            Contants.SIP_TYPE    -> {
                if (core.accountList.isEmpty()) {
                    return
                }
                sendSipMessage()
            }
            Contants.XMPP_TYPE   -> {
                xmppMessage()
            }
            Contants.SKYPE_TYPE   -> {

            }
            Contants.SLACK_TYPE   -> {

            }
            Contants.TELEGRAM_TYPE   -> {

            }
            Contants.WHATSAPP_TYPE   -> {

            }
        }
    }

    private fun contactCall() {
        when (defaultChanelType) {
            Contants.NODEFY_TYPE -> {

            }
            Contants.SIP_TYPE    -> {
                if (core.accountList.isEmpty()) {
                    return
                }
                sipCall()
            }
            Contants.XMPP_TYPE   -> {

            }
            Contants.SKYPE_TYPE   -> {

            }
            Contants.SLACK_TYPE   -> {

            }
            Contants.TELEGRAM_TYPE   -> {

            }
            Contants.WHATSAPP_TYPE   -> {

            }
        }
    }

    private fun xmppMessage() {
        var item = ContactRelationInfo()
        var xmppItem: XmppContact = XmppContact()
        relationsList.forEach {
            if (it.account_type!!.lowercase() == Contants.XMPP_TYPE) {
                item = it
                return@forEach
            }
        }
        xmppContactList.forEach {
            if (item.user_id == it.jid.toString()) {
                xmppItem = it
            }
        }

        val intent = Intent(this, PeopleChatMessageActivity::class.java)
        intent.putExtra("contact", xmppItem)
        intent.putExtra("index", 1)
        startActivity(intent)
    }

    private fun sipCall() {
        if (sipAccountList.size > 0) {
            val filterList = checkDefaultDomain()
            if (filterList!!.isEmpty()) {
                return
            }
            if (filterList.size == 1) {
                setDefaultAccount(filterList[0])
                directlyToCall()
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, filterList, true, false, true, false)
                callPopupWindow!!.showOnAnchor(views.llCall, RelativePopupWindow.VerticalPosition.ABOVE,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, true)
            }
        }
    }

    private fun directlyToCall() {
        var proxy = ""
        if (TextUtils.isEmpty(selectedAccount.extension.outProxy)) {
            proxy = selectedAccount.domain!!
        } else {
            proxy = selectedAccount.extension.outProxy!!
        }

        val intent = Intent(this, DialerCallActivity::class.java)
        intent.putExtra("index", 1)
        intent.putExtra("remote_user", defaultSipNumber)
        intent.putExtra("local_user", selectedAccount.username)
        intent.putExtra("domain", proxy)
        intent.putExtra("proxy", proxy)
        startActivity(intent)
    }

    private fun setDefaultNumber() {

        var item = ContactRelationInfo()
        relationsList.forEach {
            if (it.account_type!!.lowercase() == Contants.SIP_TYPE) {
                item = it
                return@forEach
            }
        }
        sipContactList.forEach {
            if (item.user_id == it.id) {
                sipContactInfo = it
            }
        }

        sipContactInfo.phone!!.forEach {
            if (it.isDefault!!) {
                defaultSipNumber = it.number
                defaultNumber = it.number
            }
        }
        sipContactInfo.online_phone!!.forEach {
            if (it.isDefault!!) {
                //views.tvDefaultNumber.text = it.number
                defaultSipNumber = it.number!!.split("@")[0]
                defaultNumber = it.number
            }
        }
    }

    private fun getRegisterSIPUser() {
        sipAccountList.clear()
        dialerSession.accountListInfo!!.forEach {
            if (it.type_value!!.lowercase(Locale.ROOT) == "sip" && it.enabled && it.extension.isConnected) {
                sipAccountList.add(it)
            }
        }
    }

    private fun sendSipMessage(){
        if (sipAccountList.size > 0) {
            val filterList = checkDefaultDomain()
            if (filterList!!.isEmpty()) {
                return
            }
            if (filterList.size == 1) {
                setDefaultAccount(filterList[0])
                directlyToMessage(filterList[0])
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, filterList, true, false, true, true)
                callPopupWindow.showOnAnchor(views.llMessage, RelativePopupWindow.VerticalPosition.ABOVE,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, true)
            }
        }
    }

    private fun directlyToMessage(item: DialerAccountInfo) {
        val intent = Intent(this, SendMessageActivity::class.java)
        intent.putExtra("remote_number", defaultSipNumber)
        intent.putExtra("selected_account", item)
        startActivity(intent)
    }

    private fun setDefaultAccount(item: DialerAccountInfo) {
        val mAccount = item
        selectedAccount = item
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == mAccount.username && domain == mAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
    }

    private fun checkDefaultDomain(): ArrayList<DialerAccountInfo>? {
        var filterAccountList:ArrayList<DialerAccountInfo> = ArrayList()

        if (!defaultNumber!!.contains("@")) {
            filterAccountList = sipAccountList
        } else {
            filterAccountList.clear()
            val defaultDomain = defaultNumber!!.split("@")[1]
            sipAccountList.forEach {
                if (it.domain!!.trim().trimEnd() == defaultDomain.trimEnd().trim()) {
                    filterAccountList.add(it)
                }
            }
        }
        return filterAccountList
    }

    @Subscribe
    fun onSetDefaultCallEvent(event: SetDefaultCallAccountEvent){
        selectedAccount = event.item
        Timber.e("selected account--${selectedAccount.username}")
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == selectedAccount.username && domain == selectedAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
        directlyToCall()
    }

    @Subscribe
    fun onSetDefaultMessageEvent(event: SetDefaultMessageAccountEvent){
        selectedAccount = event.item
        Timber.e("selected message account--${selectedAccount.username}")
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == selectedAccount.username && domain == selectedAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
        directlyToMessage(event.item)
    }

    private fun renderCreationLoading() {
        updateWaitingView(WaitingViewData(getString(R.string.creating_direct_room)))
    }

    private fun renderCreationFailure(error: Throwable) {
        hideWaitingView()
        when (error) {
            is CreateRoomFailure.CreatedWithTimeout           -> {

            }
            is CreateRoomFailure.CreatedWithFederationFailure -> {
                MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.create_room_federation_error, error.matrixError.message))
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok) { _, _ -> finish() }
                        .show()
            }
            else                                              -> {
                val message = if (error is Failure.ServerError && error.httpCode == HttpURLConnection.HTTP_INTERNAL_ERROR /*500*/) {
                    // This error happen if the invited userId does not exist.
                    getString(R.string.create_room_dm_failure)
                } else {
                    errorFormatter.toHumanReadable(error)
                }
                MaterialAlertDialogBuilder(this)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }
        }
    }

    private fun renderCreationSuccess(roomId: String) {
        navigator.openRoom(
                context = this,
                roomId = roomId,
                trigger = ViewRoom.Trigger.MessageUser
        )
        finish()
    }

    /**
     * Displays a progress indicator with a message to the user.
     * Blocks user interactions.
     */
    fun updateWaitingView(data: WaitingViewData?) {
        data?.let {
            views.waitingView.waitingStatusText.text = data.message

            if (data.progress != null && data.progressTotal != null) {
                views.waitingView.waitingHorizontalProgress.isIndeterminate = false
                views.waitingView.waitingHorizontalProgress.progress = data.progress
                views.waitingView.waitingHorizontalProgress.max = data.progressTotal
                views.waitingView.waitingHorizontalProgress.isVisible = true
                views.waitingView.waitingCircularProgress.isVisible = false
            } else if (data.isIndeterminate) {
                views.waitingView.waitingHorizontalProgress.isIndeterminate = true
                views.waitingView.waitingHorizontalProgress.isVisible = true
                views.waitingView.waitingCircularProgress.isVisible = false
            } else {
                views.waitingView.waitingHorizontalProgress.isVisible = false
                views.waitingView.waitingCircularProgress.isVisible = true
            }

            showWaitingView()
        } ?: run {
            hideWaitingView()
        }
    }

    override fun showWaitingView(text: String?) {
        hideKeyboard()
        views.waitingView.waitingStatusText.isGone = views.waitingView.waitingStatusText.text.isNullOrBlank()
        super.showWaitingView(text)
    }

    override fun hideWaitingView() {
        views.waitingView.waitingStatusText.text = null
        views.waitingView.waitingStatusText.isGone = true
        views.waitingView.waitingHorizontalProgress.progress = 0
        views.waitingView.waitingHorizontalProgress.isVisible = false
        super.hideWaitingView()
    }

    private fun showLoading() {
        if (loading == null || !loading!!.isShowing) {
            loading = KProgressHUD.create(this@AccountContactDetailActivity)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setDimAmount(0.5f)
            loading!!.show()
        }
    }

    private fun hideLoading() {
        if (loading != null && loading!!.isShowing) {
            loading!!.dismiss()
            loading = null
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mBus.unregister(this)
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mBus.unregister(this)
        } catch (e: Exception) {
        }
    }


}
