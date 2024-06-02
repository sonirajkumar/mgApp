package com.mgApp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentAddRakamDetailsBinding


class FragmentAddRakam : Fragment() {

    private var _binding: FragmentAddRakamDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertBuilder: AlertDialog.Builder
    private var db = Firebase.firestore
    private lateinit var fName: String
    private lateinit var mName: String
    private lateinit var lName: String
    private lateinit var city: String
    private var mobileNumber: String? = null
    private var aadharNumber: String? = null
    private lateinit var cid: String
    private lateinit var custDocumentId: String
    private lateinit var rakamType: String
    private lateinit var rakamNetWeight: String
    private lateinit var rakamWeight: String
    private lateinit var metalType: String

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
            custDocumentId = fName.filter { !it.isWhitespace() } +"_"+ mName.filter { !it.isWhitespace() } +"_"+ lName.filter { !it.isWhitespace() } +"_"+ city.filter { !it.isWhitespace() }+"_"+ mobileNumber!!.filter { !it.isWhitespace() }+"_"+ aadharNumber!!.filter { !it.isWhitespace() } +"_"+ cid.filter { !it.isWhitespace() }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRakamDetailsBinding.inflate(inflater, container, false)
        val showName = "$fName $mName $lName $city"
        binding.textViewName.text = showName
        val showCID = "Customer ID: $cid"
        binding.tvCidNumber.text = showCID

        alertBuilder = AlertDialog.Builder(activity)

        binding.addRakamButton.setOnClickListener {
            rakamType = binding.rakamType.text.toString().uppercase()
            rakamWeight = binding.rakamWeight.text.toString().uppercase()
            rakamNetWeight = binding.netRakamWeight.text.toString().uppercase()
            val radioGrpSelection = binding.radioGrpMetalType.checkedRadioButtonId
            metalType = binding.root.findViewById<RadioButton>(radioGrpSelection).text.toString().uppercase()

            if (rakamType.isEmpty() or rakamWeight.isEmpty() or rakamNetWeight.isEmpty()){
                Toast.makeText(activity,"Please insert Valid/Complete Data!", Toast.LENGTH_LONG).show()
            }
            else if (rakamWeight.toFloat() > rakamNetWeight.toFloat()){
                Toast.makeText(activity,"Fine weight should be less than or equal to net weight!", Toast.LENGTH_LONG).show()
            }
            else{
                val query = rakamType.filter { !it.isWhitespace() }+"_"+rakamWeight+"GMS"
                db.collection("cust").document(custDocumentId).collection("rakam").get().addOnSuccessListener { rakamDocs ->
                    if (!rakamDocs.isEmpty) {
                        for (docs in rakamDocs) {
                            if (docs.id.contains(query)) {
                                Toast.makeText(context, "Rakam Already Exists", Toast.LENGTH_LONG).show()
                            }
                            else{
                                addRakam()
                            }
                        }
                    }
                    else{
                        addRakam()
                    }
                }


            }
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addRakam(){
        alertBuilder.setTitle("Confirmation")
            .setMessage("Are you sure want to add rakam?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val rakamHashMap = hashMapOf(
                    "metal_type" to metalType,
                    "rakam_type" to rakamType,
                    "weight_gms" to rakamWeight,
                    "net_weight_gms" to rakamNetWeight
                )

                db.collection("cust").document(custDocumentId)
                    .collection("rakam").document(rakamType.filter { !it.isWhitespace() } +"_"+rakamWeight+"GMS")
                    .set(rakamHashMap, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Rakam Added Successfully", Toast.LENGTH_LONG).show()
                        binding.rakamType.text.clear()
                        binding.rakamWeight.text.clear()
                        binding.netRakamWeight.text.clear()

                        val bundle = Bundle()
                        val nextFragment = FragmentAddTransaction()
                        bundle.putString("f_name", fName)
                        bundle.putString("m_name", mName)
                        bundle.putString("l_name", lName)
                        bundle.putString("city", city)
                        bundle.putString("mobile_number", mobileNumber)
                        bundle.putString("aadhar_number", aadharNumber)
                        bundle.putString("cid", cid)
                        bundle.putString("rakam_type", rakamType)
                        bundle.putString("rakam_weight", rakamWeight)
                        bundle.putString("net_weight_gms", rakamNetWeight)
                        bundle.putString("metal_type", metalType)
                        nextFragment.arguments = bundle

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, nextFragment).addToBackStack(null).commit()
                    }
                    .addOnFailureListener{
                        Toast.makeText(activity, "Rakam insertion Failed", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("No"){_, _ ->
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        alertBuilder.show()
    }

}