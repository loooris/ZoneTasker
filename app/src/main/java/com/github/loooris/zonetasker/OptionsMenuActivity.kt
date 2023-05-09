package com.github.loooris.zonetasker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationBarView

class OptionsMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_menu)


        // TopAppBar Handling
        val OptionsTopAppBar = findViewById<MaterialToolbar>(R.id.OptionsTopAppBar)
        OptionsTopAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            finish()
        }

//        todo may be useful
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

        NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.messageItem -> {
                    // Respond to navigation messageItem click
                    true
                }
                R.id.reminderItem -> {
                    // Respond to navigation reminderItem click
                    true
                }
                R.id.settingsItem -> {
                    // Respond to navigation settingsItem click
                    true
                }
                else -> false
            }
        }

    }
}