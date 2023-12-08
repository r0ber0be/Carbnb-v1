package com.example.carbnb.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.carbnb.databinding.ActivityAdvertiseBinding
import com.example.carbnb.model.Advertise
import com.example.carbnb.viewmodel.AdvertiseViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AdvertiseActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdvertiseBinding

    private lateinit var carImg : ImageView
    private lateinit var carModel : TextInputEditText
    private lateinit var price : TextInputEditText
    private lateinit var description : TextInputEditText
    private lateinit var location : TextInputEditText
    private lateinit var postButton : Button
    private lateinit var goBackArrow : ImageView

    private var createAd = false
    private lateinit var ownerID : String
    private lateinit var advertise: Advertise
    private lateinit var dialog : AlertDialog
    private lateinit var mImageURI : Uri
    private var uLatitude : Double = 0.0
    private var uLongitude : Double = 0.0

    private lateinit var viewModel : AdvertiseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvertiseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AdvertiseViewModel::class.java]

        carImg = binding.carPicture
        carModel = binding.carName
        description = binding.descriptionText
        price = binding.price
        location = binding.locationText
        postButton = binding.confirmButton
        goBackArrow = binding.gobackarrow
        goBackArrow.setOnClickListener { finish() }

        verifyOperation()
    }

    private fun verifyOperation(){
        if (intent.getStringExtra("userID") != null){
            ownerID = intent.getStringExtra("userID") as String
            createAd = true
        }
        else if(@Suppress("DEPRECATION") intent.getSerializableExtra("advertise") != null){
            @Suppress("DEPRECATION")
            advertise = intent.getSerializableExtra("advertise") as Advertise
            loadAdvertiseData()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        carImg.setOnClickListener {
            checkPermissionGallery()
        }

        postButton.setOnClickListener {
            if (createAd){
                lifecycleScope.launch { postAd()}
            }else{
                lifecycleScope.launch { updateAd() }
            }
        }


    }
    private fun loadAdvertiseData(){
        viewModel.loadImage(advertise.carImage)
        viewModel.opResult.observe(this){result ->
            when(result){
                is AdvertiseViewModel.OpStats.ReceivedImage -> {
                    mImageURI = viewModel.carImage.value!!
                    Picasso.get().load(mImageURI).into(carImg)
                }
                is AdvertiseViewModel.OpStats.Error -> {
                    Log.d("TAG", "loadData: ${result.message}")
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                else -> return@observe
            }
        }
        carModel.setText(advertise.model)
        description.setText(advertise.description)
        price.setText(advertise.price)
        location.setText(advertise.location)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateAd(){
        val permissionGranted = requestLocalPermission()
        if (permissionGranted && nullVerifier() && ::mImageURI.isInitialized) {
            viewModel.updateAdvertise(
                advertise.id,
                advertise.carImage,
                carModel.text.toString(),
                price.text.toString(),
                description.text.toString(),
                location.text.toString(),
                mImageURI,
                uLatitude,
                uLongitude
            )
            viewModel.opResult.observe(this@AdvertiseActivity) {
                when (it) {
                    is AdvertiseViewModel.OpStats.PostSuccess -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Toast.makeText(this@AdvertiseActivity, "Success", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is AdvertiseViewModel.OpStats.Error -> {
                        Toast.makeText(this@AdvertiseActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> return@observe
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun postAd(){
            val permissionGranted = requestLocalPermission()
            if (permissionGranted && nullVerifier() && ::mImageURI.isInitialized) {
                viewModel.postAdvertise(
                    carModel.text.toString(),
                    price.text.toString(),
                    description.text.toString(),
                    location.text.toString(),
                    mImageURI,
                    uLatitude,
                    uLongitude
                )
                viewModel.opResult.observe(this@AdvertiseActivity) {
                    when (it) {
                        is AdvertiseViewModel.OpStats.PostSuccess -> {
                            binding.loadingIndicator.visibility = View.GONE
                            Toast.makeText(this@AdvertiseActivity, "Success", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is AdvertiseViewModel.OpStats.Error -> {
                            Toast.makeText(this@AdvertiseActivity, it.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> return@observe
                    }
                }
            }

    }
    private fun checkPermissionGallery(){
        when {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || checkPermission(Manifest.permission.READ_MEDIA_IMAGES) -> {
                resultGallery.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showDialogPermission()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                showDialogPermission()
            }
            else -> {
                requestGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private suspend fun requestLocalPermission(): Boolean {
        return withContext(Dispatchers.Main) {
            if (ActivityCompat.checkSelfPermission(
                    this@AdvertiseActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requestGPS()
                val location = suspendCoroutine { continuation ->
                    binding.loadingIndicator.visibility = View.VISIBLE
                    LocationServices.getFusedLocationProviderClient(this@AdvertiseActivity).lastLocation
                        .addOnSuccessListener { currentLocation ->
                            continuation.resume(currentLocation)
                        }
                        .addOnFailureListener { _ ->
                            continuation.resume(null)
                        }
                }
                if (location != null) {
                    uLatitude = location.latitude
                    uLongitude = location.longitude
                    true
                } else {
                    false
                }
            } else {
                ActivityCompat.requestPermissions(
                    this@AdvertiseActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
                )
                false
            }
        }
    }

    private fun requestGPS(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun checkPermission(permission : String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private val resultGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.data?.data != null) {
                mImageURI = result.data?.data as Uri
                carImg.setImageURI(mImageURI)
            }
        }

    private val requestGallery =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ permission ->
            if (permission){
                resultGallery.launch(
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                )
            }else{
                showDialogPermission()
            }
        }
    private fun showDialogPermission(){
        val builder = AlertDialog.Builder(this)
            .setTitle("Request").setMessage("we need permission to access this smartphone gallery")
            .setNegativeButton("decline") { _,_ -> dialog.dismiss()}
            .setPositiveButton("accept") { _,_ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                dialog.dismiss()
            }
        dialog = builder.create()
        dialog.show()
    }
    private fun nullVerifier(): Boolean {
        val fieldsToCheck = listOf(
            carModel to "Car model",
            price to "Price",
            location to "Location"
        )

        for ((field, message) in fieldsToCheck) {
            if (field.text.isNullOrEmpty()) {
                emptyMessage(message)
                return false
            }
        }
        return true
    } 

    private fun emptyMessage(input : String){
        Toast.makeText(this, "$input is empty", Toast.LENGTH_SHORT).show()
    }
}