package app.invoicer.android.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.invoicer.data.AppContainer
import app.invoicer.domain.calc.InvoiceCalculator
import app.invoicer.domain.model.Invoice
import app.invoicer.domain.model.InvoiceLine
import app.invoicer.domain.model.InvoiceTotals
import app.invoicer.domain.model.Money
import app.invoicer.domain.model.VatRate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class LineInput(
    val description: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val vatRate: VatRate = VatRate.HIGH,
)

data class EditorState(
    val clientName: String = "",
    val lines: List<LineInput> = listOf(LineInput()),
    val totals: InvoiceTotals = InvoiceCalculator.totals(emptyList()),
    val error: String? = null,
    val saved: Boolean = false,
)

class EditorViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun onClientName(value: String) = _state.update { it.copy(clientName = value, error = null) }

    fun onLine(index: Int, line: LineInput) = _state.update { s ->
        val lines = s.lines.toMutableList().also { it[index] = line }
        s.copy(lines = lines, totals = InvoiceCalculator.totals(lines.mapNotNull(::toDomain)), error = null)
    }

    fun addLine() = _state.update { it.copy(lines = it.lines + LineInput()) }

    fun removeLine(index: Int) = _state.update { s ->
        val lines = s.lines.toMutableList().also { if (it.size > 1) it.removeAt(index) }
        s.copy(lines = lines, totals = InvoiceCalculator.totals(lines.mapNotNull(::toDomain)))
    }

    @OptIn(ExperimentalUuidApi::class)
    fun save() {
        viewModelScope.launch {
            val s = _state.value
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val invoice = Invoice(
                id = Uuid.random().toString(),
                number = container.invoiceRepository.nextNumber(today.year),
                clientId = "",
                clientName = s.clientName.trim(),
                issueDate = today,
                dueDate = today.plus(DatePeriod(days = 30)),
                lines = s.lines.mapNotNull(::toDomain),
            )
            val error = container.saveInvoice(invoice)
            _state.update { it.copy(error = error, saved = error == null) }
        }
    }

    private fun toDomain(input: LineInput): InvoiceLine? {
        val qty = parseScaled(input.quantity, 3) ?: return null
        val price = parseScaled(input.unitPrice, 2) ?: return null
        return InvoiceLine(input.description.trim(), qty, Money(price), input.vatRate)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory { initializer { EditorViewModel(container) } }
    }
}

/** Parses "1,5" or "1.5" into an integer scaled by 10^[decimals]; null if invalid. */
internal fun parseScaled(text: String, decimals: Int): Long? {
    val clean = text.trim().replace(',', '.')
    if (!Regex("""\d+(\.\d*)?""").matches(clean)) return null
    val (whole, frac) = clean.split('.', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
    val fraction = frac.take(decimals).padEnd(decimals, '0')
    return (whole + fraction).toLongOrNull()
}
