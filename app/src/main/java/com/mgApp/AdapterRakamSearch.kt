package com.mgApp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class AdapterRakamSearch(private  val rakamList: ArrayList<DataRakamSearch>): RecyclerView.Adapter<AdapterRakamSearch.RakamSearchViewHolder>() {
    inner class RakamSearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rakamDetails: TextView = itemView.findViewById(R.id.SearchDetails)
        val rakamConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.SearchConstrainLayout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RakamSearchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_items, parent, false)
        return RakamSearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RakamSearchViewHolder, position: Int) {
        val rakamSearchData = rakamList[position]
        val rakamSearchText = rakamSearchData.rakamType + " " + rakamSearchData.rakamWeight + " GMS"
        holder.rakamDetails.text = rakamSearchText

        holder.rakamConstraintLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("f_name", rakamSearchData.fName)
            bundle.putString("m_name", rakamSearchData.mName)
            bundle.putString("l_name", rakamSearchData.lName)
            bundle.putString("city", rakamSearchData.city)
            bundle.putString("mobile_number", rakamSearchData.mobileNumber)
            bundle.putString("aadhar_number", rakamSearchData.aadharNumber)
            bundle.putString("cid", rakamSearchData.cid)
            bundle.putString("rakam_type", rakamSearchData.rakamType)
            bundle.putString("rakam_weight", rakamSearchData.rakamWeight)
            bundle.putString("metal_type", rakamSearchData.metalType)
            bundle.putString("net_weight_gms", rakamSearchData.netWeight)

            val nextFragment = FragmentTransactionSearch()
            nextFragment.arguments = bundle
            val appCompactActivity = it.context as AppCompatActivity
            appCompactActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, nextFragment).addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return rakamList.size
    }
}