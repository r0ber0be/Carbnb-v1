package com.example.carbnb.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carbnb.R
import com.example.carbnb.databinding.FragmentSingInBinding
import com.example.carbnb.viewmodel.LoginViewModel
import com.google.android.material.textfield.TextInputEditText

class SingInFragment : Fragment(R.layout.fragment_sing_in) {

    private lateinit var binding : FragmentSingInBinding
    private lateinit var viewModel : LoginViewModel

    private lateinit var email : TextInputEditText
    private lateinit var password : TextInputEditText
    private lateinit var connection : CheckBox
    private lateinit var singinButton : Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSingInBinding.bind(view)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        email = binding.emailTxt
        password = binding.passwordTxt
        connection = binding.connection
        singinButton = binding.singinButton
    }

    override fun onResume() {
        super.onResume()
        singinButton.setOnClickListener{
            if (checkTextFields()){
                val userEmail = email.text.toString()
                val userPassword = password.text.toString()
                viewModel.login(userEmail, userPassword)
                observeLoginResult()
            }
        }
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { authentication ->
            when (authentication) {
                is LoginViewModel.Authentication.Success ->
                    startNextActivity()

                is LoginViewModel.Authentication.Error ->
                    Toast.makeText(requireContext(), authentication.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startNextActivity(){
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.putExtra("user", viewModel.userIn.value)
        Log.d("TAG", "startNextActivity: ${viewModel.userIn.value.toString()}")
        requireActivity().apply {
            startActivity(intent)
            finish()
        }
    }

    private fun checkTextFields(): Boolean{
        if (email.text.isNullOrEmpty() || password.text.isNullOrEmpty()){
            Toast.makeText(requireContext(), "Fill all text fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}