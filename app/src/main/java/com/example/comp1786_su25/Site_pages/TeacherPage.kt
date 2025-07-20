package com.example.comp1786_su25.Site_pages

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.GymAppApplication
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.teacherModel
import com.example.comp1786_su25.functionPages.Teacher.TeacherDetailsDialog
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPage(modifier: Modifier, navController: NavController) {
    // State to hold the list of classes
    var teachers by remember { mutableStateOf<List<teacherModel>>(emptyList()) }
    // State for search query
    var searchQuery by remember { mutableStateOf("") }
    // State for refresh indicator
    var isRefreshing by remember { mutableStateOf(false) }
    var networkStatusMessage by remember { mutableStateOf("") }

    val syncManager = GymAppApplication.getInstance().syncManager
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
                    syncManager.getAllTeachers { fetchedTeachers ->
                        teachers = fetchedTeachers

                        isRefreshing = false
                        Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sync failed, still try to show data from local DB
                    val localTeachers = GymAppApplication.getInstance().teacherDatabaseHelper.getAllTeachers()
                    teachers = localTeachers
                    isRefreshing = false
                    Toast.makeText(context, "Sync failed, showing local data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Offline mode - use local database
            networkStatusMessage = "Offline mode: Using local data"
            val localTeachers = GymAppApplication.getInstance().teacherDatabaseHelper.getAllTeachers()
            teachers = localTeachers

            isRefreshing = false
            Toast.makeText(context, "Offline mode: Using local data", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch classes from Firebase when the composable is first displayed
    LaunchedEffect(key1 = true) {
        refreshData()
    }

    // Filter classes based on search query
    val filteredTeachers = if (searchQuery.isEmpty()) {
        teachers
    } else {
        teachers.filter { teacherData ->
            teacherData.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Teachers",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Logout button
                    IconButton(onClick = {
                        // Sign out and navigate to login screen
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
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
                onClick = { navController.navigate("addteacher") }, // Changed from "addclass" to "addteacher"
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Teacher",
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
                    label = { Text("Search teachers") }, // Changed label
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
                    // Display teachers from Firebase
                    if (filteredTeachers.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (teachers.isEmpty())
                                    "No teachers found" // Changed message
                                else
                                    "No teachers match your search", // Changed message
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (teachers.isEmpty()) {
                                Text(
                                    text = "Add a teacher using the + button", // Changed message
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
                            items(filteredTeachers) { teacherData ->
                                TeacherCard(teacherData = teacherData, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherCard(teacherData: teacherModel, navController: NavController) {
    // Add state to control dialog visibility
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Show dialog if state is true
    if (showDetailsDialog) {
        TeacherDetailsDialog(
            teacherData = teacherData,
            onDismiss = { showDetailsDialog = false },
            navController = navController
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
            )
            .clickable { showDetailsDialog = true }, // Make card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = teacherData.name,
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
                        text = "Class name",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = teacherData.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Age",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = teacherData.age,
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
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = teacherData.email,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phone",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = teacherData.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Specialization",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = teacherData.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
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
