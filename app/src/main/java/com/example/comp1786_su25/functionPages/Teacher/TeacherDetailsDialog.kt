package com.example.comp1786_su25.functionPages.Teacher

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.comp1786_su25.components.DetailItem
import com.example.comp1786_su25.components.DetailSection
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.teacherModel

@Composable
fun TeacherDetailsDialog(
    teacherData: teacherModel, // Added parameter
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    navController: NavController
) {
    var teacherClasses by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    // Print out teacher details for debugging
    LaunchedEffect(Unit) {
        println("DEBUG: TeacherDetailsDialog opened with teacher:")
        println("DEBUG: ID: ${teacherData.id}")
        println("DEBUG: Name: ${teacherData.name}")
        println("DEBUG: Fields: ${teacherData.email}, ${teacherData.phone}, ${teacherData.age}")
    }

    fun refreshTeacherClasses() {
        val teacherId = teacherData.id ?: ""
        println("DEBUG: Looking for classes with teacherId: $teacherId")

        classFirebaseRepository.getClassesByTeacherId(teacherId) { classes ->
            teacherClasses = classes.map { it.class_name to it.type_of_class }
        }
    }
    // Always refresh when dialog is shown
    LaunchedEffect(Unit) {
        refreshTeacherClasses()
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
                    text = teacherData.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Specialization: ${teacherData.specialization}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                // Teacher Identity Section
                DetailSection(title = "Teacher Info", content = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Email", value = teacherData.email)
                            DetailItem(label = "Phone", value = teacherData.phone)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Age", value = teacherData.age)
                        }
                    }
                })
                // Classes Taught Section
                DetailSection(title = "Classes Taught", content = {
                    if (teacherClasses.isEmpty()) {
                        Text("No classes found", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            teacherClasses.forEach { (className, typeOfClass) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Class: $className", style = MaterialTheme.typography.bodyMedium)
                                    Text("Type: $typeOfClass", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                })

                Button(
                    onClick = {
                        // Navigate to update screen with class ID
                        teacherData.id?.let { id ->
                            navController.navigate("updateteacher/${id}")
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
                    teacherData.id?.let { id ->
                        // Call the delete function from the repository
                        teacherFirebaseRepository.deleteTeacher(id)
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
