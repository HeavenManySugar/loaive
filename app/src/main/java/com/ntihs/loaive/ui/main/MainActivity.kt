package com.ntihs.loaive.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.ntihs.loaive.R






class MainActivity : AppCompatActivity() {
    companion object{
        var userName: String = ""
        var token: String = ""
    }
    private var frameLayout: FrameLayout? = null
    private var radioGroup: RadioGroup? = null
    private lateinit var mFragments: Array<Fragment>
    private lateinit var mButtons: Array<Button>
    private var mIndex = 0
    private val image: Array<String> = arrayOf<String>("home", "message", "notice", "setting")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
        initFragment()
        setRadioGroupListener()

        //Get the userName and token from entrance activity.
        try {
            userName = intent.getStringExtra("userName")!!
            token = intent.getStringExtra("token")!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("token", token)
    }

    private fun initFragment() {
        radioGroup = findViewById<View>(R.id.radioGroup) as RadioGroup
        frameLayout = findViewById<View>(R.id.fragmentContainerView) as FrameLayout
        val homeFragment = HomeFragment()
        val messageFragment = MessageFragment()
        val noticeFragment = NoticeFragment()
        val settingFragment = SettingFragment()
        //新增到陣列
        mFragments = arrayOf<Fragment>(
            homeFragment,
            messageFragment,
            noticeFragment,
            settingFragment
        )
        mButtons = arrayOf<Button>(
            findViewById<Button>(R.id.home_button),
            findViewById<Button>(R.id.message_button),
            findViewById<Button>(R.id.notice_button),
            findViewById<Button>(R.id.setting_button)
        )
        //開啟事務
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        //新增首頁
        ft.add(R.id.fragmentContainerView, homeFragment).commit()
        //預設設定為第0個
        setIndexSelected(0)
    }

    private fun setIndexSelected(index: Int) {
        if (mIndex == index) {
            return
        }
        val fragmentManager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = fragmentManager.beginTransaction()
        //隱藏
        ft.hide(mFragments[mIndex])
        //判斷是否新增
        if (!mFragments[index].isAdded()) {
            ft.add(R.id.fragmentContainerView, mFragments[index]).show(mFragments[index])
        } else {
            ft.show(mFragments[index])
        }
        ft.commit()
        clearButton()
        val top = resources.getIdentifier("select_${image[index]}", "drawable", packageName)
        mButtons[index].setCompoundDrawablesWithIntrinsicBounds(0, top, 0, 0)
        //再次賦值
        mIndex = index
    }

    private fun setRadioGroupListener() {
        radioGroup!!.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.home_button -> setIndexSelected(0)
                R.id.message_button -> setIndexSelected(1)
                R.id.notice_button -> setIndexSelected(2)
                R.id.setting_button -> setIndexSelected(3)
                else -> setIndexSelected(0)
            }
        }
    }

    private fun clearButton() {
        for(button in mButtons){
            val index = mButtons.indexOf(button)
            val top = resources.getIdentifier(image[index], "drawable", packageName)
            mButtons[index].setCompoundDrawablesWithIntrinsicBounds(0, top, 0, 0)
        }
    }
/*
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //僅當activity為task根（即首個啟動activity）時才生效,這個方法不會改變task中的activity狀態，
            // 按下返回鍵的作用跟按下HOME效果一樣；重新點選應用還是回到應用退出前的狀態；
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
 */

}