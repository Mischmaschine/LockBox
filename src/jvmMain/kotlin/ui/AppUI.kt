package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
                val scope = rememberCoroutineScope()

                var expanded by remember { mutableStateOf(false) }
                var selectedIndex by remember { mutableStateOf(0) }
                fun checkError(selectedFilePath: String) =
                    (selectedFilePath.isNotBlank() && !File(selectedFilePath).exists()).also {
                        viewModel.isError = it
                    }

                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "LockBox - Mühelos sicher",
                        style = MaterialTheme.typography.h5
                    )

                    CustomTextField(
                        value = viewModel.selectedFilePath,
                        isError = checkError(viewModel.selectedFilePath),
                        onValueChange = {
                            viewModel.selectedFilePath = it.trim()
                        },
                        label = "Dateipfad",
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (viewModel.isError.not()) {
                        scope.launch {
                            if (viewModel.savingPath.isEmpty()) viewModel.savingPath =
                                viewModel.selectedFilePath.trim().replaceAfterLast(
                                    File.separator,
                                    ""
                                )
                        }

                    }

                    if (viewModel.isError) {
                        Text(
                            "Datei existiert nicht!",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }


                    OutlinedButton(onClick = {

                        val file = if (viewModel.selectedFilePath.isEmpty()) {
                            viewModel.selectFile()
                        } else {
                            File(viewModel.selectedFilePath)
                        }
                        if (file == null) return@OutlinedButton
                        viewModel.selectedFilePath = file.absolutePath

                        if (!file.exists()) {
                            println("Datei existiert nicht!")
                            return@OutlinedButton
                        }

                        if (file.isDirectory) {
                            viewModel.isSourceFileFolder = true
                            println("Ordner ausgewählt: ${file.absolutePath}")
                            return@OutlinedButton
                        }

                        println("Datei ausgewählt: ${file.absolutePath}")


                    }, enabled = viewModel.isError.not()) {
                        Text("Datei auswählen")
                    }

                    CustomTextField(
                        value = viewModel.savingPath,
                        onValueChange = { viewModel.savingPath = it.trim() },
                        label = "Speicherpfad",
                        isError = false
                    )

                    CustomTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = "Passwort",
                        isError = false
                    )
                    OutlinedButton(
                        onClick = {

                            val cipher = Cipher.getInstance(viewModel.algorithm)
                            val inputFile = File(viewModel.selectedFilePath)


                            val outputFolder = File(viewModel.savingPath)

                            println("Verschlüssle ${inputFile.absolutePath} nach ${outputFolder.absolutePath}")

                            viewModel.encrypt(
                                viewModel.generateKeyFromPassword(viewModel.password),
                                cipher,
                                inputFile,
                                outputFolder
                            )

                        },
                        enabled = viewModel.selectedFilePath.isNotBlank() && viewModel.password.isNotBlank() && viewModel.savingPath.isNotBlank()
                    ) {
                        Text("Verschlüsseln")
                    }

                    OutlinedButton(
                        onClick = {
                            val cipher = Cipher.getInstance(viewModel.algorithm)
                            val inputFile = File(viewModel.selectedFilePath)

                            try {
                                viewModel.decrypt(
                                    viewModel.generateKeyFromPassword(viewModel.password),
                                    cipher,
                                    inputFile
                                )
                            } catch (e: Exception) {
                                println("Fehler beim Entschlüsseln: ${e.message}")

                            }
                        },
                        enabled = viewModel.selectedFilePath.isNotBlank() && viewModel.password.isNotBlank() && viewModel.savingPath.isNotBlank()
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
                            onAlgorithmChange = { viewModel.algorithm = it }
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
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        isError: Boolean,
        modifier: Modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier,
            isError = isError,
        )
    }

    @Composable
    fun AlgorithmSelection(
        items: List<String>,
        selectedIndex: Int,
        onSelectedIndexChange: (Int) -> Unit,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        onAlgorithmChange: (String) -> Unit,
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