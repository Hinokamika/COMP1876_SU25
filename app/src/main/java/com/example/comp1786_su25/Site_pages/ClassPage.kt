package com.example.comp1786_su25.Site_pages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.AuthViewModel
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.dataClasses.classModel
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

    // Function to refresh data
    fun refreshData() {
        isRefreshing = true
        classFirebaseRepository.getClasses { fetchedClasses ->
            classes = fetchedClasses
            // Reset teacher names map before fetching new data
            teacherNames = emptyMap()
            // Fetch teacher names for all unique teacher IDs in the class list
            val uniqueTeacherIds = fetchedClasses.map { it.teacher }.toSet()
            uniqueTeacherIds.forEach { teacherId ->
                teacherFirebaseRepository.getTeacherById(teacherId) { teacher ->
                    teacher?.let {
                        teacherNames = teacherNames + (teacherId to it.name)
                    }
                }
            }
            isRefreshing = false
        }
    }

    // Fetch classes from Firebase when the composable is first displayed
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
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
                                ClassCard(classData = classData, navController = navController, teacherNames = teacherNames)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassCard(classData: classModel, navController: NavController, teacherNames: Map<String, String>) {
    // Add state to control dialog visibility
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Show dialog if state is true
    if (showDetailsDialog) {
        ClassDetailsDialog(
            classData = classData,
            onDismiss = { showDetailsDialog = false },
            navController = navController,
            teacherName = teacherNames[classData.teacher]
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
                        text = classData.time_of_course,
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
                        text = classData.duration,
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
                        text = classData.price_per_class,
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
                        text = teacherNames[classData.teacher] ?: "Unknown Teacher",
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
