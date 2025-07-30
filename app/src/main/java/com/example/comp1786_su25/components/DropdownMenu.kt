package com.example.comp1786_su25.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.teacherFirebaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String = "Select Option",
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            isError = isError,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun DateListDropdown(selectedType: String,
                     onTypeSelected: (String) -> Unit,
                     modifier: Modifier = Modifier,
                     isError: Boolean = false,
                     errorMessage: String? = null) {
    val dateListOptions = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    CustomDropdownMenu(
        modifier = modifier.fillMaxWidth(),
        options = dateListOptions,
        selectedOption = selectedType,
        onOptionSelected = onTypeSelected,
        label = "Date",
        placeholder = "Select date",
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
fun ClassTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val classTypeOptions = listOf(
        "Flow Yoga",
        "Aerial Yoga",
        "Family Yoga",
    )

    CustomDropdownMenu(
        modifier = modifier.fillMaxWidth(),
        options = classTypeOptions,
        selectedOption = selectedType,
        onOptionSelected = onTypeSelected,
        label = "Class Type",
        placeholder = "Select class type",
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
fun DynamicClassTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    classId: String? = null // Add classId parameter to fetch parent class type
) {
    // State to hold class types fetched from courses
    var classTypeOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch class types from courses table AND include default options
    LaunchedEffect(Unit) {
        val defaultOptions = listOf("Flow Yoga", "Aerial Yoga", "Family Yoga")

        // First, get existing course types
        classFirebaseRepository.fetchAllCoursesFromDatabase { coursesWithClassId ->
            // Extract unique class types from existing courses
            val existingClassTypes = coursesWithClassId
                .map { (_, course) -> course.classType }
                .filter { it.isNotEmpty() }
                .distinct()

            // If classId is provided, also get the parent class type
            if (classId != null) {
                classFirebaseRepository.getClassById(classId) { parentClass ->
                    val parentClassType = parentClass?.type_of_class ?: ""

                    // Combine all types: existing course types + parent class type + default options
                    val allClassTypes = (existingClassTypes + listOfNotNull(parentClassType.takeIf { it.isNotEmpty() }) + defaultOptions)
                        .distinct()
                        .sorted()

                    classTypeOptions = allClassTypes
                    isLoading = false
                }
            } else {
                // Just combine existing types with default options
                val allClassTypes = (existingClassTypes + defaultOptions)
                    .distinct()
                    .sorted()

                classTypeOptions = allClassTypes
                isLoading = false
            }
        }
    }

    // Show loading state or dropdown
    if (isLoading) {
        OutlinedTextField(
            value = "Loading class types...",
            onValueChange = {},
            label = { Text("Class Type") },
            readOnly = true,
            modifier = modifier.fillMaxWidth(),
            enabled = false
        )
    } else {
        CustomDropdownMenu(
            modifier = modifier.fillMaxWidth(),
            options = classTypeOptions,
            selectedOption = selectedType,
            onOptionSelected = onTypeSelected,
            label = "Class Type",
            placeholder = "Select class type",
            isError = isError,
            errorMessage = errorMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDropdown(
    loadingFunction: () -> List<Pair<String, String>> = { emptyList() },
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var teacherOptions by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    // Force recomposition when this function is called
    val teachers = loadingFunction()

    // Update the state with the new teachers
    LaunchedEffect(teachers) {
        teacherOptions = teachers
    }

    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = teacherOptions.find { it.first == selectedType }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedLabel,
            onValueChange = {},
            label = { Text("Teacher") },
            placeholder = { Text("Select teacher") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            teacherOptions.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onTypeSelected(id)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
