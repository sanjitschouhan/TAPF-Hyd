package org.akshayapatra.tapf_hyd

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ListView
import com.google.firebase.database.*
import org.json.JSONObject

class CompaniesListActivity : AppCompatActivity() {

    private var lvCompanyList: ListView? = null
    var alCompanies: ArrayList<JSONObject>? = null
    var adapter: CompanyArrayAdapter? = null

    private var etSearch: EditText? = null
    var databaseReference: DatabaseReference? = null
    var childEventListener: ChildEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies_list)

        etSearch = findViewById(R.id.et_search)

        lvCompanyList = findViewById(R.id.lv_company_list)
        alCompanies = ArrayList()
        adapter = CompanyArrayAdapter(this, alCompanies!!)
        lvCompanyList!!.adapter = adapter

        initDBOperations()

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val query = etSearch?.text.toString().trim()
                Log.e("Query", query)
                databaseReference?.removeEventListener(childEventListener)
                alCompanies?.clear()
                adapter?.notifyDataSetChanged()
                if (query.length >= 0)
                    databaseReference
                            ?.orderByChild("COMPANY_NAME")
                            ?.startAt(null, query.toUpperCase())
                            ?.endAt(query.toUpperCase()+"\uf8ff")
                            ?.limitToFirst(10)
                            ?.addChildEventListener(childEventListener)
                else
                    databaseReference
                            ?.orderByChild("COMPANY_NAME")
                            ?.limitToFirst(10)
                            ?.addChildEventListener(childEventListener)

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    private fun initDBOperations() {

        val query = etSearch?.text.toString().trim()
        val database = FirebaseDatabase.getInstance()

        val statusRef = database.getReference("status")

        databaseReference = database.getReference("companies")
                .child(intent.getStringExtra("state"))

        databaseReference?.limitToFirst(10)

        childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                val company = JSONObject()
                for (child in p0?.children!!) {
                    company.put(child.key, child.value.toString())
                }

                statusRef.child(p0.child("REGISTERED_STATE")?.value.toString())
                        .child(p0.child("CIN")?.value.toString())
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

        }

        databaseReference
                ?.orderByChild("COMPANY_NAME")
                ?.limitToFirst(50)
                ?.addChildEventListener(childEventListener)
    }
}
