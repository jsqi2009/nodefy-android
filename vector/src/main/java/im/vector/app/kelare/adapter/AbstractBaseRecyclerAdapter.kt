package im.vector.app.kelare.adapter

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractBaseRecyclerAdapter<T>(var context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var dataList: MutableList<T>? = null

    override fun getItemCount(): Int {
        return if (dataList == null) 0 else dataList!!.size
    }

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

    fun addData(data: T?) {
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

    fun getDataList(): MutableList<T>? {
        return dataList
    }

    fun updateDataByIndex(info: T, index: Int) {
        dataList!!.removeAt(index)
        dataList!!.add(index, info)
        notifyItemChanged(index)
    }
}
