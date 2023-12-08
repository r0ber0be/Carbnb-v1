package com.example.carbnb.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.carbnb.databinding.ActivityScheduleBinding
import com.example.carbnb.model.Advertise
import com.example.carbnb.viewmodel.AdvertiseViewModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SchedulingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityScheduleBinding
    private lateinit var viewModel : AdvertiseViewModel

    private lateinit var backButton : ImageView
    private lateinit var carName : TextView
    private lateinit var carPicture : ImageView
    private lateinit var mapsButton : Button
    private lateinit var locationText : TextView
    private lateinit var description : TextView
    private lateinit var owner : TextView
    private lateinit var dateButton : Button
    private lateinit var userMessage : EditText
    private lateinit var confirmButton : Button
    private lateinit var cancelButton : Button

    private lateinit var advertiseID : String
    private lateinit var advertise: Advertise
    private var adLatitude : Double = 0.0
    private var adLongitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        advertiseID = intent.getStringExtra("advertiseID").toString()
        Log.d("TAG", "onCreate: $advertiseID")

        backButton = binding.gobackarrow
        carName = binding.carName
        carPicture = binding.carPicture
        mapsButton = binding.mapsButton
        locationText = binding.location
        description = binding.descriptionText
        owner = binding.owner
        dateButton = binding.calendarButton
        userMessage = binding.userMessage
        confirmButton = binding.confirmButton
        cancelButton = binding.cancelButton

        viewModel = ViewModelProvider(this)[AdvertiseViewModel::class.java]
        loadData()
    }

    private fun loadData(){
        viewModel.loadAdvertise(advertiseID)
        var mImageURI : Uri

        viewModel.opResult.observe(this){result ->
            when(result){
                is AdvertiseViewModel.OpStats.ReceivedImage -> {
                    mImageURI = viewModel.carImage.value!!
                    Picasso.get().load(mImageURI).into(carPicture)
                    advertise = viewModel.fAdvertise.value!!
                    carName.text = advertise.model
                    locationText.text = advertise.location
                    description.text = advertise.description
                    owner.text = advertise.ownerId
                    adLatitude = advertise.latitude
                    adLongitude = advertise.longitude
                }
                is AdvertiseViewModel.OpStats.Error -> {
                    Log.d("TAG", "loadData: ${result.message}")
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                else -> return@observe
            }
        }
    }
    override fun onResume() {
        super.onResume()
        backButton.setOnClickListener { finish() }

        mapsButton.setOnClickListener {
            requestLocalPermissions()
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ){
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("lat", adLatitude)
                intent.putExtra("long", adLongitude)
                Log.d("TAG", "Starting Map LatLng: [$adLongitude, $adLongitude]")
                startActivity(intent)
            }

        }
        confirmButton.setOnClickListener {
            Toast.makeText(this, "Confirmed", Toast.LENGTH_SHORT).show()
            finish()
        }
        cancelButton.setOnClickListener { finish() }
        dateButton.setOnClickListener{
            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)

                    dateButton.text = formattedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

            datePickerDialog.show()
        }
    }

    private fun requestLocalPermissions(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
            )
        }
    }

}