package com.example.firebasechatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatListener: ListenerRegistration
    private lateinit var adapter: MessagesAdapter
    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMessages)
        val messageField = findViewById<EditText>(R.id.editTextMessage)
        val sendBtn = findViewById<Button>(R.id.btnSend)

        adapter = MessagesAdapter(messagesList, auth.currentUser?.email ?: "Guest")
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        sendBtn.setOnClickListener {
            val msgText = messageField.text.toString()
            if (msgText.isNotEmpty()) {
                val msg = Message(
                    sender = auth.currentUser?.email ?: "Guest",
                    message = msgText
                )
                db.collection("chats").add(msg)
                messageField.text.clear()
            }
        }

        chatListener = db.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messagesList.clear()
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) messagesList.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListener.remove()
    }
}