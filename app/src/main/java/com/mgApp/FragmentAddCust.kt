package com.mgApp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentAddCustBinding


class FragmentAddCust : Fragment() {
    private var _binding: FragmentAddCustBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var fName: String
    private lateinit var mName: String
    private lateinit var lName: String
    private lateinit var city: String
    private var mobileNumber: String = "null"
    private var aadharNumber: String = "null"
    private lateinit var oldCid: String
    private lateinit var cid: String
    private lateinit var custDocumentId: String
    private var isTransferredFromSearch: Boolean = false
    private var isCustomerExists:Boolean = false
    private var lastCidNumber: String = ""
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {bundle->
            fName = bundle.getString("f_name").toString()
            mName = bundle.getString("m_name").toString()
            lName = bundle.getString("l_name").toString()
            city = bundle.getString("city").toString()
            mobileNumber = bundle.getString("mobile_number").toString()
            aadharNumber = bundle.getString("aadhar_number").toString()
            oldCid = bundle.getString("cid").toString()
            isTransferredFromSearch = bundle.getBoolean("isTransferredFromSearch")
            custDocumentId = fName.filter { !it.isWhitespace() } +"_"+ mName.filter { !it.isWhitespace() } +"_"+ lName.filter { !it.isWhitespace() } +"_"+ city.filter { !it.isWhitespace() }+"_"+ mobileNumber.filter { !it.isWhitespace() }+"_"+ aadharNumber.filter { !it.isWhitespace() } +"_"+ oldCid.filter { !it.isWhitespace() }
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentAddCustBinding.inflate(inflater, container, false)
        alertBuilder = AlertDialog.Builder(activity)

        val etAadharNumber = binding.aadharNumber

        etAadharNumber.addTextChangedListener(object: TextWatcher{
            val prevL = 0
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val length = p0?.length
                if((prevL < length!!) && (length==4 || length==9)){
                    p0.append("-")
                }
            }

        })

        if (isTransferredFromSearch) {
            binding.firstName.setText(fName)
            binding.middleName.setText(mName)
            binding.lastName.setText(lName)
            binding.city.setText(city)
            binding.mobileNumber.setText(mobileNumber)
            binding.aadharNumber.setText(aadharNumber)
            binding.customerId.setText(oldCid)
            binding.addAccountButton.text = "Update Customer Info"
            binding.textViewAddAccount.text = "Update Customer Account"
        }

        binding.btnShowLastCid.setOnClickListener {
            db.collection("last_cid").document("cid").get().addOnSuccessListener {
                lastCidNumber = "Last Customer ID: " + it.data?.get("cid").toString()
                binding.tvLastCidNumber.text = lastCidNumber
            }
        }

