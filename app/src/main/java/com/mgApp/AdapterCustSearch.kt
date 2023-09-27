package com.mgApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class AdapterCustSearch(private val custList: ArrayList<DataCustSearch>): RecyclerView.Adapter<AdapterCustSearch.CustSearchViewHolder>() {
    inner class CustSearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val custDetails: TextView = itemView.findViewById(R.id.SearchDetails)
        val custConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.SearchConstrainLayout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustSearchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_items, parent, false)
        return CustSearchViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return custList.size
    }

    override fun onBindViewHolder(holder: CustSearchViewHolder, position: Int) {
        val custSearchData = custList[position]
        val custDetailsText: String = custSearchData.fName + " " +
                custSearchData.mName + " " +
                custSearchData.lName + " " +
                custSearchData.city + " (" +
                custSearchData.cid + ")"

        holder.custDetails.text = custDetailsText

        holder.custConstraintLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("f_name", custSearchData.fName)
            bundle.putString("m_name", custSearchData.mName)
            bundle.putString("l_name", custSearchData.lName)
            bundle.putString("city", custSearchData.city)
            bundle.putString("mobile_number", custSearchData.mobileNumber)
            bundle.putString("aadhar_number", custSearchData.aadharNumber)
            bundle.putString("cid", custSearchData.cid)
            val nextFragment = FragmentRakamSearch()
            nextFragment.arguments = bundle
            val appCompactActivity = it.context as AppCompatActivity

            appCompactActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, nextFragment).addToBackStack(null)
                .commit()

        }
    }
}