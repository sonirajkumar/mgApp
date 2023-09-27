package com.mgApp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentRakamSearchBinding

class FragmentRakamSearch : Fragment() {
    private var _binding: FragmentRakamSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertBuilder: AlertDialog.Builder

    private lateinit var fName: String
    private lateinit var mName: String
    private lateinit var lName: String
    private lateinit var city: String
    private var mobileNumber: String? = null
    private var aadharNumber: String? = null
    private lateinit var cid: String
    private lateinit var custDocumentId: String

    private lateinit var recyclerView: RecyclerView
    private var rakamList = ArrayList<DataRakamSearch>()
    private lateinit var adapter: AdapterRakamSearch

    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            fName = bundle.getString("f_name").toString()
            mName = bundle.getString("m_name").toString()
            lName = bundle.getString("l_name").toString()
            city = bundle.getString("city").toString()
            mobileNumber = bundle.getString("mobile_number").toString()
            aadharNumber = bundle.getString("aadhar_number").toString()
            cid = bundle.getString("cid").toString()
            custDocumentId = fName.filter { !it.isWhitespace() } +"_"+ mName.filter { !it.isWhitespace() } +"_"+ lName.filter { !it.isWhitespace() } +"_"+ city.filter { !it.isWhitespace() }+"_"+ mobileNumber!!.filter { !it.isWhitespace() }+"_"+ aadharNumber!!.filter { !it.isWhitespace() }+"_"+ cid.filter { !it.isWhitespace() }

        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRakamSearchBinding.inflate(inflater, container, false)
        alertBuilder = AlertDialog.Builder(activity)

        recyclerView = binding.recyclerViewRakamSearch
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AdapterRakamSearch(rakamList)
        recyclerView.adapter = adapter
        rakamList.clear()
        adapter.notifyDataSetChanged()

        val custDetails = "$fName $mName $lName $city $cid"
        binding.textViewSearchResultCustDetails.text = custDetails
        binding.textViewMobileNumber.text = "Mobile: $mobileNumber"
        binding.textViewAadharNumber.text = "Aadhar: $aadharNumber"

        val collectionRef = db.collection("cust").document(custDocumentId).collection("rakam")
        collectionRef.get().addOnSuccessListener {
            if(!it.isEmpty){
                for(rakam in it){
                    val rakamDetail = DataRakamSearch(
                        fName,mName,lName,city,mobileNumber,aadharNumber,cid,
                        rakam.data["rakam_type"].toString(),
                        rakam.data["weight_gms"].toString(),
                        rakam.data["metal_type"]?.toString(),
                        rakam.data["net_weight_gms"]?.toString()
                    )
                    rakamList.add(rakamDetail)
                    adapter.notifyDataSetChanged()
                }

            }

        }.addOnFailureListener {
            Toast.makeText(context, "Data Fetching Failed", Toast.LENGTH_LONG).show()
        }

        binding.btnEditCustInfo.setOnClickListener {
            val bundle = Bundle()
            val nextFragment = FragmentAddCust()
            bundle.putString("f_name", fName)
            bundle.putString("m_name", mName)
            bundle.putString("l_name", lName)
            bundle.putString("city", city)
            bundle.putString("mobile_number", mobileNumber)
            bundle.putString("aadhar_number", aadharNumber)
            bundle.putString("cid", cid)
            bundle.putBoolean("isTransferredFromSearch", true)
            nextFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frameLayout, nextFragment).commit()
        }

        binding.buttonRakamSearchAddRakam.setOnClickListener {
            val nextFragment = FragmentAddRakam()
            nextFragment.arguments = arguments
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frameLayout, nextFragment).commit()
        }

        // DELETING CUSTOMER HERE
        binding.btnDeleteCustomer.setOnClickListener {
            alertBuilder.setTitle("Confirmation")
                .setMessage("Are you sure want to Delete Customer?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    db.collection("archive").document(custDocumentId).set(
                        hashMapOf(
                            "f_name" to fName,
                            "m_name" to mName,
                            "l_name" to lName,
                            "city" to city,
                            "mobile_no" to mobileNumber,
                            "aadhar_no" to aadharNumber,
                            "cid" to cid
                        )
                    ).addOnSuccessListener {  }.addOnFailureListener {  }

                    db.collection("cust").document(custDocumentId)
                        .collection("rakam").get()
                        .addOnSuccessListener { rakams->
                            if (!rakams.isEmpty){
                                for(rakam in rakams){
                                    db.collection("archive").document(custDocumentId)
                                        .collection("rakam").document(rakam.id).set(rakam.data)

                                    db.collection("cust").document(custDocumentId)
                                        .collection("rakam").document(rakam.id)
                                        .collection("transaction").get()
                                        .addOnSuccessListener { ts->
                                            if (!ts.isEmpty){
                                                for (transactions in ts) {
                                                    db.collection("archive").document(custDocumentId)
                                                        .collection("rakam").document(rakam.id)
                                                        .collection("transaction").document(transactions.id)
                                                        .set(transactions.data)

                                                    db.collection("cust").document(custDocumentId)
                                                        .collection("rakam").document(rakam.id)
                                                        .collection("transaction").document(transactions.id)
                                                        .delete()
                                                }
                                            }
                                        }
                                    db.collection("cust").document(custDocumentId)
                                        .collection("rakam").document(rakam.id).delete()
                                }
                            }
                        }
                    db.collection("cust").document(custDocumentId).delete().addOnSuccessListener {
                        Toast.makeText(activity, "Deleted Successfully", Toast.LENGTH_LONG).show()
                    }

                    db.collection("history").get().addOnSuccessListener {
                        if (!it.isEmpty){
                            for (custs in it){
                                db.collection("history").document(custs.id).get().addOnSuccessListener { hist->
                                    val histCid = hist.data?.get("cid").toString()
                                    if (histCid==cid){
                                        db.collection("history").document(custs.id).delete()
                                    }
                                }
                            }
                        }
                    }

                }.setNegativeButton("No") { _, _ ->
                    Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            alertBuilder.show()
        }
        return binding.root
    }

}