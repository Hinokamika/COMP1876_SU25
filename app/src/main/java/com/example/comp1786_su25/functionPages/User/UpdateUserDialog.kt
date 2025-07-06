package com.example.comp1786_su25.functionPages.User

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.comp1786_su25.controllers.userFirebaseRepository
import com.example.comp1786_su25.dataClasses.userModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserScreen(modifier: Modifier = Modifier, navController: NavController, userId: String? = null) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf("") }
    var carts by remember { mutableStateOf(emptyMap<String, com.example.comp1786_su25.dataClasses.CartModel>()) }

    val context = LocalContext.current

    LaunchedEffect(key1 = userId) {
        if (userId != null) {
            userFirebaseRepository.getUserById(userId) { userData ->
                if (userData != null) {
                    id = userData.id
                    name = userData.name
                    email = userData.email
                    phone = userData.phone
                    age = userData.age.toString()
                    uid = userData.uid
                    createdAt = userData.createdAt
                    carts = userData.carts
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Update User")
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
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (userId != null) {
                        // Validate age is a number
                        val ageValue = try {
                            age.toInt()
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Age must be a number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Update user
                        userFirebaseRepository.updateUser(
                            userModel(
                                id = id,
                                name = name,
                                email = email,
                                phone = phone,
                                age = ageValue,
                                uid = uid,
                                createdAt = createdAt,
                                carts = carts
                            )
                        )
                        Toast.makeText(context, "User updated", Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (userId != null) "Update User" else "Save User")
            }
        }
    }
}