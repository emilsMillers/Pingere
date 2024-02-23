package com.example.pinger

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.pinger.ui.theme.PingerTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            MyApp(auth)
        }
    }
}

data class Perfil(val descrip: String?, val edad: Int?, val foto: String?, val nombre: String?, val numrand: Int?)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TinderUI() {
    val items = listOf("Home", "Chats")
    var selectedItem by remember { mutableStateOf("Home") }
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B)),
        startY = 0f
    )


    val mAuth = FirebaseAuth.getInstance()
    val userID = mAuth.currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("pinger").child(userID ?: "")

    val perfiles = mutableListOf<Perfil>()

    ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val descrip = dataSnapshot.child("descrip").getValue(String::class.java)
                val edad = dataSnapshot.child("edad").getValue(Long::class.java)?.toInt()
                val foto = dataSnapshot.child("foto").getValue(String::class.java)
                val nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                val numrand = dataSnapshot.child("numrand").getValue(Long::class.java)?.toInt()

                val perfil = Perfil(descrip, edad, foto, nombre, numrand)
                perfiles.add(perfil)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("Fallo la lectura: " + databaseError.code)
        }
    })

    //modifier = Modifier.background(brush = gradient),

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Pinger", textAlign = TextAlign.Center) },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                    }
                    IconButton(onClick = {  }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation {
                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(item) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                "Home" -> HomeScreen(perfiles)
                "Chats" -> ChatsScreen()
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(perfiles: List<Perfil>) {
    var perfilActual by remember { mutableStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }

    var backgroundColor by remember { mutableStateOf(Color.White) }
    var rotation by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    if (perfiles.isNotEmpty() && perfilActual in perfiles.indices) {
        val perfil = perfiles[perfilActual]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(backgroundColor)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX > 200) {
                            backgroundColor = Color.Green
                            perfilActual = (perfilActual + 1) % perfiles.size
                        } else if (offsetX < -200) {
                            backgroundColor = Color.Red
                            perfilActual = (perfilActual + 1) % perfiles.size
                        }
                        offsetX = 0f
                        rotation = 0f
                    },
                    onDrag = { change, dragAmount ->
                        offsetX += dragAmount.x
                        rotation = (offsetX / 1000f) * 45
                        change.consumeAllChanges()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        backgroundColor = Color.White
                        Toast
                            .makeText(context, "¡Has dado un Super Like!", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .rotate(rotation)
        ) {
            Image(
                painter = rememberImagePainter(data = perfil.foto),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "${perfil.nombre}, ${perfil.edad}",
                    style = TextStyle(fontSize = 24.sp, color = Color.White),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = perfil.descrip ?: "",
                    style = TextStyle(fontSize = 16.sp, color = Color.White),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // Swipe Buttons
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                backgroundColor = Color.Red
                perfilActual = (perfilActual + 1) % perfiles.size
            }) {
                Icon(Icons.Filled.Clear, contentDescription = "Dislike", tint = Color.Red)
            }
            IconButton(onClick = {
                backgroundColor = Color.White
                Toast.makeText(context, "¡Has dado un Super Like!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.Star, contentDescription = "Super Like", tint = Color.Blue)
            }
            IconButton(onClick = {
                backgroundColor = Color.Green
                perfilActual = (perfilActual + 1) % perfiles.size
                }) {
                Icon(Icons.Filled.Favorite, contentDescription = "Like", tint = Color.Green)
            }
        }
    }
}
}


@Composable
fun ChatsScreen() {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFFAF7B)),
        startY = 0f
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .padding(16.dp)
        ) {
            Text(
                text = "MENSAJES",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn {
                items(listOf(
                    "Ramona - Tonces me das una hamburguesa?",
                    "Ricardo - Perdon, estaba en el velatorio de mi prima política",
                    "Ombongo - Tu culo de negra ah",
                    "Ramoncin - Ramón fallecio hace 3 días por sobredosis de fentanilo, lo siento deveras."
                )) { message ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Card(
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(data = "https://firebasestorage.googleapis.com/v0/b/pinger-51b52.appspot.com/o/emilsMillers.jpg?alt=media&token=459fcb76-dfd7-4992-9870-87f6dc751ff4"),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("ComposableDestinationInComposeScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(auth: FirebaseAuth) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showMessage = remember { mutableStateOf<String?>(null) }
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") {
            PingerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "",
                            modifier = Modifier.padding(16.dp)
                        )
                        TextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            label = { Text("Nombre de usuario") },
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = password.value,
                            onValueChange = { password.value = it },
                            label = { Text("Contraseña") },
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (email.value.isBlank() || password.value.isBlank()) {
                                    showMessage.value =
                                        "Por favor, ingresa tu nombre de usuario y contraseña."
                                    return@Button
                                }
                                signIn(auth, email.value, password.value) { message ->
                                    showMessage.value = message
                                    if (message == "Inicio de sesión exitoso") {
                                        navController.navigate("tinderUI")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors()
                        ) {
                            Text("Iniciar sesión")
                        }
                        TextButton(
                            onClick = {
                                if (email.value.isBlank() || password.value.isBlank()) {
                                    showMessage.value =
                                        "Por favor, ingresa tu nombre de usuario y contraseña."
                                    return@TextButton
                                }
                                register(auth, email.value, password.value) { message ->
                                    showMessage.value = message
                                }
                            },
                        ) {
                            Text("Registrarse")
                        }

                        showMessage.value?.let {
                            AlertDialog(
                                onDismissRequest = { showMessage.value = null },
                                title = { Text("Mensaje") },
                                text = { Text(it) },
                                confirmButton = {
                                    Button(onClick = { showMessage.value = null }) {
                                        Text("OK")
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
        composable("tinderUI") {
            TinderUI()
        }
    }
}

private fun signIn(auth: FirebaseAuth, email: String, password: String, callback: (String) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback("Inicio de sesión exitoso")
            } else {
                val errorMessage = when(task.exception?.message) {
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> "No hay ningún registro de usuario correspondiente a este nombre de usuario. El usuario puede haber sido eliminado."
                    "The password is invalid or the user does not have a password." -> "La contraseña es inválida o el usuario no tiene una contraseña."
                    else -> "Error al iniciar sesión: ${task.exception?.message}"
                }
                callback(errorMessage)
            }
        }
}

private fun register(auth: FirebaseAuth, email: String, password: String, callback: (String) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback("Registro exitoso")
            } else {
                val errorMessage = when(task.exception?.message) {
                    "The email address is already in use by another account." -> "La dirección de correo electrónico ya está en uso por otra cuenta."
                    "The email address is badly formatted." -> "La dirección de correo electrónico tiene un formato incorrecto."
                    "The given password is invalid. [ Password should be at least 6 characters ]" -> "La contraseña proporcionada es inválida. La contraseña debe tener al menos 6 caracteres."
                    else -> "Error al registrarse: ${task.exception?.message}"
                }
                callback(errorMessage)
            }
        }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PingerTheme {
        TinderUI()
    }
}