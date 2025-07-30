package com.example.comp1786_su25.functionPages.Teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.comp1786_su25.controllers.classFirebaseRepository
import com.example.comp1786_su25.controllers.teacherFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.teacherModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeacherScreen(modifier: Modifier = Modifier, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }

    // Error states for validation
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var specializationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // Validation functions
    fun validateName(): Boolean {
        return if (name.trim().isEmpty()) {
            nameError = "Name cannot be empty"
            false
        } else {
            nameError = null
            true
        }
    }

    fun validateEmail(): Boolean {
        return if (email.trim().isEmpty()) {
            emailError = "Email cannot be empty"
            false
        } else if (!email.trim().endsWith("@gmail.com")) {
            emailError = "Email must end with @gmail.com"
            false
        } else {
            emailError = null
            true
        }
    }

    fun validatePhone(): Boolean {
        return if (phone.trim().isEmpty()) {
            phoneError = "Phone cannot be empty"
            false
        } else if (phone.trim().length < 10) {
            phoneError = "Phone must have at least 10 numbers"
            false
        } else {
            phoneError = null
            true
        }
    }

    fun validateAge(): Boolean {
        return if (age.trim().isEmpty()) {
            ageError = "Age cannot be empty"
            false
        } else {
            try {
                val ageValue = age.trim().toInt()
                if (ageValue <= 0) {
                    ageError = "Age must be greater than 0"
                    false
                } else {
                    ageError = null
                    true
                }
            } catch (e: NumberFormatException) {
                ageError = "Age must be a valid number"
                false
            }
        }
    }

    fun validateSpecialization(): Boolean {
        return if (specialization.trim().isEmpty()) {
            specializationError = "Specialization cannot be empty"
            false
        } else {
            specializationError = null
            true
        }
    }

    fun validateForm(): Boolean {
        val nameValid = validateName()
        val emailValid = validateEmail()
        val phoneValid = validatePhone()
        val ageValid = validateAge()
        val specializationValid = validateSpecialization()

        return nameValid && emailValid && phoneValid && ageValid && specializationValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Add Teacher")
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
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) validateName()
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) validateEmail()
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() })) {
                        phone = newValue
                        if (phoneError != null) validatePhone()
                    }
                },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = phoneError != null,
                supportingText = { phoneError?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        age = newValue
                        if (ageError != null) validateAge()
                    }
                },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = ageError != null,
                supportingText = { ageError?.let { Text(it) } }
            )
            Spacer(Modifier.height(12.dp))
            ClassTypeDropdown(
                selectedType = specialization,
                onTypeSelected = {
                    specialization = it
                    if (specializationError != null) validateSpecialization()
                },
                modifier = Modifier.fillMaxWidth(),
                isError = specializationError != null,
                errorMessage = specializationError
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (validateForm()) {
                        val createdAt = System.currentTimeMillis().toString()
                        val newTeacher = teacherModel(
                            "",
                            name,
                            email,
                            phone,
                            age,
                            specialization,
                            createdAt
                        )

                        val firebaseId = teacherFirebaseRepository.addTeacher(newTeacher)
                        navController.popBackStack() // Navigate back after saving
                    } else {
                        Toast.makeText(context, "Please fix the errors in the form", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Teacher")
            }
        }
    }
}