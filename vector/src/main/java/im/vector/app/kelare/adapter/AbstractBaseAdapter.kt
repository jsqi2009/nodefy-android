package im.vector.app.kelare.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import java.util.ArrayList

/**
 * @param <T>
</T> */
abstract class AbstractBaseAdapter<T>(var context: Activity) : BaseAdapter() {

    private var dataList: MutableList<T>? = null

    override fun getCount(): Int {
        return if (dataList == null) 0 else dataList!!.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return creatView(position, convertView, parent)
    }

    abstract fun creatView(position: Int, convertView: View?,
                           parent: ViewGroup): View?

    fun addDataList(dataList: List<T>?) {
        if (this.dataList == null) {
            this.dataList = ArrayList()
        }
        if (dataList != null) {
            this.dataList!!.addAll(dataList)
        }
    }

    fun addDataList(data: T?) {
        if (this.dataList == null) {
            this.dataList = ArrayList()
        }
        if (data != null) {
            this.dataList!!.add(data)
        }
    }

    fun clearDataList() {
        if (dataList != null) {
            dataList!!.clear()
        }
    }

    fun getDataList(): List<T>? {
        return dataList
    }

    /*fun <T : View> View.findViewOften(viewId: Int): T {
        var viewHolder: SparseArray<View> = tag as? SparseArray<View> ?: SparseArray()
        tag = viewHolder
        var childView: View? = viewHolder.get(viewId)
        if (null == childView) {
            childView = findViewById(viewId)
            viewHolder.put(viewId, childView)
        }
        return childView as T
    }*/
}
