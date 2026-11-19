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
import app.invoicer.android.ui.clients.ClientFormScreen
import app.invoicer.android.ui.clients.ClientsScreen
import app.invoicer.android.ui.clients.ClientsViewModel
import app.invoicer.android.ui.editor.EditorScreen
import app.invoicer.android.ui.editor.EditorViewModel
import app.invoicer.android.ui.invoices.InvoiceListScreen
import app.invoicer.android.ui.invoices.InvoicesViewModel
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.invoicer.android.pdf.InvoicePdf
import app.invoicer.android.ui.clients.ClientDetailScreen
import app.invoicer.android.ui.invoices.InvoiceDetailScreen
import app.invoicer.android.ui.settings.BusinessProfileViewModel
import app.invoicer.domain.calc.ClientCalculator
import app.invoicer.domain.calc.belongsTo
import app.invoicer.domain.model.BusinessProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import app.invoicer.data.AppContainer

private const val EDITOR = "editor"
private const val CLIENT_FORM = "client_form?id={id}"
private const val INVOICE = "invoice/{id}"
private const val CLIENT = "client/{id}"
private fun clientForm(id: String? = null) = if (id == null) "client_form" else "client_form?id=$id"

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
            if (Tab.entries.any { it.route == currentRoute }) {
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
        val clientsVm: ClientsViewModel = viewModel(factory = ClientsViewModel.factory(container))
        val profileVm: BusinessProfileViewModel = viewModel(factory = BusinessProfileViewModel.factory(container))
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        NavHost(navController, startDestination = Tab.Home.route, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Tab.Home.route) {
                val homeEditorVm: EditorViewModel = viewModel(factory = EditorViewModel.factory(container))
                DashboardScreen(
                    invoicesViewModel = invoicesVm,
                    editorViewModel = homeEditorVm,
                    onOpenInvoices = { navController.navigate(Tab.Invoices.route) },
                    onOpenInvoice = { navController.navigate("invoice/${it.id}") },
                    onOpenEditor = { navController.navigate(EDITOR) },
                )
            }
            composable(Tab.Invoices.route) {
                InvoiceListScreen(invoicesVm, onOpenInvoice = { navController.navigate("invoice/${it.id}") }) { navController.navigate(EDITOR) }
            }
            composable(Tab.Clients.route) {
                ClientsScreen(clientsVm, onOpenClient = { navController.navigate("client/${it.id}") }) { navController.navigate(clientForm()) }
            }
            composable(
                CLIENT_FORM,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { entry ->
                val editId = entry.arguments?.getString("id")
                val clients by clientsVm.clients.collectAsStateWithLifecycle()
                val initial = clients.firstOrNull { it.id == editId }
                if (editId == null || initial != null) {
                    ClientFormScreen(
                        initial = initial,
                        onSave = { clientsVm.save(it); navController.popBackStack() },
                        onClose = { navController.popBackStack() },
                    )
                }
            }
            composable(INVOICE, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id")
                val state by invoicesVm.state.collectAsStateWithLifecycle()
                val clients by clientsVm.clients.collectAsStateWithLifecycle()
                val profile by profileVm.profile.collectAsStateWithLifecycle()
                val invoice = state.invoices.firstOrNull { it.id == id }
                if (invoice != null) {
                    val client = clients.firstOrNull { invoice.belongsTo(it) }
                    InvoiceDetailScreen(
                        invoice = invoice,
                        onBack = { navController.popBackStack() },
                        onOpenClient = client?.let { c -> { navController.navigate("client/${c.id}") } },
                        onSetStatus = { invoicesVm.setStatus(invoice, it) },
                        onSharePdf = {
                            scope.launch {
                                val business = profile ?: BusinessProfile()
                                runCatching {
                                    withContext(Dispatchers.IO) { InvoicePdf.create(context, invoice, client, business) }
                                }.onSuccess { file ->
                                    context.startActivity(InvoicePdf.shareIntent(context, file, invoice, client, business))
                                }.onFailure {
                                    Toast.makeText(context, R.string.pdf_error, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                    )
                }
            }
            composable(CLIENT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id")
                val clients by clientsVm.clients.collectAsStateWithLifecycle()
                val state by invoicesVm.state.collectAsStateWithLifecycle()
                val client = clients.firstOrNull { it.id == id }
                if (client != null) {
                    val theirs = state.invoices.filter { it.belongsTo(client) }
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    ClientDetailScreen(
                        client = client,
                        invoices = theirs,
                        summary = ClientCalculator.summarize(theirs, today),
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(clientForm(client.id)) },
                        onDelete = { clientsVm.delete(client.id); navController.popBackStack() },
                        onNewInvoice = {
                            val homeEditor: EditorViewModel = ViewModelProvider(
                                navController.getBackStackEntry(Tab.Home.route),
                                EditorViewModel.factory(container),
                            )[EditorViewModel::class.java]
                            homeEditor.reset()
                            homeEditor.pickClient(client)
                            navController.navigate(EDITOR)
                        },
                        onOpenInvoice = { navController.navigate("invoice/${it.id}") },
                    )
                }
            }
            composable(Tab.Settings.route) { SettingsScreen(profileVm) }
            composable(EDITOR) {
                // Shares the Home draft so "More lines…" keeps what was typed
                val homeEntry = remember(it) { navController.getBackStackEntry(Tab.Home.route) }
                val vm: EditorViewModel = viewModel(homeEntry, factory = EditorViewModel.factory(container))
                EditorScreen(vm, onClose = { navController.popBackStack() })
            }
        }
    }
}
