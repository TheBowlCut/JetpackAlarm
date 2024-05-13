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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionChecker: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionContent(onPermissionAccepted = {
                requestPermission()
            }, onPermissionDeclined = {
                declinePermission()
            })
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
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Permission Activity")

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = onPermissionAccepted) {
                Text(text = "Accept")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = onPermissionDeclined) {
                Text(text = "Decline")
            }
        }
    }
}



/*
@Composable
fun PermissionContent(activity: ComponentActivity) {
    Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        , contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Text(text = "Permission Activity")

            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier.weight(1f))

            // Accept Permission
            Button(
                onClick = {
                    acceptTerms(activity)
                })
            {
                Text(text = "Accept")
            }

            //Spacer
            Spacer(modifier = Modifier.padding(8.dp))

            // Decline Permission. Go back to MainActivity
            Button(
                onClick = {
                   declinePermission(activity)
                })
            {
                Text(text = "Decline")
            }
        }
    }
}

fun acceptTerms(activity: ComponentActivity) {

    Log.d("PermissionChecker", "AcceptTerms")

    val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001

    // Users gets permission request from phone
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf<String>(Manifest.permission.ACTIVITY_RECOGNITION),
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION
        )
    }
    // NEXT STEPS - Can't return this way or doesn't open pop up.
    // Decline works, just need accept to work.
    //returnIntent(activity)
}

fun declinePermission(activity: ComponentActivity) {
    Toast.makeText(activity, "Permission Declined - SnorLabs cannot run",
        Toast.LENGTH_LONG).show()

    returnIntent(activity)

}

fun returnIntent (activity: ComponentActivity) {
    val returnHomeIntent = Intent(activity
        , MainActivity::class.java)
        .apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    activity.startActivity(returnHomeIntent)
}

 */