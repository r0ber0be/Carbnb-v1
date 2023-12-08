package com.example.carbnb.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.carbnb.adapters.PageAdapter
import com.example.carbnb.dao.UsersDataSource
import com.example.carbnb.databinding.ActivityLoginBinding
import com.example.carbnb.model.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginActivity : AppCompatActivity() {

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager2
    private lateinit var binding : ActivityLoginBinding

    private lateinit var usersList : MutableList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        usersList = UsersDataSource.createUsersList()
        configTabLayout()

    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun configTabLayout(){
        val adapter = PageAdapter(this)
        viewPager.adapter = adapter

        val singInFragment = SingInFragment()
        val singUpFragment = SingUpFragment()

        adapter.addFragment(singInFragment, "Sing In")
        adapter.addFragment(singUpFragment, "Sing Up")

        viewPager.offscreenPageLimit = adapter.itemCount
        val mediator = TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = adapter.getTitle(position)
        }
        mediator.attach()
    }
}