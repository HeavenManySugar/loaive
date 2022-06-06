package com.ntihs.loaive.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ntihs.loaive.R
import com.ntihs.loaive.ui.chatroom.ChatRoomActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [home.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val matching = view.findViewById<ImageView>(R.id.matching)
        matching.setOnClickListener{
            enterChatroom(MainActivity.userName, "room", MainActivity.token)
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun enterChatroom(userName: String = "default", roomName: String = "lobby", token: String  = ""){
        if(!roomName.isNullOrBlank()&&!userName.isNullOrBlank()) {
            val intent = Intent(activity, ChatRoomActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("roomName", roomName)
            intent.putExtra("token", token)
            startActivity(intent)
        }else{
            Toast.makeText(activity,"Nickname and Roomname should be filled!",Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}