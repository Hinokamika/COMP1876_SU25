package com.example.comp1786_su25.functionPages.Class

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.comp1786_su25.controllers.dataClasses.classModel
import androidx.navigation.NavController
import com.example.comp1786_su25.components.DetailItem
import com.example.comp1786_su25.components.DetailSection
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.sqliteHelper.ClassDatabaseHelper

@Composable
fun ClassDetailsDialog(
    classData: classModel,
    onDismiss: () -> Unit,
    navController: NavController,
    teacherName: String? = null
) {
    // Get the context once at the Composable function level
    val context = LocalContext.current
    val dbHelper = ClassDatabaseHelper(context)

    // Use remember+mutableStateOf to properly handle state changes in Compose
    var teacherDisplayName by remember { mutableStateOf(teacherName ?: "Loading") }

    // Fetch teacher data when the component is first composed or when teacher ID changes
    LaunchedEffect(classData.teacher) {
        // Only fetch if we have a teacher ID
        if (classData.teacher.isNotEmpty()) {
            teacherFirebaseRepository.getTeacherById(classData.teacher) { teacher ->
                if (teacher != null) {
                    teacherDisplayName = teacher.name
                } else {
                    teacherDisplayName = "Unknown Teacher"
                }
            }
        } else {
            teacherDisplayName = "No Teacher Assigned"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = classData.class_name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                DetailSection(title = "Class Type", content = {
                    DetailItem(
                        label = "class type",
                        value = classData.type_of_class,
                        modifier = Modifier.fillMaxWidth()
                    )
                })

                // Class details in sections
                DetailSection(title = "Schedule", content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailItem(label = "Day", value = classData.day_of_week)
                        DetailItem(label = "Time", value = classData.time_of_course)
                    }
                })

                DetailSection(title = "Class Information", content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailItem(label = "Duration", value = classData.duration)
                        DetailItem(label = "Price", value = classData.price_per_class)
                    }
                })

                DetailSection(title = "Instructor", content = {
                    DetailItem(
                        label = "Teacher",
                        value = teacherDisplayName,
                        modifier = Modifier.fillMaxWidth()
                    )
                })

                Button(
                    onClick = {
                        // Navigate to update screen with class ID
                        classData.id?.let { id ->
                            navController.navigate("updateclass/${id}")
                            onDismiss() // Close the dialog after navigation
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Update Class")
                }

                Button(onClick = {
                    classData.id?.let { id ->
                        // Delete from Firebase
                        classFirebaseRepository.deleteClass(id)
                        // Delete from local SQLite database
                        dbHelper.deleteClassByFirebaseId(id)
                        // Dismiss the dialog
                        onDismiss()
                        // Navigate back to refresh the class list
                        navController.popBackStack()
                    }
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Delete Class")
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
