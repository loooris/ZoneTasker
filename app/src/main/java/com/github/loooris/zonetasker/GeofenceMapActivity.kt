package com.github.loooris.zonetasker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class GeofenceMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_map)

        val goToMainMenuFAB: ExtendedFloatingActionButton = findViewById(R.id.goToMainMenuFAB)
        goToMainMenuFAB.setOnClickListener { view ->
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }
}
