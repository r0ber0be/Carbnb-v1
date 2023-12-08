package com.example.carbnb.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carbnb.adapters.MessageAdapter
import com.example.carbnb.databinding.ActivityMessagesBinding
import com.example.carbnb.model.Message

class MessagesActivity : AppCompatActivity(){

    private lateinit var binding : ActivityMessagesBinding

    private lateinit var backButton : ImageView
    private lateinit var sendButton : Button
    private lateinit var messageText : EditText
    private lateinit var recyclerView: RecyclerView

    private val dbMessage = ArrayList<Message>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbMessage.add(
            Message(
                "1",
                "123",
                "John Doe",
                "2023-11-02 14:30:00",
                "Hello, this is a sample message."
            )
        )

        backButton = binding.gobackarrow
        sendButton = binding.sendButtom
        messageText = binding.messageBox
        recyclerView = binding.messageRecyclerView
    }

    override fun onResume() {
        super.onResume()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MessageAdapter(dbMessage)

        sendButton.setOnClickListener {
            Toast.makeText(this, "${messageText.text}", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}