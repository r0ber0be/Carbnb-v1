package com.example.carbnb.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.carbnb.databinding.ActivityProfileBinding
import com.example.carbnb.model.User
import com.example.carbnb.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var username : EditText
    private lateinit var email : EditText
    private lateinit var password : EditText
    private lateinit var profileImage : ImageView
    private lateinit var backButton : ImageView
    private lateinit var saveButton : Button
    private lateinit var deleteButton : Button
    private lateinit var logoutButton: ImageView

    private var changedProfile = false
    private lateinit var userIn : User
    private lateinit var viewModel : ProfileViewModel
    private lateinit var mImageURI : Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        @Suppress("DEPRECATION")
        userIn = intent.getSerializableExtra("user") as User

        backButton = binding.gobackarrow
        profileImage = binding.profileImage
        username = binding.username
        email = binding.email
        password = binding.password
        saveButton = binding.saveChangesButton
        deleteButton = binding.deleteAccountButton
        logoutButton = binding.logoutbutton

        loadData()
    }

    override fun onResume() {
        super.onResume()

        profileImage.setOnClickListener {
            checkPermissionGallery()
        }

        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
            updateData()
            val resultIntent = Intent()
            resultIntent.putExtra("username", username.text.toString())
            resultIntent.putExtra("imageChange", changedProfile)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        deleteButton.setOnClickListener {
            showConfirmationDialog("delete")
        }

        logoutButton.setOnClickListener {
            showConfirmationDialog("logout")
        }
    }

    private fun showConfirmationDialog(op : String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to perform this action?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            val resultIntent = Intent()
            if (op == "delete") {
                viewModel.deleteUser()

                viewModel.opResult.observe(this) {
                    if (it is ProfileViewModel.OpStats.Deleted) {
                        resultIntent.putExtra("logoff", true)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else if (it is ProfileViewModel.OpStats.Error)
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
            else if (op == "logout"){
                resultIntent.putExtra("logoff", true)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateData(){
        viewModel.updateUserData(username.text.toString(), password.text.toString())
        if (::mImageURI.isInitialized && changedProfile){
            viewModel.imageUpdate(mImageURI)
        }
    }
    private fun loadData(){
        username.setText(userIn.name)
        email.setText(userIn.email)
        password.setText(userIn.password)
        if(userIn.profile != null) {
            viewModel.loadImage(userIn.profile!!)
            viewModel.opResult.observe(this){result ->
                when(result){
                    is ProfileViewModel.OpStats.ReceivedImage -> {
                        mImageURI = viewModel.userImage.value!!
                        Picasso.get().load(mImageURI).into(profileImage)
                    }
                    is ProfileViewModel.OpStats.Error -> {
                        Log.d("TAG", "loadData: ${result.message}")
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> return@observe
                }
            }
        }
    }

    private val requestGallery =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (permission) {
                resultGallery.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            } else {
                showDialogPermission()
            }
        }

    private val resultGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data?.data != null) {
                mImageURI = result.data?.data as Uri
                profileImage.setImageURI(mImageURI)
                changedProfile = true
            }
        }

    private fun checkPermissionGallery() {
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

    private fun showDialogPermission() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Request")
            .setMessage("We need permission to access this smartphone gallery")
            .setNegativeButton("Decline") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Accept") { dialog, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

}