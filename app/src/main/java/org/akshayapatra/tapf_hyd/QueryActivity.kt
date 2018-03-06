package org.akshayapatra.tapf_hyd

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class QueryActivity : AppCompatActivity() {

    var states: ArrayList<String>? = null
    var statesAdapter: ArrayAdapter<String>? = null
    var statesSpinner: Spinner? = null
    var statusSpinner: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        val companies = FirebaseDatabase.getInstance().getReference("/states")
        states = ArrayList()

        statesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, states);

        statesSpinner = findViewById(R.id.states)
        statusSpinner = findViewById(R.id.status)
        statesSpinner!!.adapter = statesAdapter

        companies.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                states!!.remove(p1)
                states!!.add(p0!!.key)
                statesAdapter!!.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                states!!.remove(p0!!.key)
                statesAdapter!!.notifyDataSetChanged()
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                states!!.add(p0!!.key)
                statesAdapter!!.notifyDataSetChanged()
            }

        })
    }

    fun showCompanies(view: View) {
        val state = statesSpinner!!.selectedItem.toString()
        val status = statusSpinner!!.selectedItem.toString()
        val intent = Intent(this, CompaniesListActivity::class.java)
        intent.putExtra("state", state)
        intent.putExtra("status", status)
        startActivity(intent)
    }
}
