package com.example.carbnb.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carbnb.R
import com.example.carbnb.adapters.MyAdvertisesAdapter
import com.example.carbnb.databinding.FragmentMyadsBinding
import com.example.carbnb.model.Advertise
import com.example.carbnb.viewmodel.AdvertiseViewModel
import kotlinx.coroutines.launch

class MyAdsFragment : Fragment(R.layout.fragment_myads) {

    private lateinit var binding : FragmentMyadsBinding
    private lateinit var viewModel: AdvertiseViewModel

    private lateinit var recyclerView : RecyclerView
    private lateinit var createButton : Button

    private lateinit var userID : String
    private lateinit var myAdsAdapter : MyAdvertisesAdapter
    private val emptyList :MutableList<Advertise> = ArrayList<Advertise>().toMutableList()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userID = arguments?.getString("userID")!!
        binding = FragmentMyadsBinding.bind(view)
        viewModel = ViewModelProvider(this)[AdvertiseViewModel::class.java]

        recyclerView = binding.recyclerViewAdvertises
        createButton = binding.createButton

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        myAdsAdapter = MyAdvertisesAdapter(this, emptyList) {
            when (it.messages?.get(it.messages!!.lastIndex)) {
                "delete" -> {
                    viewModel.deleteAdvertise(it.id, it.carImage)
                    viewModel.opResult.observe(viewLifecycleOwner){opStats ->
                        when(opStats){
                            is AdvertiseViewModel.OpStats.Deleted -> {
                                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                                lifecycleScope.launch { loadAdsList() }
                            }
                            else -> Log.d("TAG", "Delete response")
                        }
                    }
                }
                "view" -> {
                    val intent = Intent(requireContext(), MessagesActivity::class.java)
                    intent.putExtra("advertiseID", it.id)
                    startActivity(intent)
                }
                "edit" -> {
                    val intent = Intent(requireContext(), AdvertiseActivity::class.java)
                    intent.putExtra("advertise", it)
                    startActivity(intent)
                }
            }
        }

        recyclerView.adapter = myAdsAdapter
    }
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch { loadAdsList() }

        createButton.setOnClickListener {
            val intent = Intent(requireContext(), AdvertiseActivity::class.java)
            intent.putExtra("userID", userID)
            startActivity(intent)
        }
    }

    private suspend fun loadAdsList(){
        viewModel.loadMyAdvertisesList()
        viewModel.opResult.observe(viewLifecycleOwner) {opStats ->
            when(opStats){
                is AdvertiseViewModel.OpStats.AdvertisesList ->
                    myAdsAdapter.updateList(opStats.advertises)
                else -> Log.d("TAG", "loadAdsList")
            }
        }
    }
}