package org.akshayapatra.tapf_hyd

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import com.google.firebase.database.*
import org.json.JSONObject

class CompaniesListActivity : AppCompatActivity() {

    private var lvCompanyList: ListView? = null
    var alCompanies: ArrayList<JSONObject>? = null
    var adapter: CompanyArrayAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies_list)

        lvCompanyList = findViewById(R.id.lv_company_list)
        alCompanies = ArrayList()
        adapter = CompanyArrayAdapter(this, alCompanies!!)
        lvCompanyList!!.adapter = adapter

        val database = FirebaseDatabase.getInstance()
        val statusRef = database.getReference("status")
        database.getReference("companies")
                .child("Telangana")
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                        val company = JSONObject()
                        p0?.children?.forEach { child ->
                            company.put(child.key, child.value.toString())
                        }
                        statusRef.child(p0?.child("REGISTERED_STATE")?.value.toString())
                                .child(p0?.child("CIN")?.value.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {
                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                        company.put("STATUS", p0?.getValue(String::class.java))
                                        alCompanies?.add(company)
                                        adapter?.notifyDataSetChanged()
                                    }
                                })
                    }

                    override fun onChildRemoved(p0: DataSnapshot?) {
                    }

                })
    }
}
