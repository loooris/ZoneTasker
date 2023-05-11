package com.github.loooris.zonetasker

import MessageFragment
import ReminderFragment
import SettingsFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class OptionsMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_menu)

        val messageFragment = MessageFragment()
        val reminderFragment = ReminderFragment()
        val settingsFragment = SettingsFragment()
        setCurrentFragment(messageFragment)


        // TopAppBar Handling
        val OptionsTopAppBar = findViewById<MaterialToolbar>(R.id.OptionsTopAppBar)
        OptionsTopAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            finish()
        }

//        todo (may be useful)
//        OptionsTopAppBar.setOnMenuItemClickListener { menuItem ->
//            when (menuItem.itemId) {
//
//                R.id.more -> {
//                    // Handle more item (inside overflow menu) press
//                    true
//                }
//                else -> false
//            }
//        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.messageItem -> {
                    // Respond to navigation messageItem click
                    setCurrentFragment(messageFragment)
                    true
                }
                R.id.reminderItem -> {
                    // Respond to navigation reminderItem click
                    setCurrentFragment(reminderFragment)
                    true
                }
                R.id.settingsItem -> {
                    // Respond to navigation settingsItem click
                    setCurrentFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }


    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

}

