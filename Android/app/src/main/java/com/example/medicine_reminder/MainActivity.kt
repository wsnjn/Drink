package com.example.medicine_reminder

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.medicine_reminder.medicine.MedicineListFragment
import com.example.medicine_reminder.notification.NotificationSettingsFragment
import com.example.medicine_reminder.home.HomeFragment
import com.example.medicine_reminder.service.MedicineReminderScheduler
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 恢复保存的主题模式
        restoreThemeMode()

        // 启动通知服务
        startNotificationService()

        setContentView(R.layout.activity_main)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val fragments = listOf(
            HomeFragment(),
            MedicineListFragment(),
            NotificationSettingsFragment()
        )
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
                tab.text = when (pos) {
                    0 -> "首页"
                    1 -> "药品"
                    2 -> "通知"
                    else -> ""
                }
        }.attach()

        // 处理通知点击（在ViewPager初始化后）
        handleNotificationClick(viewPager)
    }
    
    /** 恢复保存的主题模式 */
    private fun restoreThemeMode() {
        try {
            val sharedPrefs = getSharedPreferences("medicine_reminder_settings", android.content.Context.MODE_PRIVATE)
            val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } catch (e: Exception) {
            // 如果出现异常，使用系统默认主题
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /** 启动通知服务 */
    private fun startNotificationService() {
        val serviceIntent = Intent(this, MedicineReminderScheduler::class.java)
        startService(serviceIntent)
    }
    
    /** 处理通知点击 */
    private fun handleNotificationClick(viewPager: ViewPager2) {
        val action = intent.getStringExtra("action")
        if (action == "checkin") {
            val medicineId = intent.getStringExtra("medicine_id")
            val medicineTime = intent.getStringExtra("medicine_time")
            
            if (medicineId != null && medicineTime != null) {
                // 跳转到首页并展开详情
                viewPager.currentItem = 0
                
                // 发送广播通知首页刷新
                val refreshIntent = Intent("com.example.medicine_reminder.CHECKIN_UPDATED")
                sendBroadcast(refreshIntent)
            }
        }
    }
}
