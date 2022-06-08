package com.goldenowl.ecommerce.ui.global.shop

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class ShopCategoryAdapter(
    mContext: Context,
    listCategory: List<String>,
) :
    ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, listCategory) {
    private var visibilityList: List<Boolean> = listCategory.map { true }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (!visibilityList[position])
            view.visibility = View.GONE
        return view
    }

    fun setVisibilityList(vlist: List<Boolean>) {
        visibilityList = vlist
        notifyDataSetChanged()
    }

//    }
//    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
//    }
}
