package org.akshayapatra.tapf_hyd

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import com.google.firebase.database.*
import org.json.JSONObject

class CompaniesListActivity : AppCompatActivity() {

    private var lvCompanyList: ListView? = null
    var alCompanies: ArrayList<JSONObject>? = null
    var alFilteredCompanies: ArrayList<JSONObject>? = null
    var adapter: CompanyArrayAdapter? = null

    private var etSearch: EditText? = null
    var databaseReference: DatabaseReference? = null
    var childEventListener: ChildEventListener? = null

    val fieldsToShow = arrayOf("CIN", "COMPANY_NAME", "REGISTERED_OFFICE_ADDRESS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies_list)

        etSearch = findViewById(R.id.et_search)

        lvCompanyList = findViewById(R.id.lv_company_list)
        alCompanies = ArrayList()
        alFilteredCompanies = ArrayList()
        adapter = CompanyArrayAdapter(this, alFilteredCompanies!!)
        lvCompanyList!!.adapter = adapter

        lvCompanyList?.emptyView = findViewById(R.id.loading)

        initDBOperations()

        lvCompanyList?.setOnItemClickListener { adapterView, view, i, l ->
            val selectedItem: JSONObject = alCompanies!![i]
            val intent = Intent(this, CompanyDetailsActivity::class.java)
            intent.putExtra("CIN", selectedItem.getString("CIN"))
            intent.putExtra("state", getIntent().getStringExtra("state"))
            startActivityForResult(intent, 0)
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                filterCompanies()
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
                    if (fieldsToShow.contains(child.key))
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
//                                adapter?.notifyDataSetChanged()
                                filterCompanies()
                            }
                        })
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        }

        val group = intent.getIntExtra("group", 0)

        if (group == 0) {
            databaseReference
                    ?.orderByChild("COMPANY_NAME")
                    ?.startAt("0")
                    ?.endAt("9" + "\uf8ff")
                    ?.addChildEventListener(childEventListener)
        } else {
            var c = ('A' + group - 1).toString()
            databaseReference
                    ?.orderByChild("COMPANY_NAME")
                    ?.startAt(c)
                    ?.endAt(c + "\uf8ff")
                    ?.addChildEventListener(childEventListener)
        }
    }

    private fun filterCompanies() {
        alFilteredCompanies?.clear()
        val query = etSearch?.text.toString()
        for (company in alCompanies!!) {
            if (company["COMPANY_NAME"].toString().contains(query, true)) {
                alFilteredCompanies?.add(company)
            }
        }
        adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.recreate()
        super.onActivityResult(requestCode, resultCode, data)
    }
}
