package com.ntihs.loaive.ui.chatroom

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.ntihs.loaive.*
import com.ntihs.loaive.ui.chatroom.model.MessageType
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter


class ChatRoomActivity : AppCompatActivity(), View.OnClickListener {


    val TAG = ChatRoomActivity::class.java.simpleName
    var editText: EditText? = null
    var recyclerView: RecyclerView? = null
    lateinit var chatRoomAdapter: ChatRoomAdapter
    lateinit var send: ImageView

    lateinit var mSocket: Socket
    lateinit var partnerName: TextView
    var userName: String = ""
    var roomName: String = ""
    var token: String = ""

    val gson: Gson = Gson()

    //For setting the recyclerView.
    val chatList: ArrayList<Message> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)
        send = findViewById<ImageView>(R.id.send)
        val leave = findViewById<ImageView>(R.id.leave)
        partnerName = findViewById<TextView>(R.id.partnerName)
        editText = findViewById<EditText>(R.id.editText)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        userName = intent.getStringExtra("userName").toString()
        roomName = intent.getStringExtra("roomName").toString()
        token = intent.getStringExtra("token").toString()

        editText!!.isEnabled = false
        leave.setOnClickListener(this)

        //Get the nickname and roomname from entrance activity.
        try {
            userName = intent.getStringExtra("userName")!!
            roomName = intent.getStringExtra("roomName")!!
            token = intent.getStringExtra("token")!!
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //Set Chatroom RecyclerView adapter
        chatRoomAdapter = ChatRoomAdapter(this, chatList)
        recyclerView!!.adapter = chatRoomAdapter

        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager

        //Let's connect to our Chat room! :D
        try {
            //This address is the way you can connect to localhost with AVD(Android Virtual Device)
            mSocket = IO.socket("http://59.127.39.10:3000")
            //Log.d("success", mSocket.id())

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("fail", "Failed to connect")
        }

        mSocket.connect()
        //Register all the listener and callbacks here.
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on("newUserToChatRoom", onNewUser) // To know if the new user entered the room.
        mSocket.on("updateChat", onUpdateChat) // To update if someone send a message to chatroom
        mSocket.on("userLeftChatRoom", onUserLeft) // To know if the user left the chatroom.
    }


    // <----- Callback functions ------->

    var onConnect = Emitter.Listener {
        //After getting a Socket.EVENT_CONNECT which indicate socket has been connected to server,
        //send userName and roomName so that they can join the room.
        val data = InitialData(userName, roomName, token)
        val jsonData = gson.toJson(data) // Gson changes data object to Json type.
        mSocket.emit("subscribe", jsonData)
    }

    var onNewUser = Emitter.Listener {
        val name = it[0] as String //This pass the userName!
        val chat = Message(name, "", roomName, MessageType.USER_JOIN.index)
        addItemToRecyclerView(chat)
        runOnUiThread(Runnable {
            partnerName.text = "${getString(R.string.chat_with)} ${name}"
            send.setOnClickListener(this)
            editText!!.isEnabled = true
        })
        Log.d(TAG, "on New User triggered.")
    }

    var onUserLeft = Emitter.Listener {
        val leftUserName = it[0] as String
        val chat = Message(leftUserName, "", "", MessageType.USER_LEAVE.index)
        addItemToRecyclerView(chat)
        runOnUiThread(Runnable {
            partnerName.text = getString(R.string.chat_dismissed)
            editText!!.isEnabled = false
        })
        dimisssed()
    }

    var onUpdateChat = Emitter.Listener {
        val chat: Message = gson.fromJson(it[0].toString(), Message::class.java)
        chat.viewType = MessageType.CHAT_PARTNER.index
        addItemToRecyclerView(chat)
    }


    private fun sendMessage() {
        val content = editText?.text.toString()
        if(content.isBlank()){
            return
        }
        val sendData = SendMessage(userName, content, roomName)
        val jsonData = gson.toJson(sendData)
        mSocket.emit("newMessage", jsonData)

        val message = Message(userName, content, roomName, MessageType.CHAT_MINE.index)
        addItemToRecyclerView(message)
        runOnUiThread {
            editText?.setText("")
        }
    }

    private fun addItemToRecyclerView(message: Message) {

        //Since this function is inside of the listener,
        //You need to do it on UIThread!
        runOnUiThread {
            chatList.add(message)
            chatRoomAdapter.notifyItemInserted(chatList.size)
            //editText?.setText("")
            recyclerView?.scrollToPosition(chatList.size - 1) //move focus on last message
        }
    }

    private fun dimisssed() {
        val data = InitialData(userName, roomName, token)
        val jsonData = gson.toJson(data)

        //Before disconnecting, send "unsubscribe" event to server so that
        //server can send "userLeftChatRoom" event to other users in chatroom
        mSocket.emit("unsubscribe", jsonData)
        mSocket.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        dimisssed()
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.send -> sendMessage()
            R.id.leave -> onBackPressed()
        }
    }
}