        binding.addAccountButton.setOnClickListener {
            fName = binding.firstName.text.toString().replace("/",".").uppercase()
            lName = binding.lastName.text.toString().replace("/",".").uppercase()
            mName = binding.middleName.text.toString().replace("/",".").uppercase()
            city = binding.city.text.toString().replace("/",".").uppercase()
            cid = binding.customerId.text.toString().replace("/",".").uppercase()
            mobileNumber = binding.mobileNumber.text.toString().replace("/",".")
            aadharNumber = binding.aadharNumber.text.toString().replace("/",".")
            isCustomerExists = false

            if (fName.isEmpty() or lName.isEmpty() or mName.isEmpty() or city.isEmpty() or cid.isEmpty()) {
                Toast.makeText(activity, "Please insert Valid Data ", Toast.LENGTH_LONG).show()
            } else if (mobileNumber.isNotBlank() and (mobileNumber.length != 10)) {
                Toast.makeText(activity, "Please insert Valid Number ", Toast.LENGTH_LONG)
                    .show()
            } else if (aadharNumber.isNotBlank() and (aadharNumber.length != 14)) {
                Toast.makeText(activity, "Please insert Valid Aadhar ", Toast.LENGTH_LONG)
                    .show()
            } else {
                db.collection("cust").get().addOnSuccessListener { custDocs ->

                    if (!custDocs.isEmpty) {
                        for (docs in custDocs) {
                            if ((docs.data["cid"].toString() == cid) && !isTransferredFromSearch) {
                                isCustomerExists = true
                            }
                        }
                        if (isCustomerExists) {
                            Toast.makeText(context, "Customer ID Already Exists", Toast.LENGTH_LONG).show()
                        } else {
                            alertBuilder.setTitle("Confirmation")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes") { _, _ ->
                                    addCustomer()
                                }.setNegativeButton("No"){_,_->
                                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                                }
                            alertBuilder.show()
                        }
                    }
                }.addOnFailureListener { Toast.makeText(context, "Data Fetching Failed", Toast.LENGTH_SHORT).show()
                }
            }

        }
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun addCustomer(){
        val accountHashMap = hashMapOf(
            "f_name" to fName,
            "m_name" to mName,
            "l_name" to lName,
            "city" to city,
            "mobile_no" to mobileNumber,
            "aadhar_no" to aadharNumber,
            "cid" to cid

        )
        val tempDocID = fName.filter { !it.isWhitespace() } + "_"+ mName.filter { !it.isWhitespace() } + "_"+ lName.filter { !it.isWhitespace() } + "_"+ city.filter { !it.isWhitespace() } + "_"+ mobileNumber.filter { !it.isWhitespace() } + "_"+ aadharNumber.filter { !it.isWhitespace() } +"_"+ cid.filter { !it.isWhitespace() }
        if (!isTransferredFromSearch) {
            db.collection("cust").document(tempDocID)
                .set(accountHashMap, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(activity, "Account Added Successfully", Toast.LENGTH_LONG).show()
                    db.collection("last_cid").document("cid").set(hashMapOf("cid" to cid))
                    binding.firstName.text.clear()
                    binding.lastName.text.clear()
                    binding.middleName.text.clear()
                    binding.city.text.clear()
                    binding.mobileNumber.text.clear()
                    binding.aadharNumber.text.clear()
                    binding.customerId.text.clear()

                    val bundle = Bundle()
                    val nextFragment = FragmentAddRakam()
                    bundle.putString("f_name", fName)
                    bundle.putString("m_name", mName)
                    bundle.putString("l_name", lName)
                    bundle.putString("city", city)
                    bundle.putString("mobile_number", mobileNumber)
                    bundle.putString("aadhar_number", aadharNumber)
                    bundle.putString("cid", cid)
                    nextFragment.arguments = bundle


                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frameLayout,nextFragment).addToBackStack(null).commit()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Account insertion Failed", Toast.LENGTH_LONG).show()
                }
        }
        else {
            if (tempDocID == custDocumentId) {
                Toast.makeText(context, "No Change in Customer Data", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("cust").document(tempDocID)
                    .set(accountHashMap, SetOptions.merge())
                    .addOnSuccessListener {
                        db.collection("cust").document(custDocumentId)
                            .collection("rakam").get()
                            .addOnSuccessListener { rakams ->
                                if (!rakams.isEmpty) {
                                    for (rakam in rakams) {
                                        db.collection("cust").document(tempDocID).collection("rakam")
                                            .document(rakam.id).set(rakam.data).addOnSuccessListener {

                                                db.collection("cust").document(custDocumentId)
                                                    .collection("rakam").document(rakam.id)
                                                    .collection("transaction").get()
                                                    .addOnSuccessListener { ts ->
                                                        if (!ts.isEmpty) {
                                                            for (transactions in ts) {
                                                                db.collection("cust").document(tempDocID)
                                                                    .collection("rakam").document(rakam.id)
                                                                    .collection("transaction")
                                                                    .document(transactions.id)
                                                                    .set(transactions.data)
                                                                    .addOnSuccessListener {
                                                                        db.collection("cust")
                                                                            .document(custDocumentId)
                                                                            .collection("rakam")
                                                                            .document(rakam.id)
                                                                            .collection("transaction")
                                                                            .document(transactions.id)
                                                                            .delete()
                                                                    }

                                                            }
                                                        }
                                                    }
                                                db.collection("cust").document(custDocumentId)
                                                    .collection("rakam").document(rakam.id).delete()
                                            }
                                    }
                                }
                            }
                        Toast.makeText(
                            activity,
                            "Account Updated Successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.firstName.text.clear()
                        binding.lastName.text.clear()
                        binding.middleName.text.clear()
                        binding.city.text.clear()
                        binding.mobileNumber.text.clear()
                        binding.aadharNumber.text.clear()
                        binding.customerId.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Account Update Failed", Toast.LENGTH_LONG)
                            .show()
                    }


                db.collection("cust").document(custDocumentId).delete()

                if(oldCid != cid) {
                    db.collection("history").get().addOnSuccessListener {
                        if (!it.isEmpty) {
                            for (custs in it) {
                                db.collection("history").document(custs.id).get()
                                    .addOnSuccessListener { hist ->
                                        val histCid = hist.data?.get("cid").toString()
                                        if (histCid == oldCid) {
                                            db.collection("history").document(custs.id).delete()
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