package com.mgApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterTransactionSearch(private val tranList: ArrayList<DataTransactionSearch>): RecyclerView.Adapter<AdapterTransactionSearch.TranSearchViewHolder>() {

    inner class TranSearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tranType: TextView = itemView.findViewById(R.id.tvType)
        val tranAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tranIR: TextView = itemView.findViewById(R.id.tvIR)
        val tranDate: TextView = itemView.findViewById(R.id.tvDate)
        val tranRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranSearchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_items, parent, false)
        return TranSearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TranSearchViewHolder, position: Int) {
        val tranSearchData = tranList[position]
        holder.tranType.text = tranSearchData.tranType
        holder.tranAmount.text = tranSearchData.amount
        holder.tranIR.text = tranSearchData.ir
        holder.tranDate.text = tranSearchData.date
        holder.tranRemarks.text = tranSearchData.remarks
    }

    override fun getItemCount(): Int {
        return tranList.size
    }
}