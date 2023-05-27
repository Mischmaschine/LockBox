import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import java.io.File

@Composable
fun app() {
    MaterialTheme {
        val selectedFilePath = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = selectedFilePath.value,
                onValueChange = { selectedFilePath.value = it.trim() },
                label = { Text("Dateipfad") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                val file = File(selectedFilePath.value)
                if (!file.exists()) {
                    println("Datei existiert nicht!")
                    return@Button
                }
                println("Datei ausgewählt: ${file.absolutePath}")
            }, enabled = selectedFilePath.value.isNotBlank()) {
                Text("Datei auswählen")
            }
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                println("Verschlüsseln: ${selectedFilePath.value} mit Passwort ${password.value}")
            }, enabled = selectedFilePath.value.isNotBlank() && password.value.isNotBlank()) {
                Text("Verschlüsseln")
            }
            Button(onClick = {
                println("Entschlüsseln: ${selectedFilePath.value} mit Passwort ${password.value}")
            }, enabled = selectedFilePath.value.isNotBlank() && password.value.isNotBlank()) {
                Text("Entschlüsseln")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication,
        title = "LockBox - Dateiverschlüsselung und -entschlüsselung",
        resizable = false,
        state = rememberWindowState(width = 600.dp, height = 500.dp)) {
        app()
    }
}