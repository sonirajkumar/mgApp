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
    private var custSearchList = ArrayList<DataHistorySearch>()
    private lateinit var adapter: AdapterHistorySearch
    private lateinit var custData: DataHistorySearch
    private val db = Firebase.firestore
    private var lastHistCid = "1"

    private var histCidList: MutableList<String> = mutableListOf()
    //    private var histCustDetails: MutableList<DataHistorySearch> = mutableListOf()
    private var histCustDetails: MutableMap<String, DataHistorySearch> = mutableMapOf()

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
        adapter = AdapterHistorySearch(custSearchList)
        rvCustHistory.adapter = adapter

        auth = FirebaseAuth.getInstance()

        binding.btnShowLastTransactions.setOnClickListener {
            var lastDate=""
            histCidList.clear()
            custSearchList.clear()
            adapter.notifyDataSetChanged()
            // ADDING LAST HISTORY CID TO A VARIABLE
            db.collection("history").orderBy("timestamp", Query.Direction.ASCENDING).limit(1).get()
                .addOnSuccessListener { lastCust ->
                    if (!lastCust.isEmpty) {
                        for (ele in lastCust) {
                            lastHistCid = ele.data["cid"].toString().filter { !it.isWhitespace() }
                        }
                    }

                    // ADDING HISTORY CID TO A LIST WITH TIMESTAMP
                    db.collection("history").orderBy("timestamp", Query.Direction.DESCENDING).get()
                        .addOnSuccessListener { custID ->
                            var lastDataAvailable = ""
                            if (!custID.isEmpty) {
                                for (ids in custID) {
                                    if (ids.data["timestamp"].toString().split("T")[0] != lastDataAvailable ){

                                        histCidList.add(ids.data["timestamp"].toString().split("T")[0].filter { !it.isWhitespace() })
                                        lastDataAvailable = ids.data["timestamp"].toString().split("T")[0].filter { !it.isWhitespace() }

                                        if (!histCidList.contains(ids.data["cid"].toString().filter { !it.isWhitespace() }+ "|" +ids.data["timestamp"].toString().filter { !it.isWhitespace() })
                                        ) {
                                            histCidList.add(ids.data["cid"].toString().filter { !it.isWhitespace() }+ "|" + ids.data["timestamp"].toString().filter { !it.isWhitespace() })
                                        }
                                    } else {
                                        if (!histCidList.contains(ids.data["cid"].toString().filter { !it.isWhitespace() } + "|" +  ids.data["timestamp"].toString().filter { !it.isWhitespace() })
                                        ) {
                                            histCidList.add(ids.data["cid"].toString().filter { !it.isWhitespace() } + "|" + ids.data["timestamp"].toString().filter { !it.isWhitespace() })
                                        }
                                    }
                                }
//                                println(histCidList)
                            }
                            db.collection("cust").get().addOnSuccessListener { custIDs ->
                                if (!custIDs.isEmpty) {
                                    for (ele in histCidList) {
//                                        println(ele)
                                        for (customerDoc in custIDs) {
                                            if (customerDoc.id.takeLast(ele.split("|")[0].length + 1) == "_${ele.split("|")[0].filter { !it.isWhitespace() }}") {
//                                                println("ele found: $customerDoc")
                                                db.collection("cust").document(customerDoc.id).get()
                                                    .addOnSuccessListener { document ->
                                                        if (lastDate != ele.split("|")[1].split("T")[0]) {
                                                            lastDate = ele.split("|")[1].split("T")[0]
                                                            custData = DataHistorySearch(
                                                                "null",
                                                                "null",
                                                                "null",
                                                                "null",
                                                                "null",
                                                                "null",
                                                                "null",
                                                                ele.split("|")[1].split("T")[0]
                                                            )
                                                            histCustDetails[ele.split("|")[1].split("T")[0]]=custData

                                                            custData = DataHistorySearch(
                                                                document.data!!["f_name"].toString(),
                                                                document.data!!["m_name"].toString(),
                                                                document.data!!["l_name"].toString(),
                                                                document.data!!["city"].toString(),
                                                                document.data!!["mobile_no"].toString(),
                                                                document.data!!["aadhar_no"].toString(),
                                                                document.data!!["cid"].toString(),
                                                                "null"
                                                            )
                                                            histCustDetails[ele]=custData
//                                                            println("LastDateNotThere:$histCustDetails")
                                                        }else{
                                                            custData = DataHistorySearch(
                                                                document.data!!["f_name"].toString(),
                                                                document.data!!["m_name"].toString(),
                                                                document.data!!["l_name"].toString(),
                                                                document.data!!["city"].toString(),
                                                                document.data!!["mobile_no"].toString(),
                                                                document.data!!["aadhar_no"].toString(),
                                                                document.data!!["cid"].toString(),
                                                                "null"
                                                            )
                                                            histCustDetails[ele]=custData
//                                                            println("LastDateNotThere:$histCustDetails")
                                                        }
                                                        if (ele.split("|")[0]==lastHistCid){
//                                                            println(histCustDetails)
//                                                            println(histCidList)
                                                            for (histCust in histCidList) {
                                                                histCustDetails[histCust]?.let { it1 ->
                                                                    custSearchList.add(
                                                                        it1
                                                                    )
                                                                }
                                                                adapter.notifyDataSetChanged()
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


            db.collection("history").count().get(AggregateSource.SERVER)
                .addOnCompleteListener { count ->
                    if (count.isSuccessful) {
                        val countDiff = count.result.count - 100
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