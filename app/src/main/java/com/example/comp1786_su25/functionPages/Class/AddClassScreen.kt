package com.example.comp1786_su25.functionPages.Class

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.components.ClassTypeDropdown
import com.example.comp1786_su25.components.TeacherDropdown
import com.example.comp1786_su25.components.WheelDateTimePickerDialog
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.dataClasses.classModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(modifier: Modifier = Modifier, navController: NavController) {
    var class_name by remember { mutableStateOf("") }
    var day_of_week by remember { mutableStateOf("") }
    var time_of_course by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price_per_class by remember { mutableStateOf("") }
    var type_of_class by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var context = LocalContext.current

    // State for date picker
    var showDatePicker by remember { mutableStateOf(false) }

    // Show date picker dialog when state is true
    WheelDateTimePickerDialog(
        showDatePicker = showDatePicker,
        onDateSelected = { date ->
            day_of_week = date // Update the day_of_week field with the selected date
        },
        onDismiss = {
            showDatePicker = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Add Class")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = class_name,
                onValueChange = { class_name = it },
                label = { Text("Class Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row {
                OutlinedTextField(
                    value = day_of_week,
                    onValueChange = { day_of_week = it },
                    label = { Text("Day of Week") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true // Make it read-only since it's set by date picker
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        showDatePicker = true
                    }
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = time_of_course,
                onValueChange = { time_of_course = it },
                label = { Text("Time of Course") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = capacity,
                onValueChange = { capacity = it },
                label = { Text("Capacity") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = price_per_class,
                onValueChange = { price_per_class = it },
                label = { Text("Price per Class") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            ClassTypeDropdown(
                selectedType = type_of_class,
                onTypeSelected = { type_of_class = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            TeacherDropdown(
                selectedType = teacher,
                onTypeSelected = { teacher = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                navController.popBackStack()
            },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    classFirebaseRepository.addClass(classModel("",class_name, day_of_week, time_of_course, capacity, duration, price_per_class, type_of_class, description, teacher))
                    navController.popBackStack()
                    Toast.makeText(context, "Class added successfully", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Class")
            }
        }
    }
}