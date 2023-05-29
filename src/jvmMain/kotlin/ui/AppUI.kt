package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.MainViewModel
import java.io.File
import javax.crypto.Cipher

class AppUI(private val viewModel: MainViewModel) {


    @Composable
    fun initialize() {

        val isDarkMode = remember { mutableStateOf(true) }

        MaterialTheme(
            colors = if (isDarkMode.value) darkColors() else lightColors(),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground
            ) {

                var expanded by remember { mutableStateOf(false) }
                var selectedIndex by remember { mutableStateOf(0) }
                fun checkError(selectedFilePath: String) =
                    (selectedFilePath.isNotBlank() && !File(selectedFilePath).exists()).also {
                        viewModel.isError.value = it
                    }

                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LockBox - Dateiverschlüsselung und -entschlüsselung", style = MaterialTheme.typography.h5)

                    CustomTextField(
                        value = viewModel.selectedFilePath.value,
                        onValueChange = { viewModel.selectedFilePath.value = it.trim() },
                        label = "Dateipfad",
                        isError = checkError(viewModel.selectedFilePath.value)
                    )

                    if (viewModel.isError.value) {
                        Text(
                            "Datei existiert nicht!",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    OutlinedButton(onClick = {
                        val file = if (viewModel.selectedFilePath.value.isEmpty()) {
                            viewModel.selectFile()
                        } else {
                            File(viewModel.selectedFilePath.value)
                        }

                        if (file == null) return@OutlinedButton
                        viewModel.selectedFilePath.value = file.absolutePath

                        if (!file.exists()) {
                            println("Datei existiert nicht!")
                            return@OutlinedButton
                        }

                        if (file.isDirectory) {
                            viewModel.isSourceFileFolder.value = true
                            println("Ordner ausgewählt: ${file.absolutePath}")
                            return@OutlinedButton
                        }

                        println("Datei ausgewählt: ${file.absolutePath}")
                    }, enabled = viewModel.isError.value.not()) {
                        Text("Datei auswählen")
                    }

                    CustomTextField(
                        value = viewModel.savingPath.value,
                        onValueChange = { viewModel.savingPath.value = it.trim() },
                        label = "Speicherpfad",
                        isError = false
                    )

                    CustomTextField(
                        value = viewModel.password.value,
                        onValueChange = { viewModel.password.value = it },
                        label = "Passwort",
                        isError = false
                    )

                    OutlinedButton(
                        onClick = {

                            val cipher = Cipher.getInstance(viewModel.algorithm.value)
                            val inputFile = File(viewModel.selectedFilePath.value)


                            val outputFolder =
                                if (viewModel.isSavingPathFolder.value) File(viewModel.savingPath.value) else File(
                                    viewModel.savingPath.value + File.separator + inputFile.name
                                )

                            viewModel.encrypt(
                                viewModel.generateKeyFromPassword(viewModel.password.value),
                                cipher,
                                inputFile,
                                outputFolder
                            )
                        },
                        enabled = viewModel.selectedFilePath.value.isNotBlank() && viewModel.password.value.isNotBlank() && viewModel.savingPath.value.isNotBlank()
                    ) {
                        Text("Verschlüsseln")
                    }

                    OutlinedButton(
                        onClick = {
                            val cipher = Cipher.getInstance(viewModel.algorithm.value)
                            val inputFile = File(viewModel.selectedFilePath.value)
                            val outputFile = File(viewModel.savingPath.value)

                            viewModel.decrypt(
                                viewModel.generateKeyFromPassword(viewModel.password.value),
                                cipher,
                                inputFile,
                                outputFile
                            )
                        },
                        enabled = viewModel.selectedFilePath.value.isNotBlank() && viewModel.password.value.isNotBlank() && viewModel.savingPath.value.isNotBlank()
                    ) {
                        Text("Entschlüsseln")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AlgorithmSelection(
                            items = listOf("AES", "RSA"),
                            selectedIndex = selectedIndex,
                            onSelectedIndexChange = { selectedIndex = it },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onAlgorithmChange = { viewModel.algorithm.value = it }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Switch(
                                checked = isDarkMode.value,
                                onCheckedChange = { isDarkMode.value = it })
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, isError: Boolean) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError
        )
    }

    @Composable
    fun AlgorithmSelection(
        items: List<String>,
        selectedIndex: Int,
        onSelectedIndexChange: (Int) -> Unit,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onAlgorithmChange: (String) -> Unit
    ) {
        OutlinedButton(onClick = { onExpandedChange(true) }) {
            Text(items[selectedIndex])
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.width(150.dp)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    onSelectedIndexChange(index)
                    onExpandedChange(false)
                    onAlgorithmChange(s)
                }) {
                    Text(text = s)
                }
            }
        }

    }

}