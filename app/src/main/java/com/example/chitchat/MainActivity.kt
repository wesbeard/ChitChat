package com.example.chitchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import kotlin.concurrent.thread


const val API_KEY = "784ff8a6-1328-42b7-9702-5a11a99bf0e0"
const val CLIENT = "wesley.beard@mymail.champlain.edu"
var client: OkHttpClient = OkHttpClient()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        thread {
            var messages = getMessages(limit = 100)
            print(messages)
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