package com.example.comp1786_su25.functionPages.User

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.comp1786_su25.controllers.userFirebaseRepository
import com.example.comp1786_su25.controllers.dataClasses.CartModel
import com.example.comp1786_su25.controllers.dataClasses.userModel

@Composable
fun UserDetailsDialog(modifier: Modifier = Modifier, userData: userModel, onDismiss: () -> Unit, navController: NavController) {
    var userClasses by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val scrollState = rememberScrollState()

    // Print out user details for debugging
    LaunchedEffect(Unit) {
        println("DEBUG: UserDetailsDialog opened with user:")
        println("DEBUG: ID: ${userData.id}")
        println("DEBUG: Name: ${userData.name}")
        println("DEBUG: Fields: ${userData.email}, ${userData.phone}, ${userData.age}")
        println("DEBUG: Created At: ${userData.createdAt}")
        println("DEBUG: Carts: ${userData.carts.size}")
    }

    fun refreshUserClasses() {
        val userId = userData.id
        println("DEBUG: Looking for classes with userId: $userId")

        classFirebaseRepository.getClassesByTeacherId(userId) { classes ->
            println("DEBUG: Found ${classes.size} classes for user")
            if (classes.isNotEmpty()) {
                println("DEBUG: First class: ${classes[0].class_name}, teacher: ${classes[0].teacher}")
            }
            userClasses = classes.map { it.class_name to it.type_of_class }
        }
    }
    // Always refresh when dialog is shown
    LaunchedEffect(Unit) {
        refreshUserClasses()
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
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = userData.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // User Identity Section
                DetailSection(title = "User Info", content = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Email", value = userData.email)
                            DetailItem(label = "Phone", value = userData.phone)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailItem(label = "Age", value = userData.age.toString())
                        }
                    }
                })
                // Carts Section
                DetailSection(title = "Shopping Carts (${userData.carts.size})", content = {
                    if (userData.carts.isEmpty()) {
                        Text("No carts found", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            userData.carts.entries.forEach { (cartId, cart) ->
                                CartDetails(cart = cart, cartId = cartId)
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                })

                Button(
                    onClick = {
                        // Navigate to update screen with user ID
                        navController.navigate("updateuser/${userData.id}")
                        onDismiss() // Close the dialog after navigation
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Update User")
                }

                Button(onClick = {
                    // Call the delete function from the repository
                    userFirebaseRepository.deleteUser(userData.id)
                    // Dismiss the dialog
                    onDismiss()
                    // Navigate back to refresh the user list
                    navController.popBackStack()
                },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Delete User")
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

@Composable
fun CartDetails(cart: CartModel, cartId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Cart: ${cart.timestamp}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Price: $${cart.total_price}", style = MaterialTheme.typography.bodyMedium)
            Text("Items: ${cart.total_items}", style = MaterialTheme.typography.bodyMedium)
        }

        // Show items if any
        if (cart.items.isNotEmpty()) {
            Text(
                text = "Items:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            cart.items.entries.forEach { (itemId, item) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.class_name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$${item.price}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
