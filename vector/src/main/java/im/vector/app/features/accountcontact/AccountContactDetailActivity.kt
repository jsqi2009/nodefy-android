package im.vector.app.features.accountcontact

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.features.accountcontact.util.AvatarRendererUtil
import im.vector.app.features.accountcontact.widget.AssociateContactBottomDialog
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetContactRelationResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.XmppContact
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import javax.inject.Inject

private const val nodefyType  = "nodefy"

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener, AssociateContactBottomDialog.InteractionListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    @Inject lateinit var sessionHolder: ActiveSessionHolder

    var session: Session? = null
    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var sipContactList:ArrayList<DialerContactInfo> = ArrayList()
    private var xmppContactList: ArrayList<XmppContact> = ArrayList()
    private var targetContact: AccountContactInfo = AccountContactInfo()
    private var relationsList: ArrayList<ContactRelationInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
        getRelations()
    }

    override fun onResume() {
        super.onResume()
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
        }

        views.tvUsername.text = targetContact.displayname
        AvatarRendererUtil.render(mContext, targetContact, views.ivAvatar)

        views.rlBack.setOnClickListener(this)
        views.sipAssociate.setOnClickListener(this)
        views.xmppAssociate.setOnClickListener(this)
        views.skypeAssociate.setOnClickListener(this)
        views.slackAssociate.setOnClickListener(this)
        views.telegramAssociate.setOnClickListener(this)
        views.whatsappAssociate.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_back -> {
                finish()
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
            else         -> {}
        }
    }

    private fun associateContact(accountType: String) {

        AssociateContactBottomDialog(this, mBus, accountType, session!!.myUserId, mConnectionList, dialerSession,
                contactList, sipContactList, xmppContactList, targetContact, relationsList,this).show(supportFragmentManager, "associate")
    }

    private fun getRelations() {
        showLoadingDialog()
        HttpClient.getContactRelations(this, targetContact.contacts_id!!)
    }

    @Subscribe
    fun onRelationsEvent(event: GetContactRelationResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            relationsList.clear()
            relationsList = event.model!!.children_users
            Timber.e("relations list----->${Gson().toJson(relationsList)}")

            renderAssociateInfo()
        }
    }

    private fun renderAssociateInfo() {
        relationsList.forEach { item ->
            if (item.account_type!!.lowercase() == "sip") {
                sipContactList.forEach {
                    if (it.id == item.user_id) {
                        views.sipAssociate.text = it.first_name
                        return@forEach
                    }
                }
            }
            if (item.account_type!!.lowercase() == "xmpp") {
                xmppContactList.forEach {
                    if (it.jid.toString() == item.user_id) {
                        views.xmppAssociate.text = it.jid
                        return@forEach
                    }
                }
            }

            contactList.forEach {
                if (item.account_type!!.lowercase() == "slack" && it.contacts_id == item.user_id) {
                    views.slackAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == "skype" && it.contacts_id == item.user_id) {
                    views.skypeAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == "telegram" && it.contacts_id == item.user_id) {
                    views.telegramAssociate.text = it.displayname
                }
                if (item.account_type!!.lowercase() == "whatsapp" && it.contacts_id == item.user_id) {
                    views.whatsappAssociate.text = it.displayname
                }
            }
        }
    }

    override fun onRefreshRelations() {
        Timber.e("call the refresh relation")
        getRelations()
    }
}
