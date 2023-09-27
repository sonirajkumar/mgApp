package com.mgApp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mgApp.databinding.FragmentAddTransactionBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FragmentAddTransaction : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var alertBuilder: AlertDialog.Builder
    private var db = Firebase.firestore
    private lateinit var editTextDate: EditText
    private lateinit var btnDatePicker: Button
    private var ir: String? = ""
    private lateinit var radioBtn: RadioButton
    private lateinit var amount: String
    private lateinit var date:String
    private var remarks: String? = ""

    private lateinit var fName: String
    private lateinit var mName: String
    private lateinit var lName: String
    private lateinit var city: String
    private var mobileNumber: String? = null
    private var aadharNumber: String? = null
    private lateinit var cid: String
    private lateinit var custDocumentId: String
    private lateinit var rakamType: String
    private lateinit var netRakamWeight: String
    private lateinit var rakamWeight: String
    private lateinit var metalType: String
    private lateinit var rakamDocumentID: String
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
            custDocumentId = fName.filter { !it.isWhitespace() } + "_" + mName.filter { !it.isWhitespace() } + "_" + lName.filter { !it.isWhitespace() } + "_" + city.filter { !it.isWhitespace() } + "_" + mobileNumber!!.filter { !it.isWhitespace() } + "_" + aadharNumber!!.filter { !it.isWhitespace() } + "_" + cid.filter { !it.isWhitespace() }

            rakamType = bundle.getString("rakam_type").toString()
            rakamWeight = bundle.getString("rakam_weight").toString()
            netRakamWeight = bundle.getString("net_weight_gms").toString()
            metalType = bundle.getString("metal_type").toString()
            rakamDocumentID = rakamType.filter { !it.isWhitespace() } + "_" + rakamWeight + "GMS"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)

        var radioSelection = binding.radioGrpNaameJama.checkedRadioButtonId
        radioBtn = binding.root.findViewById(radioSelection)

        val showName = "$fName $mName $lName $city"
        val showRakam = "$metalType: $rakamType"
        val showWeight = "Net: $netRakamWeight GMS | Fine: $rakamWeight GMS | Customer ID: $cid"

        binding.textViewName.text = showName
        binding.textViewRakam.text = showRakam
        binding.textViewRakamWeightNumber.text = showWeight

        val myCalender = Calendar.getInstance()
        editTextDate = binding.editTextDate
        btnDatePicker = binding.datePickerButton

        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH, month)
            myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editTextDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(myCalender.time))
        }
        btnDatePicker.setOnClickListener{
            DatePickerDialog(requireContext(), datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
            myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        alertBuilder = AlertDialog.Builder(activity)

        binding.radioGrpNaameJama.setOnCheckedChangeListener { _, _ ->
            radioSelection = binding.radioGrpNaameJama.checkedRadioButtonId
            radioBtn = binding.root.findViewById(radioSelection)

            binding.editTextIr.isEnabled = radioBtn.text.toString() != "Jama"
            if (radioBtn.text.toString() == "Jama"){
                binding.editTextIr.text.clear()
                ir=""
            }
        }

        val etDate = binding.editTextDate
        etDate.addTextChangedListener(object: TextWatcher{
            val prevL = 0
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val length = p0?.length
                if((prevL < length!!) && (length==2 || length==5)){
                    p0.append("/")
                }

            }

        })

        binding.addTransactionBtn.setOnClickListener {
            amount = binding.editTextAmount.text.toString()
            remarks = binding.editTextRemarks.text.toString().uppercase()
            date = editTextDate.text.toString()

            if(radioBtn.text == "Naame"){
                ir = binding.editTextIr.text.toString()
            }

            if (amount.isEmpty() or date.isEmpty()){
                Toast.makeText(activity,"Please insert  ", Toast.LENGTH_LONG).show()
            }
            else if(radioBtn.text == "Naame" && ir.isNullOrEmpty()){
                Toast.makeText(activity,"Please insert IR ", Toast.LENGTH_LONG).show()

            }
            else {

                alertBuilder.setTitle("Confirmation")
                    .setMessage("Are you sure want to add transaction?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        val transactionHashMap = hashMapOf(
                            "type" to radioBtn.text.toString().uppercase(),
                            "amount" to amount,
                            "ir" to ir,
                            "remarks" to remarks,
                            "date" to date,
                            "timestamp" to LocalDateTime.now().toString()
                        )
                        db.collection("cust").document(custDocumentId)
                            .collection("rakam").document(rakamDocumentID)
                            .collection("transaction").document(LocalDateTime.now().toString())
                            .set(transactionHashMap, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(activity, "Transaction Added Successfully", Toast.LENGTH_LONG).show()
                                binding.editTextAmount.text.clear()
                                binding.editTextRemarks.text.clear()
                                binding.editTextIr.text.clear()
                                binding.editTextDate.text.clear()
                                // ADDING HISTORY HERE
                                db.collection("history").document(LocalDateTime.now().toString())
                                    .set(hashMapOf(
                                        "cid" to cid,
                                        "timestamp" to LocalDateTime.now().toString()
                                    ))
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    activity,
                                    "Transaction insertion Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("No") { _, _ ->
                        Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                alertBuilder.show()
            }
        }
        return binding.root
    }

}