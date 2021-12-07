package com.example.chitchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import kotlin.concurrent.thread


const val API_KEY = "784ff8a6-1328-42b7-9702-5a11a99bf0e0"
const val CLIENT = "wesley.beard@mymail.champlain.edu"
var client: OkHttpClient = OkHttpClient()

class MainActivity : AppCompatActivity() {

    private lateinit var messagesRecycler: RecyclerView
    private lateinit var sendButton: ImageButton
    private lateinit var messageBox: EditText

    lateinit var messages: List<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        title = "ChitChat"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loadingThread = Thread {
            var rawMessages = getMessages(limit = 3)
            val gson = Gson()
            val deserializedMessages = gson.fromJson(rawMessages, Messages::class.java)

            messages = deserializedMessages.messages
        }

        loadingThread.start() // spawn thread
        loadingThread.join() // wait for thread to finish

        messagesRecycler = findViewById(R.id.messages_recycler)
        messagesRecycler.layoutManager = GridLayoutManager(this@MainActivity,1)
        messagesRecycler.adapter = RecyclerAdapter()
    }

    inner class MessageViewHolder(messageView: View) : RecyclerView.ViewHolder(messageView) {
        lateinit var message: Message
        private val messageView: TextView = messageView.findViewById(R.id.message_display)

        init {
            message = messages[position]
            this.messageView.text = message.message
        }

        fun initIndex(position: Int) {
            message = messages[position]
        }
    }

    inner class RecyclerAdapter : RecyclerView.Adapter<MessageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(parent.context)
            val cellView: View = inflater.inflate(R.layout.message, parent, false)
            return MessageViewHolder(cellView)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.initIndex(position)

            // Operations to perform once the last cell is created
//            if (position == grid.totalCells - 1) {
//
//                // Set grid to intent extra if it exists
//                if (intent.hasExtra("grid")) {
//                    val json = intent.getStringExtra("grid")
//                    if (json != null) {
//                        grid.setFromJson(json, this@MainActivity)
//                    }
//                }
//
//                // Separate thread for calculating neighbors of each cell
//                thread {
//                    for (cell in grid.cells.values) {
//                        cell.calculateNeighbors(grid)
//                    }
//                }
//            }
        }

        override fun getItemCount(): Int {
            return 1
        }
    }
}

fun getMessages(skip: Int = 0, limit: Int = 10): String {

        val getUrl = HttpUrl.Builder()
                .scheme("https")
                .host("stepoutnyc.com")
                .addPathSegment("chitchat")
                .addQueryParameter("key", API_KEY)
                .addQueryParameter("client", CLIENT)
                .addQueryParameter("skip", skip.toString())
                .addQueryParameter("limit", limit.toString())
                .build()

        return getRequest(getUrl)
}

fun getRequest(url: HttpUrl): String {
    val request: Request = Request.Builder()
            .url(url)
            .build()

    val call = client.newCall(request)
    val response = call.execute()
    return response.body!!.string()
}

fun sendMessage(message: String) {

}

fun postRequest(url: String): Response {
    val request: Request = Request.Builder()
            .url(url)
            .build()

    val call = client.newCall(request)
    return call.execute()
}