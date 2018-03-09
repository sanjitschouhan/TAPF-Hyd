package org.akshayapatra.tapf_hyd

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.widget.ListView
import com.google.firebase.database.*
import org.json.JSONObject


class CompaniesListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private var lvCompanyList: ListView? = null
    var alCompanies: ArrayList<JSONObject>? = null
    private var alFilteredCompanies: ArrayList<JSONObject>? = null
    private var adapter: CompanyArrayAdapter? = null

    private var databaseReference: DatabaseReference? = null
    private var childEventListener: ChildEventListener? = null

    val fieldsToShow = arrayOf("CIN", "COMPANY_NAME", "REGISTERED_OFFICE_ADDRESS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companies_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        lvCompanyList = findViewById(R.id.lv_company_list)
        alCompanies = ArrayList()
        alFilteredCompanies = ArrayList()
        adapter = CompanyArrayAdapter(this, (alFilteredCompanies as List<JSONObject>?)!!)
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

    }

    private fun initDBOperations() {

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
                                filterCompanies()
                            }
                        })
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }

        }

        val start = intent.getLongExtra("start", 0).toDouble()
        val end = intent.getLongExtra("end", 0).toDouble()

        databaseReference
                ?.orderByChild("PAIDUP_CAPITAL (RS)")
                ?.startAt(start)
                ?.endAt(end)
                ?.addChildEventListener(childEventListener)
    }

    var query: String = ""

    private fun filterCompanies() {
        alFilteredCompanies?.clear()
        query = query.trim()
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_company_list, menu)

        val searchItem = menu?.findItem(R.id.action_search)

        val searchView: SearchView? = searchItem?.actionView as SearchView
        searchView?.queryHint = "Search Companies"
        searchView?.setOnQueryTextListener(this)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        this.query = query.toString()
        filterCompanies()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        this.query = newText.toString()
        filterCompanies()
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
