package com.example.carbnb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider

class ProfileViewModel : ViewModel(){

    sealed class OpStats {
        data object ReceivedImage : OpStats()
        data object UploadSuccess : OpStats()
        data object Deleted : OpStats()
        data class Error(val message: String) : OpStats()
    }

    private val _opResult = MutableLiveData<OpStats>()
    private val _userImage = MutableLiveData<Uri>()

    val opResult: LiveData<OpStats> get() = _opResult
    val userImage : LiveData<Uri> get() = _userImage


    private val auth = FirebaseAuth.getInstance()
    private val userID = auth.currentUser!!.uid
    private val firebaseUserDoc = FirebaseFirestore.getInstance().collection("Users").document(userID)
    private val imageDatabase = FirebaseStorage.getInstance().getReference("Profiles/")


    fun deleteUser(){
        auth.currentUser!!.delete()
        firebaseUserDoc.delete()
        imageDatabase.child(userID).delete()
        _opResult.value = OpStats.Deleted
    }
    fun updateUserData(name : String, password : String){
        firebaseUserDoc.get().addOnSuccessListener { fireUser ->
            if (fireUser.getString("name") != null && fireUser.getString("name") != name) {
                firebaseUserDoc.update("name", name)
            }

            if (fireUser.getString("password") != password){
                val user = auth.currentUser!!
                user.reauthenticate(EmailAuthProvider.getCredential
                    (fireUser.getString("email")!!, fireUser.getString("password")!!))
                    .addOnSuccessListener {
                        user.reload().addOnSuccessListener {
                            user.updatePassword(password)
                            firebaseUserDoc.update("password", password)
                        }
                    }
            }
        }
    }

    fun imageUpdate(image : Uri){
        imageDatabase.child(userID).putFile(image)
            .addOnSuccessListener {
                firebaseUserDoc.update("profile", userID)
                _opResult.value = OpStats.UploadSuccess
            }.addOnFailureListener {exception ->
                _opResult.value = OpStats.Error(exception.toString())
            }
    }

    fun loadImage(imageId : String){
        imageDatabase.child(imageId).downloadUrl.addOnSuccessListener {
            _userImage.value = it
            _opResult.value = OpStats.ReceivedImage
        }.addOnFailureListener {
            _opResult.value = OpStats.Error(it.toString())
        }
    }
}