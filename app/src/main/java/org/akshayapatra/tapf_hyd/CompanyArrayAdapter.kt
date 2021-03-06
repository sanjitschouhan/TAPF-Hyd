package org.akshayapatra.tapf_hyd

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import org.json.JSONObject

/**
 * Created by sanjit on 6/3/18.
 * Project: TAPF-Hyd
 */

class CompanyArrayAdapter(context: Context, objects: List<JSONObject>) : ArrayAdapter<JSONObject>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var newView: View? = convertView
        if (newView == null) {
            newView = LayoutInflater.from(context).inflate(R.layout.card_company_item, null)
        }
        val company: JSONObject? = getItem(position)
        val companyNameView: TextView? = newView?.findViewById(R.id.name)
        val companyAddressView: TextView? = newView?.findViewById(R.id.address)
        val indicator: View? = newView?.findViewById(R.id.indicator)

        companyNameView?.text = company?.getString("COMPANY_NAME")
        companyAddressView?.text = company?.getString("REGISTERED_OFFICE_ADDRESS")

        if (company!!.has("STATUS")) {
            indicator?.setBackgroundColor(Color.RED)
        } else {
            indicator?.setBackgroundColor(Color.GREEN)
        }

        return newView!!
    }
}
