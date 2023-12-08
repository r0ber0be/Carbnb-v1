package com.example.carbnb.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.carbnb.databinding.ItemMyAdvertiseBinding
import com.example.carbnb.model.Advertise
import com.example.carbnb.viewmodel.AdvertiseViewModel
import com.squareup.picasso.Picasso


class MyAdvertisesAdapter(private val lifecycleOwner: LifecycleOwner, private val advertisesList: MutableList<Advertise>, private val onItemClicked : (Advertise)->Unit) :
    RecyclerView.Adapter<MyAdvertisesAdapter.AdvertiseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertiseViewHolder {
        val advertiseItemView = ItemMyAdvertiseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdvertiseViewHolder(advertiseItemView)
    }

    override fun onBindViewHolder(holder: AdvertiseViewHolder, position: Int) {
        val advertise = advertisesList[position]
        val viewModel = AdvertiseViewModel()
        advertise.messages = ArrayList<String>().toMutableList()
        var imageURI : Uri

        viewModel.loadImage(advertise.carImage)
        viewModel.opResult.observe(lifecycleOwner){result ->
            when(result){
                is AdvertiseViewModel.OpStats.ReceivedImage -> {
                    imageURI = viewModel.carImage.value!!
                    holder.apply {
                        Picasso.get().load(imageURI).into(carImage)
                        carName.text = advertise.model
                        local.text = advertise.location
                        description.text = advertise.description
                        price.text = advertise.price
                        date.text = advertise.date
                        editButton.setOnClickListener {
                            advertise.messages!!.add("edit")
                            onItemClicked(advertise)
                        }
                        viewButton.setOnClickListener {
                            advertise.messages!!.add("view")
                            onItemClicked(advertise)
                        }
                        deleteButton.setOnClickListener {
                            advertise.messages!!.add("delete")
                            advertisesList.removeAt(position)
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
    class AdvertiseViewHolder(binding: ItemMyAdvertiseBinding): RecyclerView.ViewHolder(binding.root){
        val carImage = binding.carImage
        val carName = binding.carName
        val local = binding.location
        val description = binding.descriptionText
        val price = binding.price
        val date = binding.date
        val editButton = binding.editButton
        val viewButton = binding.viewButton
        val deleteButton = binding.deleteButton
    }
}