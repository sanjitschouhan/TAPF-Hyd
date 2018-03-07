package org.akshayapatra.tapf_hyd

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.GridView

class CompanyGroupActivity : AppCompatActivity() {

    var lvGroups: GridView? = null
    var alGroups: ArrayList<String>? = null
    var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_group)
        lvGroups = findViewById(R.id.lv_groups)
        alGroups = ArrayList()
        alGroups?.add("0-9")
        var c = 'A'
        while (c <= 'Z') {
            alGroups?.add(c.toString())
            c++
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, alGroups)
        lvGroups?.adapter = adapter

        lvGroups?.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, CompaniesListActivity::class.java)
            intent.putExtras(getIntent().extras)
            intent.putExtra("group", i)
            startActivity(intent)
        }
    }
}
