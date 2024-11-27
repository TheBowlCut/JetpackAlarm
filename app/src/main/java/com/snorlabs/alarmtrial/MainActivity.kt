package com.snorlabs.alarmtrial

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material.DropdownMenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snorlabs.alarmtrial.R
import com.snorlabs.alarmtrial.ui.theme.AlarmTrialTheme
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar


class MainActivity : AppCompatActivity(), ConvertToMilliCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmTrialTheme {
                Surface() {
                    AlarmTrialLayout(this)
                }
            }
        }
        // Register the broadcast receiver using LocalBroadcastManager
        val filter = IntentFilter(CountdownTimer.COUNTDOWN_BR)
        LocalBroadcastManager.getInstance(this).registerReceiver(countdownReceiver, filter)
    }

    override fun onConversionComplete(totalMilli: Double) {
        Log.d("MainActivity", "Total Milliseconds: $totalMilli")
        //Toast.makeText(this, "Total Milliseconds: $totalMilli", Toast.LENGTH_LONG).show()

        //Start the service and pass the totalMilli value
        val intent = Intent(this, CountdownTimer::class.java)
        intent.putExtra("totalMilli", totalMilli).apply {
            action = CountdownTimer.ACTION_START
        }
        startForegroundService(intent)
    }

    private val countdownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val timeRemaining = intent?.getStringExtra(CountdownTimer.EXTRA_TIME_REMAINING) ?: ""
            val isPaused = intent?.getBooleanExtra(CountdownTimer.EXTRA_PAUSE_STATE, false) ?: false

            Log.d("MainActivity", "Time remaining : $timeRemaining")
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

    // Retrieve or create the DynamicViewModel and booleanViewModel to save alarm
    val dynViewModel: DynamicViewModel = viewModel()

    NavHost(navController = navController, startDestination = "alarm"){
        // Whatever goes in here will be considered the home screen
        composable("alarm") {
            AlarmScreen(activity = activity,
                alarmViewModel,
                dynViewModel,
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
                dynamicViewModel: DynamicViewModel,
    ){

    //val pagerState = rememberPagerState(pageCount = { 3 })

    val pagerState = rememberPagerState(
        initialPage = alarmViewModel.pageState,
        pageCount = {3}
    )

    LaunchedEffect(pagerState.currentPage) { // New lines
        alarmViewModel.pageState = pagerState.currentPage // Update ViewModel on page change
    }

    val scope = rememberCoroutineScope()

    if (dynamicViewModel.dynAlarmSet && !alarmViewModel.regAlarmSet) {
        LaunchedEffect(Unit){
        scope.launch {
            pagerState.animateScrollToPage(1)
            }
        }
    }
    if (dynamicViewModel.dynAlarmSet && alarmViewModel.regAlarmSet) {
        LaunchedEffect(Unit){
            scope.launch {
                pagerState.animateScrollToPage(2)
            }
        }
    }

    if (dynamicViewModel.cancelAll) {
        LaunchedEffect(Unit){
            scope.launch {
                pagerState.animateScrollToPage(0)
                dynamicViewModel.cancelAll = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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

                0 ->
                    DynamicAlarmScreen(
                    dynamicViewModel,
                    activity
                )

                1 -> RegularAlarmScreen(
                    activity = activity,
                    alarmViewModel,
                    dynamicViewModel
                )

                2 -> SleepScreen(
                    activity,
                    dynamicViewModel,
                    alarmViewModel
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 36.dp), // Padding to position the indicators above the navigation bar
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val color = if (pagerState.currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Gray
                }
                Spacer(modifier = Modifier.size(8.dp)) // Spacer to create space between indicators

                if (pagerState.currentPage != 2 ) {
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
}

@Composable
fun DynamicAlarmScreen(
    dynViewModel: DynamicViewModel,
    activity: AppCompatActivity
) {

    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }

    //This captures the user selected time and keeps in place through recomposition
    var selectedCntDown by remember {
        dynViewModel::dynAlarmTime
    }

    var dynAlarmSet by remember {
        dynViewModel::dynAlarmSet
    }

    var permissionBool by remember {
        dynViewModel::permissionChecker
    }

    var powerModeBool by remember {
        dynViewModel::powerModeChecker
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .navigationBarsPadding()
        , verticalArrangement = Arrangement.Top
        , horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier
            .padding(16.dp))

        Text(
            text = "Dynamic Sleep Timer",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

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

        Text(
            text = "- Starts when you're asleep\r\n- Pauses when you're awake",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if(!dynAlarmSet) {
            Button(
                onClick = {
                    // First check if permission is granted for Activity Recognition
                    permissionChecker(activity,dynViewModel)

                    if(permissionBool) {

                        powerModeCheck(activity,dynViewModel)

                        if(powerModeBool){
                        selectedCntDown = String.format("%02d:%02d", hour, minute)
                        ConvertToMilli(hour, minute, activity as ConvertToMilliCallback)
                        dynAlarmSet = !dynAlarmSet
                        }
                    }
                },
                modifier = Modifier
                    .padding(bottom = 36.dp, top = 36.dp)
            ) {
                Text(
                    text = "Set Time",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularAlarmScreen(
    activity: AppCompatActivity,
    alarmViewModel: AlarmViewModel,
    dynViewModel: DynamicViewModel
) {

    val currentTime = Calendar.getInstance()
    // Retrieve the selected time from the ViewModel
    // This captures the user selected time and keeps in place through recomposition
    var regAlarmTime by remember {
        alarmViewModel::regAlarmTime
    }

    var regAlarmSet by remember {
        alarmViewModel::regAlarmSet
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

            Spacer(modifier = Modifier.padding(16.dp))

            if (regAlarmSet) {
                Text(
                    text = "Selected Time $regAlarmTime",
                    style = MaterialTheme.typography.headlineMedium)
            } else {
                Text(text = "Set latest wake up",
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
                        .padding(bottom = 36.dp)
                        //.fillMaxWidth()

                ) {
                    Text("Set Time",
                        style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
fun SleepScreen(
    activity: AppCompatActivity,
    dynViewModel: DynamicViewModel,
    alarmViewModel: AlarmViewModel
) {

    //This captures the user selected time and keeps in place through recomposition
    var selectedCntDown by remember {
        dynViewModel::dynAlarmTime
    }

    var regAlarmTime by remember {
        alarmViewModel::regAlarmTime
    }

    var timeRemaining by remember { mutableStateOf(selectedCntDown) }
    var isPaused by remember { mutableStateOf(false)}


    // DisposableEffect is used to handle side effects that need explicit cleanup when
    // the composable leaves the composition. Useful for starting and stopping receivers,
    // sensors etc.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent?.getStringExtra(CountdownTimer.EXTRA_TIME_REMAINING) ?: selectedCntDown
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

            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "SnorLabs Active",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = "Time Remaining: $timeRemaining",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Image(
                painter = painterResource(id = R.drawable.snorlabs_icon_aug2023_512),
                contentDescription = "SnorLabs Logo",
                modifier = Modifier
                    .padding(8.dp),
                alpha = 0.15f
            )

            //Spacer to push the button to bottom of screen
            Spacer(modifier = Modifier
                .weight(0.25f)
            )

            Text(
                text = "Latest Alarm $regAlarmTime",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Button(
                onClick = {
                    cancelAlarm(activity,dynViewModel,alarmViewModel)
                },
                modifier = Modifier
                    .padding(bottom = 36.dp)

            ) {
                Text("Cancel Alarms",
                    style = MaterialTheme.typography.titleLarge)
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
    dynBoolViewModel: DynamicViewModel,
    regBoolViewModel: AlarmViewModel
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

fun permissionChecker(
    activity: AppCompatActivity,
    dynBoolViewModel: DynamicViewModel) {

    if(ContextCompat.checkSelfPermission
            (activity, Manifest.permission.ACTIVITY_RECOGNITION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        dynBoolViewModel.permissionChecker = true
        //Toast.makeText(activity, "Permission Given", Toast.LENGTH_LONG).show()
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

fun powerModeCheck(
    activity: AppCompatActivity,
    dynBoolViewModel: DynamicViewModel) {

    val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager?
    val powerSaveMode = powerManager?.isPowerSaveMode ?: false

    if(powerSaveMode){

        val dialogFragment = PowerDialogFragment()
        dialogFragment.show(activity.supportFragmentManager, "PowerDialogFragment")

    } else {

        dynBoolViewModel.powerModeChecker = true

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
            style = MaterialTheme.typography.titleLarge,
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

    Box(modifier = Modifier
        .wrapContentSize(Alignment.TopStart)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                text = "$label: ${items[selectedItem]}",
                style = MaterialTheme.typography.titleLarge)
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
                    Text(
                        text = item.toString(),
                        style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

// View Model to store the user selected regular alarm time.
class AlarmViewModel : ViewModel() {
    var pageState: Int by mutableStateOf(0) // tracks the page and supports better back navigation.
    var regAlarmTime: String by mutableStateOf(LocalTime.NOON.toString())
    var regAlarmSet: Boolean by  mutableStateOf(false)
}

class DynamicViewModel : ViewModel() {
    var dynAlarmTime: String by mutableStateOf("08:00:00")
    var dynAlarmSet: Boolean by  mutableStateOf(false)
    var permissionChecker: Boolean by mutableStateOf(false)
    var powerModeChecker: Boolean by mutableStateOf(false)
    // currentPage state to reset when cancel button pressed
    var cancelAll: Boolean by  mutableStateOf(false)
}


