package com.example.carbnb.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.carbnb.databinding.ItemAdvertiseBinding
import com.example.carbnb.model.Advertise
import com.example.carbnb.viewmodel.AdvertiseViewModel
import com.squareup.picasso.Picasso

class AdvertiseAdapter(private val lifecycleOwner: LifecycleOwner,
                       private val advertisesList: MutableList<Advertise>,
                       private val onItemClicked: (Advertise)->Unit):
    RecyclerView.Adapter<AdvertiseAdapter.AdvertiseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertiseViewHolder {
        val advertiseItemView = ItemAdvertiseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdvertiseViewHolder(advertiseItemView)
    }

    override fun onBindViewHolder(holder: AdvertiseViewHolder, position: Int) {
        val advertise = advertisesList[position]
        val viewModel = AdvertiseViewModel()
        var imageURI : Uri
        viewModel.loadImage(advertise.carImage)
        viewModel.opResult.observe(lifecycleOwner){result ->
            when(result){
                is AdvertiseViewModel.OpStats.ReceivedImage -> {
                    imageURI = viewModel.carImage.value!!
                    holder.apply {
                        Picasso.get().load(imageURI).into(carImage)
                        local.text = advertise.location
                        description.text = advertise.description
                        price.text = advertise.price
                        layout.setOnClickListener {
                            onItemClicked(advertise)
                        }
                    }
                }
                is AdvertiseViewModel.OpStats.Error -> {
                    Log.d("TAG", "AdapterBind: ${result.message}")
                }
                else -> return@observe
            }
        }
    }

    override fun getItemCount(): Int {
        return advertisesList.size
    }

    fun updateList(newList: List<Advertise>) {
        advertisesList.clear()
        advertisesList.addAll(newList)
        notifyDataSetChanged()
    }
    class AdvertiseViewHolder(binding: ItemAdvertiseBinding): RecyclerView.ViewHolder(binding.root){
        val carImage = binding.carImage
        val local = binding.localText
        val description = binding.descriptionText
        val price = binding.price
        val layout = binding.layout
    }
}