package com.example.alarmtrial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.alarmtrial.ui.theme.AlarmTrialTheme

class PermissionChecker: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmTrialTheme {
                Surface() {
                    PermissionContent(
                        onPermissionAccepted = {
                            requestPermission()
                        }, onPermissionDeclined = {
                            declinePermission()
                        })
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    private fun declinePermission() {
        Toast.makeText(this, "Permission Declined",Toast.LENGTH_LONG)
        returnToMainActivity()
    }

    private fun returnToMainActivity() {
        val returnHomeIntent = Intent(this, MainActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        startActivity(returnHomeIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission accepted, perform further actions here if needed
                    // In this example, we return to the main activity
                    returnToMainActivity()
                } else {
                    // Permission denied, handle accordingly
                    declinePermission()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001
    }

}

@Composable
fun PermissionContent(
    onPermissionAccepted: () -> Unit,
    onPermissionDeclined: () -> Unit
){

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(52.dp))

            Text(
                text = "Permission Required",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "- SnorLabs requires access to your phone sensors for detecting" +
                        " when you are asleep.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "- No information is stored. " +
                        "The information from your phone sensors are reviewed every 5 minutes " +
                        "and from that, SnorLabs determines whether you are asleep.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onPermissionAccepted
            ) {
                Text(
                    text = "Accept",
                    style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onPermissionDeclined,
                modifier = Modifier
                    .padding(bottom = 36.dp,top = 36.dp)
            ) {
                Text(
                    text = "Decline",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}