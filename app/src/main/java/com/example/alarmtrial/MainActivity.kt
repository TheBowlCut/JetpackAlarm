package com.example.alarmtrial

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material.DropdownMenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmtrial.ui.theme.AlarmTrialTheme
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity(), ConvertToMilliCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmTrialTheme {
                AlarmTrialLayout(this)
            }
        }
        // Register the broadcast receiver using LocalBroadcastManager
        val filter = IntentFilter(CountdownTimer.COUNTDOWN_BR)
        LocalBroadcastManager.getInstance(this).registerReceiver(countdownReceiver, filter)
    }

    override fun onConversionComplete(totalMilli: Double) {
        Log.d("MainActivity", "Total Milliseconds: $totalMilli")
        Toast.makeText(this, "Total Milliseconds: $totalMilli", Toast.LENGTH_LONG).show()

        //Start the service and pass the totalMilli value
        val intent = Intent(this, CountdownTimer::class.java)
        intent.putExtra("totalMilli", totalMilli).apply {
            action = CountdownTimer.ACTION_START
        }
        startForegroundService(intent)
    }

    private val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeRemaining = intent?.getLongExtra(CountdownTimer.EXTRA_TIME_REMAINING, 0L) ?: 0L
            val isPaused = intent?.getBooleanExtra(CountdownTimer.EXTRA_PAUSE_STATE, false) ?: false

            Log.d("MainActivity", "Time remaining received: $timeRemaining seconds")
            // Update UI accordingly
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(countdownReceiver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTrialLayout(activity: AppCompatActivity) {

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
       
        //topBar = {
        //    HeaderComponent()
        //},

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
fun AlarmNavigation(navController:NavHostController, activity: AppCompatActivity){

    // Navigation controller function - allows bottom nav functionality
    // Retrieve or create the AlarmViewModel and booleanViewModel to save alarm
    val alarmViewModel: AlarmViewModel = viewModel()
    val regBoolViewModel: ABoolViewModel = viewModel()

    // Retrieve or create the DynamicViewModel and booleanViewModel to save alarm
    val dynViewModel: DynamicViewModel = viewModel()
    val dynBoolViewModel: DBoolViewModel = viewModel()

    NavHost(navController = navController, startDestination = "alarm"){
        // Whatever goes in here will be considered the home screen
        composable("alarm") {
            AlarmScreen(activity = activity,
                alarmViewModel,
                regBoolViewModel,
                dynViewModel,
                dynBoolViewModel
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

@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(activity: AppCompatActivity,
                alarmViewModel: AlarmViewModel,
                regBoolViewModel: ABoolViewModel,
                dynamicViewModel: DynamicViewModel,
                dynBoolViewModel: DBoolViewModel
    ){

    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()


    if (dynBoolViewModel.dynAlarmSet) {
        LaunchedEffect(Unit){
        scope.launch {
            pagerState.animateScrollToPage(1)
            }
        }
    }
    if (regBoolViewModel.regAlarmSet) {
        LaunchedEffect(Unit){
            scope.launch {
                pagerState.animateScrollToPage(2)
            }
        }
    }

    if (dynBoolViewModel.cancelAll) {
        LaunchedEffect(Unit){
            scope.launch {
                pagerState.animateScrollToPage(0)
                dynBoolViewModel.cancelAll = false
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // HorizontalPager for the settings screens
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp), // Adjust padding to ensure space for indicators
            userScrollEnabled = false
        ) { page ->
            when (page) {

                0 -> DynamicAlarmScreen(
                    dynamicViewModel,
                    dynBoolViewModel,
                    regBoolViewModel,
                    activity
                )

                1 -> RegularAlarmScreen(
                    activity = activity,
                    alarmViewModel,
                    regBoolViewModel,
                    dynBoolViewModel
                )

                2 -> SleepScreen(
                    activity,
                    regBoolViewModel,
                    dynBoolViewModel
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp), // Padding to position the indicators above the navigation bar
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val color = if (pagerState.currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Gray
                }
                Spacer(modifier = Modifier.size(8.dp)) // Spacer to create space between indicators
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun DynamicAlarmScreen(
    dynViewModel: DynamicViewModel,
    dynBoolViewModel: DBoolViewModel,
    regBoolViewModel: ABoolViewModel,
    activity: AppCompatActivity
) {

    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(0L) }
    var isPaused by remember { mutableStateOf(false)}

    //This captures the user selected time and keeps in place through recomposition
    var selectedCntDown by remember {
        dynViewModel::dynAlarmTime
    }

    var dynAlarmSet by remember {
        dynBoolViewModel::dynAlarmSet
    }

    // DisposableEffect is used to handle side effects that need explicit cleanup when
    // the composable leaves the composition. Useful for starting and stopping receivers,
    // sensors etc.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getLongExtra(CountdownTimer.EXTRA_TIME_REMAINING, 0L) ?: 0L
                val isPausedReceived =
                    intent?.getBooleanExtra(CountdownTimer.EXTRA_PAUSE_STATE, false) ?: false
                if (!isPausedReceived) {
                    timeRemaining = time
                }
                isPaused = isPausedReceived
            }
        }
        val filter = IntentFilter(CountdownTimer.COUNTDOWN_BR)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, filter)

        onDispose {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .navigationBarsPadding()
        , verticalArrangement = Arrangement.Top
        , horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.padding(8.dp))

        if (dynAlarmSet){
            Text(
                text = "Sleep Timer: $selectedCntDown",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "Time Remaining: $timeRemaining seconds",
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            Text(
                text = "Set your dynamic alarm:",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
            , contentAlignment = Center
        ) {
            DynAlarmPicker(
                hour = hour,
                minute = minute,
                onTimeChange = { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                }
            )
        }

        Spacer(modifier = Modifier
            .height(8.dp))

        if(!dynAlarmSet) {
            Button(
                onClick = {
                    // First check if permission is granted fpr Activity Recognition
                    permissionChecker(activity)
                    powerModeCheck(activity)

                    selectedCntDown = String.format("%02d:%02d", hour, minute)
                    ConvertToMilli(hour, minute, activity as ConvertToMilliCallback)
                    dynAlarmSet = !dynAlarmSet
                },
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Set Time")
            }
        }

        if(dynAlarmSet){
            Button(
                onClick = {
                    // Stop the countdown service
                    cancelAlarm(activity, dynBoolViewModel, regBoolViewModel)
                    isPaused = false
                },
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Cancel Timer")
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularAlarmScreen(
    activity: AppCompatActivity,
    alarmViewModel: AlarmViewModel,
    regBoolViewModel: ABoolViewModel,
    dynBoolViewModel: DBoolViewModel
) {

    val currentTime = Calendar.getInstance()
    // Retrieve the selected time from the ViewModel
    // This captures the user selected time and keeps in place through recomposition
    var regAlarmTime by remember {
        alarmViewModel::regAlarmTime
    }

    var regAlarmSet by remember {
        regBoolViewModel::regAlarmSet
    }

    val timePickerState = remember {
        TimePickerState(
            is24Hour = true,
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE)
        )
    }

    Box(modifier = Modifier
        .padding(8.dp)
        .fillMaxSize()
        .navigationBarsPadding()
        , contentAlignment = TopCenter
    ) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(8.dp))

            if (regAlarmSet) {
                Text(
                    text = "Selected Time $regAlarmTime",
                    style = MaterialTheme.typography.headlineMedium)
            } else {
                Text(text = "No Alarm Set",
                    style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // Use Modifier.weight to let TimePicker take available space
            Box(
                modifier = Modifier
                    .weight(3f)
            ) {
                TimePicker(
                    state = timePickerState
                )
            }

            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier
                .weight(0.25f))

            if(!regAlarmSet){
                Button(
                    onClick = {

                        val hour = timePickerState.hour
                        val minute = timePickerState.minute

                        regAlarmTime = "%02d:%02d".format(hour, minute)
                        regAlarmSet = !regAlarmSet
                        setAlarm(activity, hour, minute)

                    },
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()

                ) {
                    Text("Set Regular Alarm")
                }
            }

            // Cancel Alarm - if alarm is set, alarmSet Bool is true, so button functionality
            // is only to cancel.
            if (regAlarmSet){
                Button(
                    onClick = {
                        cancelAlarm(activity,dynBoolViewModel,regBoolViewModel)
                        //regAlarmSet = !regAlarmSet
                    },
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()

                ) {
                    Text("Cancel Regular Alarm")
                }
            }
        }
    }
}

@Composable
fun SleepScreen(
    activity: AppCompatActivity,
    regBoolViewModel: ABoolViewModel,
    dynBoolViewModel: DBoolViewModel
) {

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .navigationBarsPadding(), contentAlignment = TopCenter
    ) {
        Column(
            horizontalAlignment =
            Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Sleep well...",
                style = MaterialTheme.typography.headlineMedium
            )

            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier
                .weight(0.25f)
            )

            Button(
                onClick = {
                    cancelAlarm(activity,dynBoolViewModel,regBoolViewModel)
                },
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth()

            ) {
                Text("Cancel Regular Alarm")
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

//Cancel alarm
fun cancelAlarm (
    context: Context,
    dynBoolViewModel: DBoolViewModel,
    regBoolViewModel: ABoolViewModel
){

    dynBoolViewModel.dynAlarmSet = false
    regBoolViewModel.regAlarmSet = false

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

    // Stop the countdown service
    val intent = Intent(context, CountdownTimer::class.java)
    context.stopService(intent)

    dynBoolViewModel.cancelAll = true

}

fun permissionChecker(activity: AppCompatActivity) {
    if(ContextCompat.checkSelfPermission
            (activity, Manifest.permission.ACTIVITY_RECOGNITION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(activity, "Permission Given", Toast.LENGTH_LONG).show()

    } else {
        Toast.makeText(activity, "Permission NOT GIVEN", Toast.LENGTH_LONG).show()
        //Opens up the cancel alarm activity for user. Service remains until closed.
        val permissionIntent = Intent(activity
            , PermissionChecker::class.java)
            .apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(permissionIntent)
    }
}

fun powerModeCheck(activity: AppCompatActivity) {
    val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager?
    val powerSaveMode = powerManager?.isPowerSaveMode ?: false

    if(powerSaveMode){
        val dialogFragment = PowerDialogFragment()
        dialogFragment.show(activity.supportFragmentManager, "PowerDialogFragment")
    }
}

class PowerDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(getString(R.string.powerSavingString))
            .setPositiveButton(
                getString(R.string.go_to_settings)
            ) { dialog, id -> // ACTION_BATTERY_SAVER_SETTINGS - send user to power saving mode
                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancelPowerSaving)) { dialog, id ->
                val powerSaveCancelIntent = Intent(context, MainActivity::class.java)
                startActivity(powerSaveCancelIntent)
            }
        // Create the AlertDialog object and return it
        return builder.create()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynAlarmPicker(hour: Int, minute: Int, onTimeChange: (Int, Int) -> Unit) {
    val hours = (0..11).map { it.toString().padStart(2,'0') }
    val minutes = (0..59).map { it.toString().padStart(2,'0') }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DropdownMenuPicker(
            label = "Hours",
            items = hours,
            selectedItem = hour,
            onItemSelected = { newHour ->
                onTimeChange(newHour.toInt(), minute)
            }
        )

        Text(
            text = ":",
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp))

        DropdownMenuPicker(
            label = "Minutes",
            items = minutes,
            selectedItem = minute,
            onItemSelected = { newMinute ->
                onTimeChange(hour, newMinute.toInt())
            }
        )
    }
}

@Composable
fun DropdownMenuPicker(
    label: String,
    items: List<String>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = "$label: ${items[selectedItem]}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = {
                    onItemSelected(index)
                    expanded = false
                }) {
                    Text(text = item.toString())
                }
            }
        }
    }
}

// View Model to store the user selected regular alarm time.
class AlarmViewModel : ViewModel() {
    var regAlarmTime: String by mutableStateOf(LocalTime.NOON.toString())
}

class ABoolViewModel : ViewModel() {
    var regAlarmSet: Boolean by  mutableStateOf(false)
}

class DynamicViewModel : ViewModel() {
    var dynAlarmTime: String by mutableStateOf("08:00")
}

class DBoolViewModel : ViewModel() {
    var dynAlarmSet: Boolean by  mutableStateOf(false)
    // currentPage state to reset when cancel button pressed
    var cancelAll: Boolean by  mutableStateOf(false)

}



