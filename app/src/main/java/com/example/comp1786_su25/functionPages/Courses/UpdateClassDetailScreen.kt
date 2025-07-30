package com.example.comp1786_su25.functionPages.Courses

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.classDetailsModel
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.teacherModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateClassDetailScreen(
    navController: NavController,
    classId: String,
    courseId: String,
    detailId: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State for form fields
    var className by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var typeOfClass by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var createdTime by remember { mutableStateOf("") }

    // State for parent class data
    var parentClassDayOfWeek by remember { mutableStateOf("") }
    var isLoadingParentClass by remember { mutableStateOf(true) }

    // Error states
    var classNameError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var teacherError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var typeError by remember { mutableStateOf(false) }
    var durationError by remember { mutableStateOf(false) }
    var capacityError by remember { mutableStateOf(false) }

    // State for teachers dropdown
    var isTeacherDropdownExpanded by remember { mutableStateOf(false) }
    var teachers by remember { mutableStateOf<List<teacherModel>>(emptyList()) }
    var selectedTeacherName by remember { mutableStateOf("Select Teacher") }

    // Load parent class data to get the day of week
    LaunchedEffect(classId) {
        classFirebaseRepository.getClassById(classId) { classData ->
            if (classData != null) {
                parentClassDayOfWeek = classData.day_of_week
            }
            isLoadingParentClass = false
        }
    }

    // Load class detail data
    LaunchedEffect(detailId) {
        classFirebaseRepository.getClassDetailById(classId, courseId, detailId) { classDetail ->
            if (classDetail != null) {
                className = classDetail.class_name
                dateText = classDetail.date
                selectedTeacherId = classDetail.teacher
                price = classDetail.price
                typeOfClass = classDetail.type_of_class
                duration = classDetail.duration
                capacity = classDetail.capacity
                description = classDetail.description
                createdTime = classDetail.createdTime

                // Get teacher name
                if (selectedTeacherId.isNotEmpty()) {
                    teacherFirebaseRepository.getTeacherById(selectedTeacherId) { teacher ->
                        if (teacher != null) {
                            selectedTeacherName = teacher.name
                        }
                    }
                }
            }
        }
    }

    // Load teachers
    LaunchedEffect(Unit) {
        teacherFirebaseRepository.getTeachers { teachersList ->
            teachers = teachersList
        }
    }

    // Helper function to get day of week number (1 = Sunday, 2 = Monday, etc.)
    fun getDayOfWeekNumber(dayName: String): Int {
        return when (dayName.lowercase()) {
            "sunday" -> 1
            "monday" -> 2
            "tuesday" -> 3
            "wednesday" -> 4
            "thursday" -> 5
            "friday" -> 6
            "saturday" -> 7
            else -> -1 // Invalid day
        }
    }

    // Helper function to find next date for specific day of week
    fun getNextDateForDayOfWeek(targetDayOfWeek: Int): Calendar {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        var daysToAdd = targetDayOfWeek - currentDayOfWeek
        if (daysToAdd <= 0) {
            daysToAdd += 7 // Move to next week if the day has passed this week
        }

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        return calendar
    }

    // Calendar date picker with day restriction
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val datePickerDialog = if (parentClassDayOfWeek.isNotEmpty()) {
        val targetDayOfWeek = getDayOfWeekNumber(parentClassDayOfWeek)
        if (targetDayOfWeek != -1) {
            // Parse current date to set initial date picker position
            val initialCalendar = if (dateText.isNotEmpty()) {
                try {
                    val currentDate = dateFormat.parse(dateText)
                    Calendar.getInstance().apply { time = currentDate!! }
                } catch (e: Exception) {
                    getNextDateForDayOfWeek(targetDayOfWeek)
                }
            } else {
                getNextDateForDayOfWeek(targetDayOfWeek)
            }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)

                    // Validate that selected date is on the correct day of week
                    if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == targetDayOfWeek) {
                        dateText = dateFormat.format(selectedCalendar.time)
                        dateError = false
                    } else {
                        // Show error message for invalid day selection
                        dateError = true
                        // You could also show a toast message here
                    }
                },
                initialCalendar.get(Calendar.YEAR),
                initialCalendar.get(Calendar.MONTH),
                initialCalendar.get(Calendar.DAY_OF_MONTH)
            )
        } else {
            // Fallback to regular date picker if day parsing fails
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateText = dateFormat.format(calendar.time)
                    dateError = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    } else {
        // Default date picker while loading
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                dateText = dateFormat.format(calendar.time)
                dateError = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Validation function
    fun validateInputs(): Boolean {
        var isValid = true

        if (className.isBlank()) {
            classNameError = true
            isValid = false
        }

        if (dateText.isBlank()) {
            dateError = true
            isValid = false
        }

        if (selectedTeacherId.isBlank()) {
            teacherError = true
            isValid = false
        }

        if (price.isBlank()) {
            priceError = true
            isValid = false
        }

        if (typeOfClass.isBlank()) {
            typeError = true
            isValid = false
        }

        if (duration.isBlank()) {
            durationError = true
            isValid = false
        }

        if (capacity.isBlank()) {
            capacityError = true
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Update Class Detail",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Class Name
        OutlinedTextField(
            value = className,
            onValueChange = {
                className = it
                classNameError = false
            },
            label = { Text("Class Name") },
            isError = classNameError,
            supportingText = {
                if (classNameError) Text("Class name is required", color = Color.Red)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Date Picker
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            isError = dateError,
            supportingText = {
                if (dateError) Text("Date is required", color = Color.Red)
            },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Teacher Dropdown
        ExposedDropdownMenuBox(
            expanded = isTeacherDropdownExpanded,
            onExpandedChange = { isTeacherDropdownExpanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedTeacherName,
                onValueChange = {},
                readOnly = true,
                isError = teacherError,
                supportingText = {
                    if (teacherError) Text("Teacher is required", color = Color.Red)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTeacherDropdownExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isTeacherDropdownExpanded,
                onDismissRequest = { isTeacherDropdownExpanded = false }
            ) {
                teachers.forEach { teacher ->
                    DropdownMenuItem(
                        text = { Text("${teacher.name} (${teacher.specialization})") },
                        onClick = {
                            selectedTeacherId = teacher.id
                            selectedTeacherName = teacher.name
                            isTeacherDropdownExpanded = false
                            teacherError = false
                        }
                    )
                }
            }
        }

        // Price
        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
                priceError = false
            },
            label = { Text("Price") },
            isError = priceError,
            supportingText = {
                if (priceError) Text("Price is required", color = Color.Red)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Type of Class
        OutlinedTextField(
            value = typeOfClass,
            onValueChange = {
                typeOfClass = it
                typeError = false
            },
            label = { Text("Type of Class") },
            isError = typeError,
            supportingText = {
                if (typeError) Text("Type is required", color = Color.Red)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Duration
        OutlinedTextField(
            value = duration,
            onValueChange = {
                duration = it
                durationError = false
            },
            label = { Text("Duration (minutes)") },
            isError = durationError,
            supportingText = {
                if (durationError) Text("Duration is required", color = Color.Red)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Capacity
        OutlinedTextField(
            value = capacity,
            onValueChange = {
                capacity = it
                capacityError = false
            },
            label = { Text("Capacity") },
            isError = capacityError,
            supportingText = {
                if (capacityError) Text("Capacity is required", color = Color.Red)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Update Button
        Button(
            onClick = {
                if (validateInputs()) {
                    val updatedClassDetail = classDetailsModel(
                        id = detailId,
                        class_name = className,
                        date = dateText,
                        teacher = selectedTeacherId,
                        price = price,
                        type_of_class = typeOfClass,
                        duration = duration,
                        capacity = capacity,
                        description = description,
                        createdTime = createdTime
                    )

                    coroutineScope.launch {
                        try {
                            classFirebaseRepository.updateClassDetail(classId, courseId, updatedClassDetail).addOnSuccessListener {
                                // Navigate back after successful update
                                navController.popBackStack()
                            }.addOnFailureListener { exception ->
                                // Handle error
                                println("Error updating class detail: ${exception.message}")
                            }
                        } catch (e: Exception) {
                            // Handle exception
                            println("Exception: ${e.message}")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp)
        ) {
            Text("Update Class Detail")
        }

        // Delete Button
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        classFirebaseRepository.deleteClassDetail(classId, courseId, detailId).addOnSuccessListener {
                            // Navigate back after successful deletion
                            navController.popBackStack()
                        }.addOnFailureListener { exception ->
                            // Handle error
                            println("Error deleting class detail: ${exception.message}")
                        }
                    } catch (e: Exception) {
                        // Handle exception
                        println("Exception: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp)
        ) {
            Text("Delete Class Detail")
        }

        // Cancel Button
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Text("Cancel")
        }
    }
}
