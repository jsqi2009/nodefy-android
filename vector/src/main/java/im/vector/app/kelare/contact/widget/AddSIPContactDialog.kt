package im.vector.app.kelare.contact.widget

import RefreshDialerContactEvent
import SelectedNumberEvent
import UpdateContactInfoEvent
import UpdateDefaultNumberEvent
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kaopiz.kprogresshud.KProgressHUD
import com.squareup.otto.Subscribe
import com.yanzhenjie.recyclerview.OnItemMenuClickListener
import com.yanzhenjie.recyclerview.SwipeMenuCreator
import com.yanzhenjie.recyclerview.SwipeMenuItem
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SipExtAdapter
import im.vector.app.kelare.adapter.SipPhoneAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.Contants
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.SaveContactInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateContactInfoResponseEvent
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.PhoneInfo
import im.vector.app.kelare.utils.UIUtils
import timber.log.Timber

/**
 * author : Jason
 * date   : 4/22/22
 * desc   :
 */
class AddSIPContactDialog(val mContext: Context, private val mBus: AndroidBus, val mSession: DialerSession, val contactInfo: DialerContactInfo?) : BottomSheetDialogFragment() {

    private var phoneRecycler: SwipeRecyclerView? = null
    private var extRecycler: SwipeRecyclerView? = null
    private var phoneAdapter: SipPhoneAdapter? = null
    private var extAdapter: SipExtAdapter? = null
    private var phoneList:ArrayList<PhoneInfo> = ArrayList()
    private var extList:ArrayList<PhoneInfo> = ArrayList()
    private var tvDefaultNum: TextView? = null
    private var loadingDialog: KProgressHUD? = null

    private var phoneTopLine: View? =null
    private var phoneBottomLine: View? =null
    private var etFirstName: EditText? =null
    private var etLastName: EditText? =null
    private var etNote: EditText? =null
    private var tvTitle: TextView? = null
    private var tvAddOrSave: TextView? = null

