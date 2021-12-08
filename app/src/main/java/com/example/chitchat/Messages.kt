package com.example.chitchat

import com.google.gson.annotations.SerializedName

data class Messages(
    @SerializedName("count") var count: Int,
    @SerializedName("messages") var messages: List<Message>
)

// {"_id":"61afa37a7172481c519d8bf3","client":"jacob.capra@mymail.champlain.edu",
// "date":"Tue, 07 Dec 2021 18:10:02 GMT","dislikes":1,"ip":"184.171.151.148",
// "likes":0,"loc":[null,null],"message":"another test"}
data class Message(
    @SerializedName("_id") var id: String,
    @SerializedName("client") var client: String,
    @SerializedName("date") var date: String,
    @SerializedName("dislikes") var dislikes: Int,
    @SerializedName("ip") var ip: String,
    @SerializedName("likes") var likes: Int,
    // @SerializedName("loc") var loc: String,
    @SerializedName("message") var message: String
)



