package org.akshayapatra.tapf_hyd

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject

class CompanyDetailsActivity : AppCompatActivity() {

    val company: JSONObject = JSONObject()
    var lvCompanyDetails: ListView? = null
    var btnHold: Button? = null
    val order = "CIN,COMPANY_NAME,COMPANY_CLASS,COMPANY_CATEGORY,COMPANY_SUBCAT,COMPANY_STATUS,DATE_OF_REGISTRATION,REGISTERED_STATE,Authorized Capital  (Rs), PAIDUP_CAPITAL (RS),PRINCIPAL_BUSINESS_ACTIVITY_CODE,REGISTERED_OFFICE_ADDRESS,EMAIL_ID,LATEST ANNUAL REPORT FILING FY END DATE,LATEST BALANCE SHEET FILING FY END DATE".split(",")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_details)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        lvCompanyDetails = findViewById(R.id.lv_company_details)

        FirebaseDatabase.getInstance().getReference("/companies")
                .child(intent.getStringExtra("state"))
                .child(intent.getStringExtra("CIN"))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        for (data in p0?.children!!) {
                            company.put(data.key, data.value.toString())
                        }
                        FirebaseDatabase.getInstance().getReference("/status")
                                .child(p0.child("REGISTERED_STATE")?.value.toString())
                                .child(p0.child("CIN")?.value.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError?) {
                                    }

                                    override fun onDataChange(p0: DataSnapshot?) {
                                        company.put("STATUS", p0?.getValue(String::class.java))
                                        updateUI()
                                    }
                                })
                    }

                })
    }

    private fun updateUI() {
        val tvName: TextView = findViewById(R.id.tv_name)
        tvName.text = company["COMPANY_NAME"].toString()

        val array = ArrayList<String>()
        for (key in order) {
            if (company.has(key)) {
                array.add(key.replace("_", " ") + ":\n" + company[key])
            }
        }
        for (key in company.keys()) {
            if (!order.contains(key))
                if (!key.equals("STATUS", true))
                    array.add(key.replace("_", " ") + ":\n" + company[key])
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array)
        lvCompanyDetails?.adapter = adapter

        btnHold = findViewById(R.id.bt_hold)

        if (!company.has("STATUS")) {
            btnHold?.text = "Status: Open (Click to Hold)"
            btnHold?.isEnabled = true
        } else {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(company["STATUS"].toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot?) {
                            btnHold?.text = "Held by " + p0?.value
                            btnHold?.isEnabled = false
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (p0?.key.equals(uid)) {
                                btnHold?.isEnabled = true
                                btnHold?.text = btnHold?.text as String + " (Click to Release)"
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {
                        }

                    })
        }

    }

    fun hold(view: View) {
        if (company.has("STATUS")) {
            val currentStatus = btnHold?.text
            btnHold?.text = "Loading..."
            btnHold?.isEnabled = false
            FirebaseDatabase.getInstance().getReference("/status")
                    .child(intent.getStringExtra("state"))
                    .child(intent.getStringExtra("CIN"))
                    .removeValue()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            btnHold?.text = "Status: Open (Click to Hold)"
                            company.remove("STATUS")
                        } else {
                            Toast.makeText(this, "Some Error Occured", Toast.LENGTH_SHORT).show()
                            btnHold?.text = currentStatus
                        }
                        btnHold?.isEnabled = true
                    }
        } else {
            btnHold?.text = "Loading..."
            btnHold?.isEnabled = false
            FirebaseDatabase.getInstance().getReference("/status")
                    .child(intent.getStringExtra("state"))
                    .child(intent.getStringExtra("CIN"))
                    .setValue(FirebaseAuth.getInstance().currentUser?.uid)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            company.put("STATUS", FirebaseAuth.getInstance().currentUser?.uid)
                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(FirebaseAuth.getInstance().currentUser?.uid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(p0: DataSnapshot?) {
                                            btnHold?.text = "Held by " + p0?.value
                                            btnHold?.isEnabled = false
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                            if (p0?.key.equals(uid)) {
                                                btnHold?.isEnabled = true
                                                btnHold?.text = btnHold?.text as String + " (Click to Release)"
                                            }
                                        }

                                        override fun onCancelled(p0: DatabaseError?) {
                                        }

                                    })
                        } else {
                            Toast.makeText(this, "Some Error Occured", Toast.LENGTH_SHORT).show()
                            btnHold?.text = "Status: Open (Click to Hold)"
                            btnHold?.isEnabled = true
                        }
                    }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
