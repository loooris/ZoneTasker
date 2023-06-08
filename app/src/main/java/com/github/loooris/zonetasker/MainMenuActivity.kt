package com.github.loooris.zonetasker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainMenuActivity : AppCompatActivity() {

    companion object {
        var option = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val intent = Intent(this, OptionsMenuActivity::class.java)


        val buttonClickMessage = findViewById<Button>(R.id.MessageButton)
        buttonClickMessage.setOnClickListener { intent.putExtra("option", "message")
            option = "message"
            startActivity(intent)
        }

        val buttonClickReminder = findViewById<Button>(R.id.ReminderButton)
        buttonClickReminder.setOnClickListener { intent.putExtra("option", "reminder")
            option = "reminder"
            startActivity(intent)
        }

        val buttonClickSettings = findViewById<Button>(R.id.SettingsButton)
        buttonClickSettings.setOnClickListener {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}