package view

import androidx.compose.runtime.mutableStateOf
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileOutputStream
import javax.crypto.Cipher
import java.security.Key
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

class MainViewModel {

    var selectedFilePath = mutableStateOf("")
    var savingPath = mutableStateOf("")
    var password = mutableStateOf("")
    var isSourceFileFolder = mutableStateOf(false)
    var isSavingPathFolder = mutableStateOf(false)
    var isError = mutableStateOf(false)
    var algorithm = mutableStateOf("AES")

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