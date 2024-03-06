package com.mgApp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentCustSearchBinding

class FragmentCustSearch : Fragment() {
    private var _binding: FragmentCustSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private var custSearchList = ArrayList<DataCustSearch>()
    private lateinit var custSearchView: SearchView
    private lateinit var adapter: AdapterCustSearch
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustSearchBinding.inflate(inflater, container, false)
        recyclerView = binding.searchRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        custSearchView = binding.searchView
        adapter = AdapterCustSearch(custSearchList)
        recyclerView.adapter = adapter

        custSearchView.setOnQueryTextListener(object: OnQueryTextListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(query: String?): Boolean {
                custSearchList.clear()
                adapter.notifyDataSetChanged()
                if (query != null) {
                    getCustData(query.filter { !it.isWhitespace() }.uppercase())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getCustData(query: String?){
        custSearchList.clear()
        adapter.notifyDataSetChanged()
        val collectionRef = db.collection("cust")
        collectionRef.get().addOnSuccessListener {
            if (!it.isEmpty){
                for (docs in it){
                    if(docs.id.contains("$query")){
                        collectionRef.document(docs.id).get().addOnSuccessListener {
                            document->
                            val cust = DataCustSearch(document.data?.get("f_name") as String,
                                document.data?.get("m_name") as String,
                                document.data?.get("l_name") as String,
                                document.data?.get("city") as String,
                                document.data?.get("mobile_no") as String,
                                document.data?.get("aadhar_no") as String,
                                document.data?.get("cid") as String)

                            custSearchList.add(cust)
                            adapter.notifyDataSetChanged()
                        }
                            .addOnFailureListener {
                                Toast.makeText(context, "Data Fetching Failed", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            }
        }
            .addOnFailureListener{
                Toast.makeText(context, "Data Fetching Failed", Toast.LENGTH_LONG).show()
            }

    }

}
