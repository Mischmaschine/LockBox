import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.io.FileOutputStream
import java.security.Key
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

@Composable
fun app() {
    val isDarkMode = remember { mutableStateOf(false) }

    MaterialTheme(
        colors = if (isDarkMode.value) darkColors() else lightColors(),
    ) {
        val selectedFilePath = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val isFolder = remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {

                OutlinedTextField(
                    value = selectedFilePath.value,
                    onValueChange = { selectedFilePath.value = it.trim() },
                    label = { Text("Dateipfad") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface,
                        focusedIndicatorColor = MaterialTheme.colors.onSurface,
                        unfocusedIndicatorColor = MaterialTheme.colors.onSurface
                    )
                )
                Button(onClick = {
                    val file = File(selectedFilePath.value)
                    if (!file.exists()) {
                        println("Datei existiert nicht!")
                        return@Button
                    }

                    if (file.isDirectory) {
                        isFolder.value = true
                        println("Ordner ausgewählt: ${file.absolutePath}")
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        cursorColor = MaterialTheme.colors.onSurface,
                        leadingIconColor = MaterialTheme.colors.onSurface,
                        focusedIndicatorColor = MaterialTheme.colors.onSurface,
                        unfocusedIndicatorColor = MaterialTheme.colors.onSurface
                    )
                )
                Button(onClick = {
                    println("Verschlüsseln: ${selectedFilePath.value} mit Passwort ${password.value}")

                    val file = File(selectedFilePath.value)

                    val encryptedFile = if (isFolder.value) {
                        File("test_enc")
                    } else {
                        File("test.enc")
                    }
                    if (encryptedFile.exists()) {
                        println("Datei existiert bereits!")
                        return@Button
                    }

                    if (isFolder.value) {
                        encryptFolder(
                            generateKeyFromPassword(password.value),
                            Cipher.getInstance("AES"),
                            file,
                            encryptedFile
                        )
                        return@Button
                    }

                    //Encrypt file
                    encrypt(generateKeyFromPassword(password.value), Cipher.getInstance("AES"), file, encryptedFile)
                }, enabled = selectedFilePath.value.isNotBlank() && password.value.isNotBlank()) {
                    Text("Verschlüsseln")
                }
                Button(onClick = {
                    println("Entschlüsseln: ${selectedFilePath.value} mit Passwort ${password.value}")

                    val file = File(selectedFilePath.value)

                    val decryptedFile = File("test.dec")
                    if (decryptedFile.exists()) {
                        println("Datei existiert bereits!")
                        return@Button
                    }

                    if (isFolder.value) {
                        decryptFolder(
                            generateKeyFromPassword(password.value),
                            Cipher.getInstance("AES"),
                            file,
                            decryptedFile
                        )
                        return@Button
                    }

                    //Decrypt file
                    decrypt(generateKeyFromPassword(password.value), Cipher.getInstance("AES"), file, decryptedFile)

                }, enabled = selectedFilePath.value.isNotBlank() && password.value.isNotBlank()) {
                    Text("Entschlüsseln")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Switch(checked = isDarkMode.value, onCheckedChange = { isDarkMode.value = it })
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LockBox - Dateiverschlüsselung und -entschlüsselung",
        resizable = false,
        state = rememberWindowState(width = 700.dp, height = 500.dp)
    ) {
        app()
    }
}

fun generateKeyFromPassword(password: String): Key {
    val sha = MessageDigest.getInstance("SHA-256")
    val key = sha.digest(password.toByteArray())
    return SecretKeySpec(key, "AES")
}

fun encryptFolder(key: Key, cipher: Cipher, inputFolder: File, outputFolder: File) {
    if (!outputFolder.exists()) {
        outputFolder.mkdir()
    }

    inputFolder.listFiles()?.forEach { file ->
        if (file.isFile) {
            val outputFile = File(outputFolder, file.name + ".enc")
            encrypt(key, cipher, file, outputFile)
        }
    }
}

fun decryptFolder(key: Key, cipher: Cipher, inputFolder: File, outputFolder: File) {
    if (!outputFolder.exists()) {
        outputFolder.mkdir()
    }

    inputFolder.listFiles()?.forEach { file ->
        if (file.isFile) {
            val outputFile = File(outputFolder, file.name.removeSuffix(".enc"))
            decrypt(key, cipher, file, outputFile)
        }
    }
}

fun encrypt(key: Key, cipher: Cipher, inputFile: File, outputFile: File) {
    cipher.init(Cipher.ENCRYPT_MODE, key, SecureRandom())
    inputFile.inputStream().use { input ->
        outputFile.outputStream().use { output ->
            cipher.outputStream(output).use { cipherOut ->
                input.copyTo(cipherOut)
            }
        }
    }
}

fun decrypt(key: Key, cipher: Cipher, inputFile: File, outputFile: File) {
    cipher.init(Cipher.DECRYPT_MODE, key)
    inputFile.inputStream().use { input ->
        outputFile.outputStream().use { output ->
            cipher.outputStream(output).use { cipherOut ->
                input.copyTo(cipherOut)
            }
        }
    }
}

fun Cipher.outputStream(output: FileOutputStream): CipherOutputStream =
    CipherOutputStream(output, this)