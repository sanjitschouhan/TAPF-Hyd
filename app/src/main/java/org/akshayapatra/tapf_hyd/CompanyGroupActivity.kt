package org.akshayapatra.tapf_hyd

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.database.*

class CompanyGroupActivity : AppCompatActivity() {

    var lvGroups: ListView? = null
    var alGroups: ArrayList<String>? = null
    var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_group)
        lvGroups = findViewById(R.id.lv_groups)
        alGroups = ArrayList()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, alGroups)
        lvGroups?.adapter = adapter
        lvGroups?.emptyView = findViewById(R.id.loading)

        lvGroups?.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, CompaniesListActivity::class.java)
            intent.putExtras(getIntent().extras)
            var group = alGroups!![i]
            group = group.split("(")[0]
            intent.putExtra("group", group)
            startActivity(intent)
        }

        getRanges()
    }

    val mapRanges = HashMap<Long, Int>()

    private fun getRanges() {
        val companies = FirebaseDatabase
                .getInstance()
                .getReference("/companies")
                .child(intent.getStringExtra("state"))


        val states = FirebaseDatabase
                .getInstance()
                .getReference("/states")
                .child(intent.getStringExtra("state"))

        states.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                val totalCompanies = p0!!.value as Long
                var loaded = 0L

                companies.orderByChild("PAIDUP_CAPITAL (RS)")
                        .addChildEventListener(object : ChildEventListener {
                            override fun onCancelled(p0: DatabaseError?) {
                            }

                            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                            }

                            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                            }

                            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                                loaded++
                                val paidUpCapital: Long = (p0!!.child("PAIDUP_CAPITAL (RS)").value as Number).toLong()

                                if (mapRanges.containsKey(paidUpCapital)) {
                                    mapRanges.put(paidUpCapital, mapRanges.get(paidUpCapital)!!.plus(1))
                                } else {
                                    mapRanges.put(paidUpCapital, 1)
                                }

                                if (loaded % 100 == 0L || loaded >= totalCompanies) {
                                    updateRanges()
                                }
                            }

                            override fun onChildRemoved(p0: DataSnapshot?) {
                            }

                        })

            }

            override fun onCancelled(p0: DatabaseError?) {
            }

        })
    }

    private fun updateRanges() {
        alGroups!!.clear()
        var rangeStart = 0L
        var rangeEnd = 0L
        var count = 0
        val format = "%15d\t- %-15d (%3d)"
        for (key in mapRanges.keys.sorted()) {
            if (count + mapRanges[key]!! > 100) {
//                Log.d("Ranges", rangeStart.toString() + "-" + rangeEnd.toString() + "=" + count)
                alGroups!!.add(format.format(rangeStart, rangeEnd, count))
                rangeStart = key
                count = 0
            }
            rangeEnd = key
            count += mapRanges[key]!!
        }
//        Log.d("Ranges", rangeStart.toString() + "-" + rangeEnd.toString() + "=" + count)
        alGroups!!.add(format.format(rangeStart, rangeEnd, count))
        adapter!!.notifyDataSetChanged()
    }


}
