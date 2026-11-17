package app.invoicer.android.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.invoicer.data.AppContainer
import app.invoicer.domain.calc.DashboardCalculator
import app.invoicer.domain.calc.DashboardSummary
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.Money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class InvoicesState(
    val invoices: List<Invoice> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(Money.ZERO, Money.ZERO, Money.ZERO),
)

class InvoicesViewModel(container: AppContainer) : ViewModel() {
    val state: StateFlow<InvoicesState> = container.observeInvoices()
        .map { list ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            InvoicesState(list, DashboardCalculator.summarize(list, today))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoicesState())

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory { initializer { InvoicesViewModel(container) } }
    }
}
