package com.example.carbnb.viewmodel

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carbnb.model.Advertise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AdvertiseViewModel : ViewModel() {
    sealed class OpStats {
        data object ReceivedImage : OpStats()
        data object PostSuccess : OpStats()
        data class AdvertisesList(val advertises : ArrayList<Advertise>) : OpStats()
        data object Deleted : OpStats()
        data class Error(val message: String) : OpStats()
    }

    private val _opResult = MutableLiveData<OpStats>()
    private val _carImage = MutableLiveData<Uri>()
    private val _fAdvertise = MutableLiveData<Advertise>()
    val opResult: LiveData<OpStats> get() = _opResult
    val carImage : LiveData<Uri> get() = _carImage
    val fAdvertise : LiveData<Advertise> get() = _fAdvertise

    private val auth = FirebaseAuth.getInstance()
    private val userID = auth.currentUser!!.uid
    private val firebaseAdvertises = FirebaseFirestore.getInstance().collection("Advertises")
    private val imageDatabase = FirebaseStorage.getInstance().getReference("Cars/")

    suspend fun loadMyAdvertisesList(){
        val allAdvertises = ArrayList<Advertise>()
        val querySnapshot = firebaseAdvertises.whereEqualTo("ownerID", userID).get().await()
        if (querySnapshot != null){
            Log.d("TAG", "NotNullList: $querySnapshot list size ${querySnapshot.documents.size}")
            for (document in querySnapshot.documents){
                Log.d("TAG", "Document: ${document.id}")
                allAdvertises.add(
                    Advertise(
                        document.id,
                        document.data!!["ownerId"].toString(),
                        document.data!!["date"].toString(),
                        document.data!!["model"].toString(),
                        document.data!!["price"].toString(),
                        document.data!!["location"].toString(),
                        document.data!!["latitude"] as Double,
                        document.data!!["longitude"] as Double,
                        document.data!!["description"].toString(),
                        document.data!!["carImage"].toString(),
                        null
                    )
                )
            }
            _opResult.value = OpStats.AdvertisesList(allAdvertises)
        }
    }
    suspend fun loadAdvertisesList(){
        val allAdvertises = ArrayList<Advertise>()
        val querySnapshot = firebaseAdvertises.whereNotEqualTo("ownerID", userID).get().await()
        if (querySnapshot != null){
            for (document in querySnapshot.documents){
                allAdvertises.add(
                    Advertise(
                    document.id,
                    document.data!!["ownerId"].toString(),
                    document.data!!["date"].toString(),
                    document.data!!["model"].toString(),
                    document.data!!["price"].toString(),
                    document.data!!["location"].toString(),
                    document.data!!["latitude"] as Double,
                    document.data!!["longitude"] as Double,
                    document.data!!["description"].toString(),
                    document.data!!["carImage"].toString(),
                    null
                    )
                )
            }
            _opResult.value = OpStats.AdvertisesList(allAdvertises)
        }
    }
    fun deleteAdvertise(id : String, imageId: String){
        firebaseAdvertises.document(id).delete().addOnSuccessListener {
            imageDatabase.child(imageId).delete().addOnSuccessListener {
                _opResult.value = OpStats.Deleted
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAdvertise(adId: String, imageID : String, model : String, price : String, description : String, location : String, image: Uri, latitude: Double, longitude: Double){
        val date = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val advertiseMap : MutableMap<String, Any?> = hashMapOf(
            "model" to model,
            "price" to price,
            "description" to description,
            "date" to date,
            "location" to location,
            "latitude" to latitude,
            "longitude" to longitude
        ).toMutableMap()

        firebaseAdvertises.document(adId).update(advertiseMap).addOnSuccessListener {
            postImage(imageID, image)
        }.addOnFailureListener {
            _opResult.value = OpStats.Error(it.message.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun postAdvertise(model : String, price : String, description : String, location : String, image: Uri, latitude: Double, longitude: Double){
        val date = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val advertiseID = UUID.randomUUID().toString()

        val advertiseMap = hashMapOf(
            "ownerID" to userID,
            "carImage" to advertiseID,
            "model" to model,
            "price" to price,
            "description" to description,
            "date" to date,
            "location" to location,
            "messages" to null,
            "latitude" to latitude,
            "longitude" to longitude
        )
        firebaseAdvertises.add(advertiseMap).addOnSuccessListener {
            postImage(advertiseID, image)
        }.addOnFailureListener {
            _opResult.value = OpStats.Error(it.message.toString())
        }
    }
    private fun postImage(advertiseID : String, image : Uri){
        imageDatabase.child(advertiseID).putFile(image)
            .addOnSuccessListener {
                firebaseAdvertises.document().update("carImage", advertiseID)
                _opResult.value = OpStats.PostSuccess
            }.addOnFailureListener {exception ->
                _opResult.value = OpStats.Error(exception.toString())
            }
    }

    fun loadAdvertise(id : String){
        firebaseAdvertises.document(id).get().addOnSuccessListener {document ->
            if (document.data != null) {
                _fAdvertise.value = Advertise(
                    document.id,
                    document.data!!["ownerID"].toString(),
                    document.data!!["date"].toString(),
                    document.data!!["model"].toString(),
                    document.data!!["price"].toString(),
                    document.data!!["location"].toString(),
                    document.data!!["latitude"] as Double,
                    document.data!!["longitude"] as Double,
                    document.data!!["description"].toString(),
                    document.data!!["carImage"].toString(),
                    null
                    )
                loadImage(document.data!!["carImage"] as String)
            }
        }
    }
    fun loadImage(imageId : String){
        imageDatabase.child(imageId).downloadUrl.addOnSuccessListener {
            _carImage.value = it
            _opResult.value = OpStats.ReceivedImage
        }.addOnFailureListener {
            _opResult.value = OpStats.Error(it.toString())
        }
    }
}