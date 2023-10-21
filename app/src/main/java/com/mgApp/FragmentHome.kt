package com.mgApp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentHomeBinding
import java.io.File
import java.io.FileOutputStream

class FragmentHome : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private var custSearchList = ArrayList<DataCustSearch>()
    private lateinit var adapter: AdapterCustSearch
    private val db = Firebase.firestore
    private var irForIntCal: String = ""
    private lateinit var tranListForIntCal: MutableList<MutableList<String>>
    private var finalAmount: Float = 0f
    private var totalAmount: Int = 0
    private var totalJama: Int = 0
    private var activeInterest: Int = 0
    private var silRate: String = "0"
    private var goldRate: String = "0"
    private var totalAmountFlag: Boolean = false
    private lateinit var alertBuilder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        recyclerView = binding.rvShowRakamInLoss
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AdapterCustSearch(custSearchList)
        recyclerView.adapter = adapter
        tranListForIntCal = mutableListOf()
        custSearchList.clear()
        adapter.notifyDataSetChanged()

        alertBuilder = AlertDialog.Builder(activity)

        binding.btnShowRakamInLoss.setOnClickListener {
            custSearchList.clear()
            adapter.notifyDataSetChanged()
            silRate = binding.etSilverRate.text.toString()
            goldRate = binding.etGoldRate.text.toString()
            if (silRate.isEmpty() || goldRate.isEmpty()){
                Toast.makeText(context, "Please Enter a Valid Rate", Toast.LENGTH_SHORT).show()
            }
            else{
                getFinalAmount()
            }
            finalAmount = 0f
            tranListForIntCal.clear()
        }

        binding.btnShowTotal.setOnClickListener {
            totalAmountFlag = true
            totalAmount = 0
            totalJama = 0
            activeInterest = 0
            getFinalAmount()
        }

        binding.btnBackupToFile.setOnClickListener {
            alertBuilder.setTitle("Confirmation")
                .setMessage("Are you sure?")
                .setNegativeButton("No") { _, _ ->
                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Yes") { _, _ ->
                    binding.homeProgressBar.visibility = View.VISIBLE
                    var custString =
                        "CID,FirstName,MiddleName,LastName,City,Mobile,Aadhar,RakamType,RakamName,Weight,Fine,TransactionType,Date,Amount,IR,Remarks\n"

                    //Reading Customer Data Here
                    db.collection("cust").get().addOnSuccessListener { custIDs ->
                        if (!custIDs.isEmpty) {
                            for (customerDoc in custIDs) {
                                val tempCustString =
                                    customerDoc.data["cid"].toString().replace(","," ") + "," + customerDoc.data["f_name"].toString().replace(","," ") + "," + customerDoc.data["m_name"].toString().replace(","," ") + "," + customerDoc.data["l_name"].toString().replace(","," ") + "," + customerDoc.data["city"].toString().replace(","," ") + "," + customerDoc.data["mobile_no"].toString().replace(","," ") + "," + customerDoc.data["aadhar_no"].toString().replace(","," ") + ","
                                //Reading Rakam Data for each Customer Here
                                db.collection("cust").document(customerDoc.id).collection("rakam")
                                    .get().addOnSuccessListener { rakamIds ->
                                        if (!rakamIds.isEmpty) {
                                            for (rakamDoc in rakamIds) {
                                                val tempRakamString =
                                                    rakamDoc.data["metal_type"].toString().replace(","," ") + "," + rakamDoc.data["rakam_type"].toString().replace(","," ") + "," + rakamDoc.data["net_weight_gms"].toString().replace(","," ") + "," + rakamDoc.data["weight_gms"].toString().replace(","," ") + ","
                                                //Reading Transaction data for each rakam here
                                                db.collection("cust").document(customerDoc.id)
                                                    .collection("rakam").document(rakamDoc.id)
                                                    .collection("transaction").get()
                                                    .addOnSuccessListener { transIDs ->
                                                        if (!transIDs.isEmpty) {
                                                            for (transDoc in transIDs) {
                                                                val tempTransString =
                                                                    transDoc.data["type"].toString().replace(","," ") + "," + transDoc.data["date"].toString().replace(","," ") + "," + transDoc.data["amount"].toString().replace(","," ") + "," + transDoc.data["ir"].toString().replace(","," ") + "," + transDoc.data["remarks"].toString().replace(","," ")
                                                                custString += tempCustString + tempRakamString + tempTransString + "\n"
                                                            }
                                                        }
                                                        writeToFile(custString)
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            alertBuilder.show()
        }

        return binding.root
    }

    private fun writeToFile(data:String){
        val appSpecificExternalDir = File(context?.getExternalFilesDir(null), "data.csv")
        val fileOutputStream = FileOutputStream(appSpecificExternalDir)
        fileOutputStream.write(data.toByteArray())
        fileOutputStream.close()
        binding.homeProgressBar.visibility = View.GONE
        Toast.makeText(context, "File written successfully", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getFinalAmount(){
        db.collection("cust").get().addOnSuccessListener {custs->
            if (!custs.isEmpty){
                for(cust in custs){
                    db.collection("cust").document(cust.id).collection("rakam")
                        .get().addOnSuccessListener {rakams->
                            if (!rakams.isEmpty){
                                for (rakam in rakams){
                                    val rakamWeight = rakam.data["weight_gms"].toString().toFloat()
                                    val metalType: String? = rakam.data["metal_type"]?.toString()
                                    db.collection("cust").document(cust.id)
                                        .collection("rakam").document(rakam.id)
                                        .collection("transaction").get()
                                        .addOnSuccessListener {tss->
                                            if(!tss.isEmpty){
                                                for(tran in tss){
                                                    if(tran.data["ir"].toString().isNotEmpty()){
                                                        irForIntCal = tran.data["ir"] as String
                                                    }
                                                    tranListForIntCal.add(mutableListOf(tran.data["type"].toString(),
                                                        tran.data["amount"] as String, tran.data["date"] as String
                                                    ))
                                                }
                                            }
                                            val intCalObject = IntCalculator()
                                            finalAmount = intCalObject.calculateFinalAmount(tranListForIntCal, irForIntCal)
                                            if (metalType == "SILVER" || metalType.isNullOrEmpty()) {
                                                if ((finalAmount > ((rakamWeight * silRate.toFloat()) / 1000) && !totalAmountFlag)) {
                                                    val custData = DataCustSearch(
                                                        cust.data["f_name"].toString(),
                                                        cust.data["m_name"].toString(),
                                                        cust.data["l_name"].toString(),
                                                        cust.data["city"].toString(),
                                                        cust.data["mobile_no"].toString(),
                                                        cust.data["aadhar_no"].toString(),
                                                        cust.data["cid"].toString()
                                                    )
                                                    custSearchList.add(custData)
                                                    adapter.notifyDataSetChanged()
                                                }
                                            }
                                            if (metalType == "GOLD"){
                                                if ((finalAmount > ((rakamWeight * goldRate.toFloat()) / 10) && !totalAmountFlag)) {
                                                    val custData = DataCustSearch(
                                                        cust.data["f_name"].toString(),
                                                        cust.data["m_name"].toString(),
                                                        cust.data["l_name"].toString(),
                                                        cust.data["city"].toString(),
                                                        cust.data["mobile_no"].toString(),
                                                        cust.data["aadhar_no"].toString(),
                                                        cust.data["cid"].toString()
                                                    )
                                                    custSearchList.add(custData)
                                                    adapter.notifyDataSetChanged()
                                                }
                                            }
                                            tranListForIntCal.clear()
                                            irForIntCal = "0"

                                            // adding code for total value
                                            if (!tss.isEmpty && finalAmount>1000f && totalAmountFlag){
                                                var tempAmount = 0
                                                for(tran in tss){
                                                    if(tran.data["type"].toString() == "NAAME"){
                                                        totalAmount += tran.data["amount"].toString().toInt()
                                                        tempAmount += tran.data["amount"].toString().toInt()
                                                    }
                                                    else{
                                                        totalJama += tran.data["amount"].toString().toInt()
                                                        tempAmount -= tran.data["amount"].toString().toInt()
                                                    }
                                                }
                                                activeInterest += (finalAmount.toInt() - tempAmount)
                                                binding.tvActiveInterestValue.text = activeInterest.toString()
                                                binding.tvTotalAmountValue.text = totalAmount.toString()
                                                binding.tvTotalJamaValue.text = totalJama.toString()
                                            }
                                        }

                                }
                            }
                        }
                }
            }

        }

    }

}