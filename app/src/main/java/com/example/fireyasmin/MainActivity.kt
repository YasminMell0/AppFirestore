package com.example.fireyasmin

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fireyasmin.ui.theme.FireYasminTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = Firebase.firestore // Inicializa o Firestore
        setContent {
            FireYasminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(db)
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    val clientes = remember { mutableStateListOf<Client>() } // Lista de clientes como objetos

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(text = "App Firebase Firestore", modifier = Modifier.padding(20.dp))

        // Inputs para Nome e Telefone
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Column(modifier = Modifier.weight(0.5f)) {
                Text(text = "Nome:")
                TextField(value = nome, onValueChange = { nome = it })
            }
            Column(modifier = Modifier.weight(0.5f)) {
                Text(text = "Telefone:")
                TextField(value = telefone, onValueChange = { telefone = it })
            }
        }

        Button(onClick = {
            val cliente = hashMapOf(
                "nome" to nome,
                "telefone" to telefone
            )

            db.collection("Clientes").add(cliente)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                    // Limpa os campos após adicionar
                    nome = ""
                    telefone = ""
                    loadClientes(db, clientes) // Recarrega a lista de clientes
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
        }) {
            Text(text = "Cadastrar")
        }

        // Carrega os clientes do Firestore
        LaunchedEffect(Unit) {
            loadClientes(db, clientes)
        }

        // Exibe os clientes em uma lista
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(clientes) { cliente ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(text = cliente.nome, modifier = Modifier.weight(0.5f))
                    Text(text = cliente.telefone, modifier = Modifier.weight(0.3f))
                    Button(onClick = { deleteCliente(db, cliente.id, clientes) }) {
                        Text(text = "Deletar")
                    }
                }
            }
        }
    }
}

// Função para carregar clientes do Firestore
private fun loadClientes(db: FirebaseFirestore, clientes: MutableList<Client>) {
    clientes.clear() // Limpa a lista antes de adicionar novos dados
    db.collection("Clientes")
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                val cliente = Client(
                    id = document.id,
                    nome = document.getString("nome") ?: "---",
                    telefone = document.getString("telefone") ?: "---"
                )
                clientes.add(cliente)
                Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }
}

// Função para deletar um cliente do Firestore
private fun deleteCliente(db: FirebaseFirestore, clienteId: String, clientes: MutableList<Client>) {
    db.collection("Clientes").document(clienteId)
        .delete()
        .addOnSuccessListener {
            Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!")
            clientes.removeAll { it.id == clienteId } // Remove o cliente da lista local
        }
        .addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error deleting document", e)
        }
}

// Data class para Cliente
data class Client(
    val id: String,
    val nome: String,
    val telefone: String
)

// Preview da UI
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FireYasminTheme {
        App(Firebase.firestore)
    }
}
