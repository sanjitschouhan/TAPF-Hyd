package org.akshayapatra.tapf_hyd

/**
 * Created by sanjit on 9/3/18.
 * Project: TAPF-Hyd
 */

class CompanyGroupItem(var rangeStart: Long, var rangeEnd: Long, var countCompany: Long) {

    override fun toString(): String {
        val format = "%15d\t- %-15d (%3d)"
        return format.format(rangeStart, rangeEnd, countCompany)
    }


}