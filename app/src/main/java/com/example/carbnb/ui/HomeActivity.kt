package com.example.carbnb.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.carbnb.R
import com.example.carbnb.databinding.ActivityHomeBinding
import com.example.carbnb.model.User
import com.example.carbnb.viewmodel.LoginViewModel
import com.example.carbnb.viewmodel.ProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding

    private lateinit var profileImage: ImageView
    private lateinit var profileButton : CardView
    private lateinit var username : TextView
    private lateinit var bottomNav : BottomNavigationView

    private lateinit var userIn : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileButton = binding.profile
        profileImage = binding.profileImage
        username = binding.username
        bottomNav = binding.bottomNav
        initBottomNav()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FeedFragment())
            .commit()

        @Suppress("DEPRECATION")
        userIn = intent.getSerializableExtra("user") as User

    }

    override fun onResume() {
        super.onResume()

        profileButton.setOnClickListener {
            val profileActivity = Intent(this, ProfileActivity::class.java)
            profileActivity.putExtra("user", userIn)
            launcher.launch(profileActivity)
        }

    }

    override fun onStart() {
        super.onStart()
        logUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        ViewModelProvider(this)[LoginViewModel::class.java].logout()
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            val data = it.data
            if (data != null && data.getBooleanExtra("logoff", false)) {
                val logoffIntent = Intent(this, LoginActivity::class.java)
                startActivity(logoffIntent)
                finish()
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            }
            else {
                userIn.name = data!!.getStringExtra("username").toString()
                if (data.getBooleanExtra("imageChange", false))
                    userIn.profile = userIn.id
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

            }
        }
        if (it.resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Changes not applied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logUserData(){
        username.text = userIn.name
        val viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        if(userIn.profile != null) {
            viewModel.loadImage(userIn.profile!!)
            viewModel.opResult.observe(this){result ->
                when(result){
                    is ProfileViewModel.OpStats.ReceivedImage ->
                        Picasso.get().load(viewModel.userImage.value).into(profileImage)

                    is ProfileViewModel.OpStats.Error ->
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()

                    else -> return@observe
                }

            }
        }
    }

    private fun initBottomNav() {
        bottomNav.setOnItemSelectedListener {
            val bundle = Bundle()
            bundle.putString("userID", userIn.id)
            when(it.itemId){
                R.id.feed -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FeedFragment())
                        .commit()
                }
                R.id.history -> {
                    Toast.makeText(this, "works", Toast.LENGTH_SHORT).show()

                }
                R.id.my_advertises -> {
                    val myAdsFragment = MyAdsFragment()
                    myAdsFragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, myAdsFragment)
                        .commit()
                }
            }
            return@setOnItemSelectedListener true
        }
    }
}