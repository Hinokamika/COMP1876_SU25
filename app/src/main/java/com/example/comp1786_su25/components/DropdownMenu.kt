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
import com.example.comp1786_su25.GymAppApplication
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.sqliteHelper.TeacherDatabaseHelper

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
