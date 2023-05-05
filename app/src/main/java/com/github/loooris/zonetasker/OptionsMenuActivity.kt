package com.github.loooris.zonetasker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.appbar.MaterialToolbar

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

    }
}