package com.example.alarmtrial

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmtrial.ui.theme.AlarmTrialTheme
import java.time.LocalTime
import java.util.Calendar


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmTrialTheme {
                AlarmTrialLayout(this)

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTrialLayout(activity: ComponentActivity) {

    val navController = rememberNavController()

    // Variable for tracking route and providing bottom nav UI
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    // Observe navigation changes, allows back button to be pressed and icons are tracked.
    navController.addOnDestinationChangedListener { _, destination, _ ->
        selectedItemIndex = when (destination.route) {
            "alarm" -> 0
            "analysis" -> 1
            "settings" -> 2
            else -> selectedItemIndex
        }
    }

    // Provides bottom nav with the information it needs for setting up bottom nav.
    val items = listOf(
        BottomNavigationItem(
            title = "Alarm",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = "alarm",
            hasNews = false
        ),
        BottomNavigationItem(
            title = "Analysis",
            selectedIcon = Icons.Filled.Menu,
            unselectedIcon = Icons.Outlined.Menu,
            route = "analysis",
            hasNews = false,
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = "settings",
            hasNews = false
        )
    )

    Scaffold(
       
        topBar = {
            HeaderComponent()
        },

        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp
                        )
                    )
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            navController.navigate(item.route)
                        },
                        label = {
                            Text(text = item.title)
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if(item.badgeCount != null) {
                                        Badge {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    } else if (item.hasNews) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    // Image vector depends if icon is selected
                                    imageVector = if (index == selectedItemIndex){
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        })
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)) {
            AlarmNavigation(navController = navController, activity = activity)
        }

    }

}

@Composable
fun AlarmNavigation(navController:NavHostController, activity: ComponentActivity){

    // Navigation controller function - allows bottom nav functionality

    var alarmSet by rememberSaveable {
        mutableStateOf(false)
    }

    // Retrieve or create the ChatViewModel
    val alarmViewModel: AlarmViewModel = viewModel()

    NavHost(navController = navController, startDestination = "alarm"){
        // Whatever goes in here will be considered the home screen
        composable("alarm") {
            AlarmScreen(activity = activity,
                alarmSet = alarmSet,
                onButtonClick = {alarmSet = !alarmSet},
                alarmViewModel
            )
        }

        composable("analysis") {
            AnalysisScreen()
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}

@Composable
fun AlarmScreen(
    activity: ComponentActivity,
    alarmSet: Boolean,
    onButtonClick: () -> Unit,
    alarmViewModel: AlarmViewModel
) {

    // Retrieve the selected time from the ViewModel
    // This captures the user selected time and keeps in place through recomposition

    var pickedTime by remember {
        alarmViewModel::pickedTime
    }

    Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .navigationBarsPadding()
        , contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(8.dp))

            if (alarmSet) {
                Text(text = "Selected Time $pickedTime")
            } else {
                Text(text = "No Alarm Set")
            }


            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier
                .weight(1.0f))

            if(!alarmSet){
                Button(
                    onClick = {
                        // Show a TimePickerDialog when the button is clicked
                        // Int, Int -> selectTime passes the user selected hour, minute as
                        // arguments into pickedTime
                        ShowTimePickerDialog(activity) { hour, minute ->
                            pickedTime = "%02d:%02d".format(hour, minute)
                            onButtonClick()
                            // Sends user selected time to AlarmSetter.kt
                            setAlarm(activity, hour, minute)
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()

                ) {
                    Text("Set Regular Alarm")
                }
            }

            // Cancel Alarm - if alarm is set, alarmSet Bool is true, so button functionality
            // is only to cancel.
            if (alarmSet){
                Button(
                    onClick = {
                        cancelAlarm(activity)
                        onButtonClick()
                    },
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()

                ) {
                    Text("Cancel Regular Alarm")
                }
            }
        }
    }
}


@Composable
fun AnalysisScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Sleep Analysis Coming Soon")
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "User Settings Coming Soon")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderComponent() {
    TopAppBar(
        modifier = Modifier.padding(8.dp),
        title = {
            Text(
                text = stringResource(R.string.header_text),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge)
        })
}

fun ShowTimePickerDialog(activity: ComponentActivity, callback: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Create and show the TimePickerDialog
    TimePickerDialog(
        activity,
        { _, hour, min ->
            callback(hour, min)
        },
        hourOfDay,
        minute,
        true
    ).show()
}

//Cancel alarm
fun cancelAlarm (context: Context){

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("NotificationMessage", "Test")
    }

    // Create pending intent with same name and requestCode to cancel.
    val alarmPendingintent = PendingIntent
        .getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    alarmManager.cancel(alarmPendingintent)

}

// View Model to store the user selected time.
class AlarmViewModel : ViewModel() {
    var pickedTime: String by mutableStateOf(LocalTime.NOON.toString())
}