    private var defaultNumber: String? = null
    private var mNumber: String? = null

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) return super.onCreateDialog(savedInstanceState)
        val bottomDialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialog)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_sip_contact, null)
        bottomDialog.setContentView(rootView)
        bottomDialog.setCanceledOnTouchOutside(false)
        //核心代码 解决了无法去除遮罩问题
        //bottomDialog.window!!.setDimAmount(0f)
        //设置宽度
        val params = rootView.layoutParams
        params.height = (0.9 * resources.displayMetrics.heightPixels).toInt()
        rootView.layoutParams = params

        initView(rootView)

        return bottomDialog
    }

    @SuppressLint("SetTextI18n")
    private fun initView(rootView: View) {
        //do something
        tvTitle = rootView.findViewById<View>(R.id.title) as TextView?
        tvAddOrSave = rootView.findViewById<View>(R.id.tv_add_contact) as TextView?

        tvTitle!!.setOnClickListener {
            dismiss()
        }
        tvAddOrSave!!.setOnClickListener {
            saveContact()
        }

        rootView.findViewById<View>(R.id.ll_add_phone).setOnClickListener {
            addPhoneNumber()
        }
        rootView.findViewById<View>(R.id.ll_add_ext).setOnClickListener {
            addWorkExt()
        }
        rootView.findViewById<View>(R.id.ll_set_default).setOnClickListener {
            setDefaultAccount()
        }
        rootView.findViewById<View>(R.id.tv_cancel).setOnClickListener {
            mSession.phoneListInfo = null
            dismiss()
        }

        tvDefaultNum = rootView.findViewById<View>(R.id.tv_set_default_number) as TextView?
        phoneRecycler = rootView.findViewById<View>(R.id.rv_phone) as SwipeRecyclerView?
        extRecycler = rootView.findViewById<View>(R.id.rv_ext) as SwipeRecyclerView?
        phoneTopLine = rootView.findViewById<View>(R.id.phone_top_line)
        phoneBottomLine = rootView.findViewById<View>(R.id.phone_bottom_line)
        etFirstName = rootView.findViewById<View>(R.id.et_first_username) as EditText?
        etLastName = rootView.findViewById<View>(R.id.et_last_username) as EditText?
        etNote = rootView.findViewById<View>(R.id.et_note) as EditText?

        initPhoneAdapter()
        initExtAdapter()
        if (contactInfo != null) {
            tvAddOrSave!!.text = "Save"
            tvTitle!!.text = "Edit Sip Contact"
            renderInfo()
        }
    }

    private fun initPhoneAdapter() {
        phoneRecycler!!.setSwipeMenuCreator(phoneSwipeMenuCreator)
        phoneRecycler!!.setOnItemMenuClickListener(phoneMenuClickListener)

        //phone adapter
        phoneRecycler!!.layoutManager = LinearLayoutManager(context)
        phoneAdapter = SipPhoneAdapter(context as Activity, mSession, mBus, object : RecyclerItemClickListener {
            override fun onRecyclerViewItemClick(view: View, position: Int) { }
        })
        phoneRecycler!!.adapter = phoneAdapter

        phoneAdapter!!.clearDataList()
        phoneAdapter!!.addDataList(phoneList)
        phoneAdapter!!.notifyDataSetChanged()
    }

    private fun initExtAdapter() {
        extRecycler!!.setSwipeMenuCreator(extSwipeMenuCreator)
        extRecycler!!.setOnItemMenuClickListener(extMenuClickListener)

        //ext adapter
        extRecycler!!.layoutManager = LinearLayoutManager(context)
        extAdapter = SipExtAdapter(context as Activity, mBus, object : RecyclerItemClickListener {
            override fun onRecyclerViewItemClick(view: View, position: Int) { }
        })
        extRecycler!!.adapter = extAdapter

        extAdapter!!.clearDataList()
        extAdapter!!.addDataList(extList)
        extAdapter!!.notifyDataSetChanged()
    }

    private fun renderInfo() {
        etFirstName!!.setText(contactInfo!!.first_name)
        etLastName!!.setText(contactInfo.last_name)
        etNote!!.setText(contactInfo.note)

        phoneAdapter!!.addDataList(contactInfo.phone)
        phoneAdapter!!.notifyDataSetChanged()

        extAdapter!!.addDataList(contactInfo.online_phone)
        extAdapter!!.notifyDataSetChanged()

        contactInfo.phone!!.forEach {
            if (it.isDefault!!) {
                tvDefaultNum!!.text = it.number
                defaultNumber = it.number
                mNumber = it.number
            }
        }
        contactInfo.online_phone!!.forEach {
            if (it.isDefault!!) {
                tvDefaultNum!!.text = it.number
                defaultNumber = it.number
                mNumber = it.number
            }
        }
    }


    private fun addPhoneNumber() {
        val size = phoneAdapter!!.getDataList()!!.size + 1

        val finalList: ArrayList<PhoneInfo> = ArrayList()
        phoneAdapter!!.getDataList()!!.forEach {
            finalList.add(it)
        }
        finalList.add(PhoneInfo("", false))
        phoneAdapter!!.clearDataList()
        phoneAdapter!!.addDataList(finalList)

        phoneAdapter!!.notifyDataSetChanged()
//        phoneAdapter!!.notifyItemChanged(size -1) //Fix position disorder problem

    }

    private fun addWorkExt() {
        val size = extAdapter!!.getDataList()!!.size + 1

        val finalList: ArrayList<PhoneInfo> = ArrayList()
        extAdapter!!.getDataList()!!.forEach {
            finalList.add(it)
        }
        finalList.add(PhoneInfo("", false))
        extAdapter!!.clearDataList()
        extAdapter!!.addDataList(finalList)

        extAdapter!!.notifyDataSetChanged()
//        extAdapter!!.notifyItemChanged(size -1) //Fix position disorder problem
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun saveContact() {
        if (!validateData()) {
           return
        }

        val firstName = etFirstName!!.text.toString()
        val lastName = etLastName!!.text.toString()
        val note = etNote!!.text.toString()

        val info:DialerContactInfo = DialerContactInfo()
        info.user_id = Contants.PRIMARY_USER_ID
        info.first_name = firstName
        info.last_name = lastName
        info.note = note
        phoneAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                info.phone!!.add(it)
            }
        }
        extAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                info.online_phone!!.add(it)
            }
        }

        showLoadingDialog()
        if (contactInfo != null) {
            contactInfo.first_name = firstName
            contactInfo.last_name = lastName
            contactInfo.note = note

            updateServerInfo()
        } else {
            HttpClient.saveDialerContact(context!!, info)
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun updateServerInfo() {
        val filterPhone: ArrayList<PhoneInfo> = ArrayList()
        val filterExt: ArrayList<PhoneInfo> = ArrayList()
        phoneAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                filterPhone.add(it)
            }
        }
        extAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                filterExt.add(it)
            }
        }
        contactInfo!!.phone = filterPhone
        contactInfo!!.online_phone = filterExt

        showLoadingDialog()
        HttpClient.updateDialerContact(context!!, contactInfo!!)
    }

    @Subscribe
    fun onSaveEvent(event: SaveContactInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            mBus.post(RefreshDialerContactEvent())
            dismiss()
        } else {
            showToast("Save failure, please try again")
        }
    }

    @Subscribe
    fun onUpdateContactEvent(event: UpdateContactInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            mBus.post(UpdateContactInfoEvent(contactInfo))
            dismiss()
        } else {
            showToast("Save failure, please try again")
        }
    }

    private fun deletePhoneItem(position: Int) {
        val mPhoneList : ArrayList<PhoneInfo> = ArrayList()
        val mList = phoneAdapter!!.getDataList()!!
        for (index in mList!!.indices) {
            if (index != position) {
                mPhoneList.add(mList!![index])
            }
        }

        phoneAdapter!!.clearDataList()
        phoneAdapter!!.addDataList(mPhoneList)
        phoneAdapter!!.notifyDataSetChanged()

        checkDefaultNumber()
    }

    private fun deleteExtItem(position: Int) {
        val mExtList : ArrayList<PhoneInfo> = ArrayList()
        val mList = extAdapter!!.getDataList()!!
        for (index in mList!!.indices) {
            if (index != position) {
                mExtList.add(mList!![index])
            }
        }

        extAdapter!!.clearDataList()
        extAdapter!!.addDataList(mExtList)
        extAdapter!!.notifyDataSetChanged()

        checkDefaultNumber()
    }

    @Subscribe
    fun onUpdateEvent(event:UpdateDefaultNumberEvent) {
        checkDefaultNumber()
    }

    @SuppressLint("SetTextI18n")
    private fun checkDefaultNumber() {
        var isExist = false
        phoneAdapter!!.getDataList()!!.forEach {
            if (mNumber == it.number) {
                isExist = true
                return@forEach
            }
        }
        extAdapter!!.getDataList()!!.forEach {
            if (mNumber == it.number) {
                isExist = true
                return@forEach
            }
        }

        if (!isExist) {
            defaultNumber = ""
            tvDefaultNum!!.text = "Set default number"
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setDefaultAccount() {

        val allNumberList:ArrayList<PhoneInfo> = ArrayList()
        phoneAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                allNumberList.add(it)
            }
        }
        extAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                allNumberList.add(it)
            }
        }

        if (allNumberList.isEmpty()) {
            return
        }

        val setDialog = SetDefaultNumberDialog(activity!!, 1, mBus, allNumberList)
        val dialogWindow: Window = setDialog.window!!
        dialogWindow.decorView.setPadding(0,0,0,0)
        val lp = dialogWindow.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialogWindow.attributes = lp
        dialogWindow.setGravity(Gravity.BOTTOM)
        setDialog.show()
    }

    private fun updateContact() {
        val contactInfo = DialerContactInfo()

        showLoadingDialog()
        HttpClient.updateDialerContact(mContext, contactInfo)
    }

    @Subscribe
    fun onSelectedNumberEvent(event: SelectedNumberEvent) {
        if (event.index == 1) {
            val selectedValue = event.selectedNum
            Timber.e("selectedValue : ${event.selectedNum}")
            var flag = false
            phoneAdapter!!.getDataList()!!.forEach {
                it.isDefault = false
            }
            extAdapter!!.getDataList()!!.forEach {
                it.isDefault = false
            }

            for (phoneInfo in phoneAdapter!!.getDataList()!!) {
                if (phoneInfo.number == event.selectedNum) {
                    phoneInfo.isDefault = true
                    flag = true
                    break
                }
            }
            if (!flag) {
                extAdapter!!.getDataList()!!.forEach {
                    if (it.number == event.selectedNum) {
                        it.isDefault = true
                    }
                }
            }

            defaultNumber = event.selectedNum
            tvDefaultNum!!.text = event.selectedNum
        }
    }

    // create phone menu
    private var phoneSwipeMenuCreator = SwipeMenuCreator { leftMenu, rightMenu, position ->
        val deleteItem = SwipeMenuItem(context)
        deleteItem.text = "delete"
        deleteItem.width  = 180
        deleteItem.height = UIUtils.dip2px(context, 40)  //40 is item height
        deleteItem.setTextColor(resources.getColor(R.color.white, null))
        deleteItem.setBackgroundColor(resources.getColor(R.color.red, null))
        rightMenu.addMenuItem(deleteItem) // add right menu
    }

    //create phone menu listener
    private var phoneMenuClickListener: OnItemMenuClickListener = OnItemMenuClickListener{ menuBridge, position ->
        // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
        menuBridge.closeMenu()
        // 左侧还是右侧菜单：
        val direction = menuBridge.direction
        // 菜单在Item中的Position：
        val menuPosition = menuBridge.position

        Timber.e("menu position: $menuPosition")
        Timber.e("item position: $position")
        Timber.e("item value: ${phoneAdapter!!.getDataList()!![position].number.toString()}")

        deletePhoneItem(position)
    }

    // create ext menu
    private var extSwipeMenuCreator = SwipeMenuCreator { leftMenu, rightMenu, position ->
        val deleteItem = SwipeMenuItem(context)
        deleteItem.text = "delete"
        deleteItem.width  = 180
        deleteItem.height = UIUtils.dip2px(context, 40)  //40 is item height
        deleteItem.setTextColor(resources.getColor(R.color.white, null))
        deleteItem.setBackgroundColor(resources.getColor(R.color.red, null))
        rightMenu.addMenuItem(deleteItem) // add right menu
    }

    //create ext menu listener
    private var extMenuClickListener: OnItemMenuClickListener = OnItemMenuClickListener{ menuBridge, position ->
        // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
        menuBridge.closeMenu()
        // 左侧还是右侧菜单：
        val direction = menuBridge.direction
        // 菜单在Item中的Position：
        val menuPosition = menuBridge.position

        Timber.e("item position: $position")
        Timber.e("item value: ${extAdapter!!.getDataList()!![position].number.toString()}")

        deleteExtItem(position)
    }

    fun showLoadingDialog() {
        if (this.loadingDialog == null || !this.loadingDialog!!.isShowing) {
            loadingDialog = KProgressHUD.create(mContext)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBus.register(this)
    }

    override fun onStart() {
        super.onStart()

        //拿到系统的 bottom_sheet
        val view: FrameLayout = dialog?.findViewById(R.id.design_bottom_sheet)!!
        //获取behavior
        val behavior = BottomSheetBehavior.from(view)
        //设置弹出高度
        behavior.peekHeight = 3000
        //设置展开状态
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun validateData(): Boolean {
        val totalList: ArrayList<PhoneInfo> = ArrayList()
        val filterPhone: ArrayList<PhoneInfo> = ArrayList()
        val filterExt: ArrayList<PhoneInfo> = ArrayList()

        val firstName = etFirstName!!.text.toString()
        val lastName = etLastName!!.text.toString()
        val note = etNote!!.text.toString()

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            showToast("Please input first name and last name")
            return false
        }

        phoneAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
               filterPhone.add(it)
            }
        }
        extAdapter!!.getDataList()!!.forEach {
            if (!TextUtils.isEmpty(it.number)) {
                filterExt.add(it)
            }
        }
        val phoneSize = filterPhone.size
        val extSize = filterExt.size

        if ((phoneSize + extSize) == 0) {
            showToast("Please input number")
            return false
        } else if ((phoneSize + extSize) == 1) {
            phoneAdapter!!.getDataList()!!.forEach {
                if (!TextUtils.isEmpty(it.number)) {
                    it.isDefault = true
                }
            }
            extAdapter!!.getDataList()!!.forEach {
                if (!TextUtils.isEmpty(it.number)) {
                    it.isDefault = true
                }
            }
            return true
        } else if ((phoneSize + extSize) > 1){
            if (TextUtils.isEmpty(defaultNumber)) {
                showToast("Please set default number")
                return false
            } else {
                return true
            }
        }

       return true
    }

    private fun showToast(message:String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
