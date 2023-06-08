package com.github.loooris.zonetasker

import MessageFragment
import ReminderFragment
import SettingsFragment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OptionsMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_menu)

        val option = intent.getStringExtra("option")
        if (option != null) {
            when (option) {
                "reminder" -> supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, ReminderFragment()).commit()
                "message" -> supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, MessageFragment()).commit()
                "settings" -> supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, SettingsFragment()).commit()
            }
        }

        // TopAppBar Handling
        val optionsTopAppBar = findViewById<MaterialToolbar>(R.id.OptionsTopAppBar)
        optionsTopAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            finish()
        }

        // DoneFAB Handling
        val doneFab = findViewById<FloatingActionButton>(R.id.DoneFAB)
        doneFab.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}
