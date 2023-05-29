import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.AppUI
import view.MainViewModel


fun main() = application {

    val viewModel = MainViewModel()

    Window(
        onCloseRequest = ::exitApplication,
        title = "LockBox - Dateiverschlüsselung und -entschlüsselung",
        resizable = false,
        state = rememberWindowState(width = 700.dp, height = 500.dp)
    ) {

        AppUI(viewModel).initialize()
    }
}