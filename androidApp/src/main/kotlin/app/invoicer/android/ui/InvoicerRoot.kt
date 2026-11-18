package app.invoicer.android.ui

import app.invoicer.android.ui.settings.SettingsScreen
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import app.invoicer.android.R
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.invoicer.android.ui.dashboard.DashboardScreen
import app.invoicer.android.ui.editor.EditorScreen
import app.invoicer.android.ui.editor.EditorViewModel
import app.invoicer.android.ui.invoices.InvoiceListScreen
import app.invoicer.android.ui.invoices.InvoicesViewModel
import app.invoicer.android.ui.placeholder.PlaceholderScreen
import app.invoicer.data.AppContainer

private const val EDITOR = "editor"

private enum class Tab(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Home("home", R.string.tab_home, Icons.Filled.Home),
    Invoices("invoices", R.string.tab_invoices, Icons.Filled.Receipt),
    Clients("clients", R.string.tab_clients, Icons.Filled.People),
    Settings("settings", R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
fun InvoicerRoot(container: AppContainer) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != EDITOR) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val invoicesVm: InvoicesViewModel = viewModel(factory = InvoicesViewModel.factory(container))
        NavHost(navController, startDestination = Tab.Home.route, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Tab.Home.route) {
                val homeEditorVm: EditorViewModel = viewModel(factory = EditorViewModel.factory(container))
                DashboardScreen(
                    invoicesViewModel = invoicesVm,
                    editorViewModel = homeEditorVm,
                    onOpenInvoices = { navController.navigate(Tab.Invoices.route) },
                    onOpenEditor = { navController.navigate(EDITOR) },
                )
            }
            composable(Tab.Invoices.route) {
                InvoiceListScreen(invoicesVm) { navController.navigate(EDITOR) }
            }
            composable(Tab.Clients.route) { PlaceholderScreen(stringResource(R.string.clients_coming)) }
            composable(Tab.Settings.route) { SettingsScreen() }
            composable(EDITOR) {
                // Shares the Home draft so "More lines…" keeps what was typed
                val homeEntry = remember(it) { navController.getBackStackEntry(Tab.Home.route) }
                val vm: EditorViewModel = viewModel(homeEntry, factory = EditorViewModel.factory(container))
                EditorScreen(vm, onClose = { navController.popBackStack() })
            }
        }
    }
}
