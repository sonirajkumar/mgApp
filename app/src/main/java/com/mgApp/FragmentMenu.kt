package com.mgApp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentMenuBinding
class FragmentMenu : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var token: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private var custSearchList = ArrayList<DataCustSearch>()
    private lateinit var adapter: AdapterCustSearch
    private lateinit var custData: DataCustSearch
    private val db = Firebase.firestore
    private var lastHistCid = "1"

    private var histCidList: MutableList<String> = mutableListOf()
    private var histCustDetails: MutableMap<String, DataCustSearch> = mutableMapOf()

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
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        token = requireContext().getSharedPreferences("email", Context.MODE_PRIVATE)
        val rvCustHistory = binding.rvLastTransactions
        rvCustHistory.layoutManager = LinearLayoutManager(context)
        adapter = AdapterCustSearch(custSearchList)
        rvCustHistory.adapter = adapter
        custSearchList.clear()
        adapter.notifyDataSetChanged()

        auth = FirebaseAuth.getInstance()

        binding.btnShowLastTransactions.setOnClickListener {
            custSearchList.clear()
            adapter.notifyDataSetChanged()

            db.collection("history").orderBy("timestamp", Query.Direction.ASCENDING).limit(1).get()
                .addOnSuccessListener { lastCust ->
                    if (!lastCust.isEmpty) {
                        for (ele in lastCust) {
                            lastHistCid = ele.data["cid"].toString()
                        }
                    }


                    db.collection("history").orderBy("timestamp", Query.Direction.DESCENDING).get()
                        .addOnSuccessListener { custID ->
                            if (!custID.isEmpty) {
                                for (ids in custID) {
                                    if (!histCidList.contains(
                                            ids.data["cid"].toString()
                                                .filter { !it.isWhitespace() })
                                    ) {
                                        histCidList.add(
                                            ids.data["cid"].toString()
                                                .filter { !it.isWhitespace() })
                                    }
                                }
                            }
                            db.collection("cust").get().addOnSuccessListener { custIDs ->
                                if (!custIDs.isEmpty) {
                                    for (ele in histCidList) {
//                                        var isEleFound = 0
                                        for (customerDoc in custIDs) {
                                            if (customerDoc.id.takeLast(ele.length + 1) == "_${ele.filter { !it.isWhitespace() }}") {
//                                                isEleFound = 1
                                                db.collection("cust").document(customerDoc.id).get()
                                                    .addOnSuccessListener { document ->
                                                        custData = DataCustSearch(
                                                            document.data!!["f_name"].toString(),
                                                            document.data!!["m_name"].toString(),
                                                            document.data!!["l_name"].toString(),
                                                            document.data!!["city"].toString(),
                                                            document.data!!["mobile_no"].toString(),
                                                            document.data!!["aadhar_no"].toString(),
                                                            document.data!!["cid"].toString()
                                                        )
                                                        histCustDetails[ele] = custData
//                                                if(histCidList.size == histCustDetails.size){
                                                        if (lastHistCid == ele) {
                                                            for (histCust in histCidList) {
                                                                histCustDetails[histCust]?.let { it1 ->
                                                                    custSearchList.add(
                                                                        it1
                                                                    )
                                                                }
                                                                adapter.notifyDataSetChanged()
                                                            }
                                                        }
//                                                println(histCustDetails.size)
//                                                println(histCidList.size)
                                                    }
                                            }
                                        }
//                                if (isEleFound==0){
//                                    println("ele $ele not found")
//                                }
                                    }
                                }
                            }
                        }
                }

            db.collection("history").count().get(AggregateSource.SERVER)
                .addOnCompleteListener { count ->
                    if (count.isSuccessful) {
                        val countDiff = count.result.count - 50
                        if (countDiff > 0) {
                            db.collection("history").orderBy("timestamp").limit(countDiff)
                                .get().addOnSuccessListener {
                                    for (docs in it) {
                                        db.collection("history").document(docs.id).delete()
                                    }
                                }
                        }
                    }
                }
        }

//        binding.btnBackup.setOnClickListener {
//
//        }

        binding.btnLogout.setOnClickListener {
            token.edit().clear().apply()
            auth.signOut()

            val intent = Intent(requireContext(), ActivityLogin::class.java)
            startActivity(intent)

        }
        return binding.root
    }

}