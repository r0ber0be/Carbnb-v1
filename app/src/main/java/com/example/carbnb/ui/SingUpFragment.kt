package com.example.carbnb.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carbnb.R
import com.example.carbnb.databinding.FragmentSingUpBinding
import com.example.carbnb.viewmodel.LoginViewModel
import com.google.android.material.textfield.TextInputEditText

class SingUpFragment : Fragment(R.layout.fragment_sing_up) {

    private lateinit var binding: FragmentSingUpBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var name : TextInputEditText
    private lateinit var email : TextInputEditText
    private lateinit var password : TextInputEditText
    private lateinit var thermsConditions: CheckBox
    private lateinit var singUpButton : Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSingUpBinding.bind(view)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        name = binding.nameTxt
        email = binding.emailTxt
        password = binding.passwordTxt
        thermsConditions = binding.thermsConditions
        singUpButton = binding.singupButton

    }

    override fun onResume() {
        super.onResume()
        singUpButton.setOnClickListener{
            if (nullVerifier()){
                val userEmail = email.text.toString()
                val userPassword = password.text.toString()
                val username = name.text.toString()

                viewModel.singUp(username, userEmail, userPassword)
                observeSingUpResult()
            }
        }
    }

    private fun observeSingUpResult(){
        viewModel.singUpResult.observe(viewLifecycleOwner){authentication ->
            when (authentication){
                is LoginViewModel.Authentication.Success -> toastMessage("Success")
                is LoginViewModel.Authentication.Error -> toastMessage(authentication.message)
            }
        }
    }

    private fun nullVerifier() : Boolean{
        val fields = listOf(name, email, password)

        for (field in fields) {
            if (field.text.isNullOrEmpty()) {
                toastMessage(field.hint.toString()+" is empty")
                return false
            }
        }

        if (!thermsConditions.isChecked) {
            toastMessage("Please check Terms and Conditions")
            return false
        }

        return true
    }

    private fun toastMessage(input : String){
        Toast.makeText(requireContext(), input, Toast.LENGTH_SHORT).show()
    }

    private fun clearCamps(){
        name.setText("")
        email.setText("")
        password.setText("")
    }
}