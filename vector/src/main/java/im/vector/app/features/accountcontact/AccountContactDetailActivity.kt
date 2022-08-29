package im.vector.app.features.accountcontact

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.mylhyl.circledialog.CircleDialog
import com.mylhyl.circledialog.view.listener.OnButtonClickListener
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.features.accountcontact.util.AvatarRendererUtil
import im.vector.app.features.accountcontact.widget.AssociateContactBottomDialog
import im.vector.app.features.accountcontact.widget.SetDefaultChannelDialog
import im.vector.app.kelare.content.Contants
import im.vector.app.kelare.message.widget.CreateXMPPGroupDialog
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.DefaultContactRelationResponseEvent
import im.vector.app.kelare.network.event.DeleteContactRelationResponseEvent
import im.vector.app.kelare.network.event.GetContactRelationResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ChildrenInfo
import im.vector.app.kelare.network.models.ChildrenUserInfo
import im.vector.app.kelare.network.models.ContactChannelInfo
import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DefaultContactRelationInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.UpdateContactRelationInfo
import im.vector.app.kelare.network.models.XmppContact
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import javax.inject.Inject

private const val nodefyType  = "nodefy"

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener,
        AssociateContactBottomDialog.InteractionListener, SetDefaultChannelDialog.ChannelListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    @Inject lateinit var sessionHolder: ActiveSessionHolder

    private var session: Session? = null
    private var loading: KProgressHUD? = null
    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var sipContactList:ArrayList<DialerContactInfo> = ArrayList()
    private var xmppContactList: ArrayList<XmppContact> = ArrayList()
    private var targetContact: AccountContactInfo = AccountContactInfo()
    private var relationsList: ArrayList<ContactRelationInfo> = ArrayList()
    private var defaultRelation: ContactRelationInfo? = ContactRelationInfo()
    private var contactChannelList: ArrayList<ContactChannelInfo> = ArrayList()

    private var isEdit = false
    private var defaultChanelType: String = "nodefy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
        getRelations(true)
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
                views.tvDefaultNumber.text = it.displayType
            }
        }

        if (defaultChanelType.lowercase() == Contants.XMPP_TYPE) {
            views.llCall.isEnabled = false
            views.tvCall.setTextColor(resources.getColor(R.color.text_color_black1, null))
        } else {
            views.llCall.isEnabled = true
            views.tvCall.setTextColor(resources.getColor(R.color.text_color_black, null))
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
    }

    @Subscribe
    fun onChannelEvent(event: DefaultContactRelationResponseEvent) {
        hideLoading()
        if (event.isSuccess) {
            getRelations(false)
        }
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


}
