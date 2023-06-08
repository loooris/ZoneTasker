package com.github.loooris.zonetasker

import MessageFragment
import ReminderFragment
import SettingsFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar

class OptionsMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_menu)

        val selectedOption = intent.getStringExtra("option")

        val messageFragment = MessageFragment()
        val reminderFragment = ReminderFragment()
        val settingsFragment = SettingsFragment()

        when (selectedOption) {
            "message" -> setCurrentFragment(messageFragment)
            "reminder" -> setCurrentFragment(reminderFragment)
            "settings" -> setCurrentFragment(settingsFragment)
        }

        // TopAppBar Handling
        val optionsTopAppBar = findViewById<MaterialToolbar>(R.id.OptionsTopAppBar)
        optionsTopAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            finish()
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
    }
}
