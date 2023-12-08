package com.example.carbnb.adapters

import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carbnb.databinding.ItemMessageBinding
import com.example.carbnb.model.Message

class MessageAdapter(private val messageList: MutableList<Message>):
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val messageItemView = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(messageItemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.apply {
            author.text = message.authorName
            date.text = message.dateTime
            messageView.text = message.content
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class MessageViewHolder(binding: ItemMessageBinding): RecyclerView.ViewHolder(binding.root){
        val author = binding.authorName
        val date = binding.date
        val messageView = binding.messageText
    }
}