package org.akshayapatra.tapf_hyd

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.NumberFormat

/**
 * Created by sanjit on 9/3/18.
 * Project: TAPF-Hyd
 */

class CompanyGroupAdapter(context: Context, objects: List<CompanyGroupItem>) : ArrayAdapter<CompanyGroupItem>(context, 0, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var newView = convertView
        if (newView == null) {
            newView = LayoutInflater.from(context)
                    .inflate(R.layout.item_company_group, parent, false)
        }

        newView!!
        val tvRangeStart: TextView = newView.findViewById(R.id.tv_range_start)
        val tvRangeEnd: TextView = newView.findViewById(R.id.tv_range_end)
        val tvCompanyCount: TextView = newView.findViewById(R.id.tv_company_count)

        val group = getItem(position)

        tvRangeStart.text = formatNumber(group.rangeStart)
        tvRangeEnd.text = formatNumber(group.rangeEnd)
        tvCompanyCount.text = formatNumber(group.countCompany)

        return newView
    }

    fun formatNumber(num: Long): String {
//        if (num < 1000) return "" + num
//        val exp = (Math.log(num.toDouble()) / Math.log(1000.0)).toInt()
//        return String.format("%.2f %c",
//                num / Math.pow(1000.0, exp.toDouble()),
//                "kMBTPE"[exp - 1])

        return NumberFormat.getNumberInstance().format(num)

    }
}