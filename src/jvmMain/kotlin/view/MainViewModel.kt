package view

import androidx.compose.material.Button
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileOutputStream
import java.security.Key
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

class MainViewModel {

    var selectedFilePath by mutableStateOf("")
    var savingPath by mutableStateOf("")
    var password by mutableStateOf("")
    var isSourceFileFolder by mutableStateOf(false)
    var isSavingPathFolder by mutableStateOf(false)
    var isError by mutableStateOf(false)
    var algorithm by mutableStateOf("AES")
    var snackbarVisible by mutableStateOf(false)

    fun encrypt(key: Key, cipher: Cipher, inputFile: File, outputFile: File) {

        if (inputFile.name.endsWith(".enc")) {
            return
        }

        var file = outputFile
        cipher.init(Cipher.ENCRYPT_MODE, key, SecureRandom())
        inputFile.inputStream().use { input ->
            if (file.isDirectory) {
                file.mkdirs()
                file = File(outputFile, inputFile.name + ".enc")
            }

            file.outputStream().use { output ->
                cipher.outputStream(output).use { cipherOut ->
                    input.copyTo(cipherOut)
                }
            }
        }
    }

    @Throws(Exception::class)
    fun decrypt(key: Key, cipher: Cipher, inputFile: File) {

        if (inputFile.name.endsWith(".enc").not()) {
            throw Exception("Datei ist keine verschlüsselte Datei")
        }

        cipher.init(Cipher.DECRYPT_MODE, key)
        inputFile.inputStream().use { input ->


            println(inputFile.path)

            val filePath = inputFile.path.split("\\").toTypedArray().dropLast(1).joinToString("\\")

            val decryptedFile = File(filePath, inputFile.nameWithoutExtension)

            if (decryptedFile.exists()) {
                return
            }

            decryptedFile.outputStream().use { output ->
                cipher.outputStream(output).use { cipherOut ->
                    input.copyTo(cipherOut)
                }
            }
        }
    }

    fun selectFile(): File? {
        val dialog = FileDialog(null as Frame?, "Wähle eine Datei aus")
        dialog.mode = FileDialog.LOAD
        dialog.isVisible = true
        return if (dialog.file == null) null else File(dialog.directory + dialog.file)
    }

    fun generateKeyFromPassword(password: String): Key {
        val sha = MessageDigest.getInstance("SHA-256")
        val key = sha.digest(password.toByteArray())
        return SecretKeySpec(key, "AES")
    }
}

fun Cipher.outputStream(output: FileOutputStream): CipherOutputStream =
    CipherOutputStream(output, this)