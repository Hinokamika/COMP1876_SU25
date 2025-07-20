package com.example.comp1786_su25.Site_pages

import android.R
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.AuthViewModel
import com.example.comp1786_su25.GymAppApplication
import com.example.comp1786_su25.controllers.dataClasses.classDetailsModel
import com.example.comp1786_su25.controllers.dataClasses.classModel
import com.example.comp1786_su25.functionPages.Class.ClassDetailsDialog
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassPage(modifier: Modifier = Modifier, navController: NavController) {
    // State to hold the list of classes
    var classes by remember { mutableStateOf<List<classModel>>(emptyList()) }
    // State for search query
    var searchQuery by remember { mutableStateOf("") }
    var teacherNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    // State for refresh indicator
    var isRefreshing by remember { mutableStateOf(false) }
    // State for network status message
    var networkStatusMessage by remember { mutableStateOf("") }
    // Get sync manager
    val syncManager = GymAppApplication.getInstance().syncManager
    // Context for showing toast messages
    val context = LocalContext.current

    // Function to refresh data
    fun refreshData() {
        isRefreshing = true

        // Check if device is online
        if (syncManager.isOnline()) {
            networkStatusMessage = "Online mode: Syncing with cloud"

            // First sync data between local and cloud
            syncManager.syncAll { success ->
                if (success) {
                    // Then fetch all classes using the sync manager
                    syncManager.getAllClasses { fetchedClasses ->
                        classes = fetchedClasses

                        // Reset teacher names map before fetching new data
                        teacherNames = emptyMap()

//                        // Fetch teacher names for all unique teacher IDs in the class list
//                        val uniqueTeacherIds = fetchedClasses.map { it.teacher }.toSet()
//                        uniqueTeacherIds.forEach { teacherId ->
//                            teacherFirebaseRepository.getTeacherById(teacherId) { teacher ->
//                                teacher?.let {
//                                    teacherNames = teacherNames + (teacherId to it.name)
//                                }
//                            }
//                        }

                        isRefreshing = false
                        Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sync failed, still try to show data from local DB
                    val localClasses = GymAppApplication.getInstance().classDatabaseHelper.getAllClasses()
                    classes = localClasses
                    isRefreshing = false
                    Toast.makeText(context, "Sync failed, showing local data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Offline mode - use local database
            networkStatusMessage = "Offline mode: Using local data"
            val localClasses = GymAppApplication.getInstance().classDatabaseHelper.getAllClasses()
            classes = localClasses

            // Try to fetch teacher names from local database
            val teacherHelper = GymAppApplication.getInstance().teacherDatabaseHelper
//            val uniqueTeacherIds = localClasses.map { it.teacher }.toSet()
            var localTeacherNames = emptyMap<String, String>()

//            uniqueTeacherIds.forEach { teacherId ->
//                val teacher = teacherHelper.getTeacherById(teacherId)
//                teacher?.let {
//                    localTeacherNames = localTeacherNames + (teacherId to it.name)
//                }
//            }

            teacherNames = localTeacherNames
            isRefreshing = false
            Toast.makeText(context, "Offline mode: Using local data", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch classes when the composable is first displayed
    LaunchedEffect(key1 = true) {
        refreshData()
    }

    // Filter classes based on search query
    val filteredClasses = if (searchQuery.isEmpty()) {
        classes
    } else {
        classes.filter { classData ->
            classData.type_of_class.contains(searchQuery, ignoreCase = true) ||
            classData.class_name.lowercase().contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Classes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Logout button
                    IconButton(onClick = {
                        // Sign out using the AuthViewModel and navigate to intro screen
                        val authViewModel = AuthViewModel()
                        authViewModel.logout()
                        navController.navigate("intro") {
                            // Clear the back stack so user can't navigate back after logout
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )

            )
        },
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addclass") },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Class",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search classes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                // Wrap content in SwipeRefresh
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { refreshData() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Display classes from Firebase
                    if (filteredClasses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (classes.isEmpty())
                                    "No classes found"
                                else
                                    "No classes match your search",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (classes.isEmpty()) {
                                Text(
                                    text = "Add a class using the + button",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredClasses) { classData ->
                                CourseCard(classData = classData, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(classData: classModel, navController: NavController) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
            .clickable {
                expandedState = !expandedState
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = classData.class_name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Day",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = classData.day_of_week,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = classData.time_of_course + " hrs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (expandedState) {
                    // Show additional details when expanded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = classData.duration + " students",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "$" + classData.price_per_class,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Type of Class",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = classData.type_of_class,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

        }
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show Classes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                )
                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(ContentAlpha.medium)
                        .rotate(rotationState),
                    onClick = { expandedState = !expandedState },
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand/Collapse",
                    )
                }
            }
            if (expandedState){
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courseClasses) { classData ->
                        ClassCard(classData = classData, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(courseData: classDetailsModel, navController: NavController, teacherNames: Map<String, String>) {
    // Add state to control dialog visibility
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Show dialog if state is true
    if (showDetailsDialog) {
        ClassDetailsDialog(
            classData = classData,
            onDismiss = { showDetailsDialog = false },
            navController = navController,
//            teacherName = teacherNames[classData.teacher]
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = classData.class_name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = classData.day_of_week,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = classData.time_of_course + " hrs",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = classData.duration + " students",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$" + classData.price_per_class,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Teacher",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
//                        text = teacherNames[classData.teacher] ?: "Unknown Teacher",
                        text = "Unknown Teacher",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Type of Class",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = classData.type_of_class,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = { showDetailsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("View Details")
            }
        }
    }
}
