package com.example.chitchat

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import androidx.recyclerview.widget.LinearLayoutManager

const val API_KEY = "784ff8a6-1328-42b7-9702-5a11a99bf0e0"
const val CLIENT = "wesley.beard@mymail.champlain.edu"
const val MESSAGES_PER_CALL = 20
var client: OkHttpClient = OkHttpClient()

class MainActivity : AppCompatActivity() {

    private lateinit var messagesRecycler: RecyclerView
    private lateinit var sendButton: ImageButton
    private lateinit var messageBox: EditText
    var messages = mutableListOf<Message>()
    var numMessages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        title = "Chit Chat"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loadingThread = Thread {
            getMessages()
        }

        loadingThread.start()
        // Only create recycler when messages are done loading
        loadingThread.join()

        messagesRecycler = findViewById(R.id.messages_recycler)
        messagesRecycler.adapter = RecyclerAdapter()

        messageBox = findViewById(R.id.message)
        sendButton = findViewById(R.id.send)
        sendButton.setOnClickListener {
            val sendThread = Thread {
                sendMessage(messageBox.text.toString())
                getSent()
            }
            sendThread.start()
            sendThread.join()
            messagesRecycler.adapter?.notifyItemInserted(0)
            messagesRecycler.smoothScrollToPosition(0)
        }

        // Set layout manager to reverse linear layout
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        messagesRecycler.layoutManager = linearLayoutManager
    }

    abstract class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun initialize(position: Int)
    }

    inner class ButtonViewHolder(buttonView: View) : ItemViewHolder(buttonView) {
        private val button: Button = buttonView.findViewById(R.id.load_button)

        override fun initialize(position: Int) {
            button.setOnClickListener {
                val loadingThread = Thread {
                    loadMore()
                }
                loadingThread.start()
                loadingThread.join()
                messagesRecycler.adapter?.notifyDataSetChanged()
            }
        }
    }

    inner class MessageViewHolder(messageView: View) : ItemViewHolder(messageView) {

        private lateinit var messageData: Message
        private val message: TextView = messageView.findViewById(R.id.message_display)
        private val sender: TextView = messageView.findViewById(R.id.sender_display)
        private val layout: LinearLayout = messageView.findViewById(R.id.message_layout)

        override fun initialize(position: Int) {
            messageData = messages[position]
            this.message.text = messageData.message
            this.sender.text = messageData.client.split("@")[0]

            layout.gravity = if (messageData.client == CLIENT) {
                message.setBackgroundResource(R.drawable.outgoing_message)
                message.setTextColor(resources.getColor(R.color.light))
                Gravity.RIGHT
            }
            else {
                // I don't really feel like I should need this else but it bugs out with it so idk
                message.setBackgroundResource(R.drawable.incoming_message)
                message.setTextColor(resources.getColor(R.color.dark))
                Gravity.LEFT
            }
        }
    }

    inner class RecyclerAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            return if(viewType == R.layout.message){
                MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message, parent, false))
            } else {
                ButtonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.load_button, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == numMessages - 1) R.layout.load_button else R.layout.message
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.initialize(position)
        }

        override fun getItemCount(): Int {
            return numMessages
        }
    }

    private fun getMessages() {
        var rawMessages = getRequest(limit = MESSAGES_PER_CALL)
        val gson = Gson()
        val deserializedMessages = gson.fromJson(rawMessages, Messages::class.java)
        messages.addAll(deserializedMessages.messages)
        numMessages += MESSAGES_PER_CALL
    }

    private fun getSent() {
        var rawMessages = getRequest(limit = 1)
        val gson = Gson()
        val deserializedMessages = gson.fromJson(rawMessages, Messages::class.java)
        messages.add(0, deserializedMessages.messages[0])
        numMessages++
    }

    private fun loadMore() {
        var rawMessages = getRequest(limit = MESSAGES_PER_CALL, skip = numMessages)
        val gson = Gson()
        val deserializedMessages = gson.fromJson(rawMessages, Messages::class.java)
        messages.addAll(deserializedMessages.messages)
        numMessages += MESSAGES_PER_CALL
    }

    private fun getRequest(skip: Int = 0, limit: Int = 10): String {
        val getUrl = HttpUrl.Builder()
            .scheme("https")
            .host("stepoutnyc.com")
            .addPathSegment("chitchat")
            .addQueryParameter("key", API_KEY)
            .addQueryParameter("client", CLIENT)
            .addQueryParameter("skip", skip.toString())
            .addQueryParameter("limit", limit.toString())
            .build()

        val request: Request = Request.Builder()
            .url(getUrl)
            .build()

        val call = client.newCall(request)
        val response = call.execute()
        return response.body!!.string()
    }

    private fun sendMessage(message: String) {
        postRequest(message)
    }

    private fun postRequest(message: String): Response {

        val formBody = FormBody.Builder()
            .add("key", API_KEY)
            .add("client", CLIENT)
            .add("message", message)
            .build()

        val request = Request.Builder()
            .url("https://stepoutnyc.com/chitchat")
            .post(formBody)
            .build()

        val call = client.newCall(request)
        return call.execute()
    }
}

