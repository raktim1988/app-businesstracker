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
import app.invoicer.domain.validation.FieldValidation
import app.invoicer.domain.validation.parseScaled
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
    val quantity: String = "",
    val unitPrice: String = "",
    val vatRate: VatRate = VatRate.HIGH,
) {
    val descriptionError get() = FieldValidation.description(description)
    val quantityError get() = FieldValidation.quantity(quantity)
    val rateError get() = FieldValidation.rate(unitPrice)
    val isValid get() = descriptionError == null && quantityError == null && rateError == null
}

data class EditorState(
    val clientName: String = "",
    val lines: List<LineInput> = listOf(LineInput()),
    val totals: InvoiceTotals = InvoiceCalculator.totals(emptyList()),
    val showErrors: Boolean = false,
    val saved: Boolean = false,
) {
    val clientError get() = FieldValidation.client(clientName)
    val isValid get() = clientError == null && lines.isNotEmpty() && lines.all { it.isValid }
}

class EditorViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun onClientName(value: String) = _state.update { it.copy(clientName = value.take(80)) }

    fun onLine(index: Int, line: LineInput) = _state.update { s ->
        val lines = s.lines.toMutableList().also { it[index] = line }
        s.copy(lines = lines, totals = totalsOf(lines))
    }

    fun addLine() = _state.update { it.copy(lines = it.lines + LineInput(quantity = "1")) }

    fun removeLine(index: Int) = _state.update { s ->
        val lines = s.lines.toMutableList().also { if (it.size > 1) it.removeAt(index) }
        s.copy(lines = lines, totals = totalsOf(lines))
    }

    fun reset() = _state.update { EditorState() }

    @OptIn(ExperimentalUuidApi::class)
    fun save() {
        val s = _state.value
        if (!s.isValid) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
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
            _state.update { it.copy(showErrors = error != null, saved = error == null) }
        }
    }

    private fun totalsOf(lines: List<LineInput>) = InvoiceCalculator.totals(lines.mapNotNull(::toDomain))

    private fun toDomain(input: LineInput): InvoiceLine? {
        if (input.quantityError != null || input.rateError != null) return null
        val qty = parseScaled(input.quantity, 3) ?: return null
        val price = parseScaled(input.unitPrice, 2) ?: return null
        return InvoiceLine(input.description.trim(), qty, Money(price), input.vatRate)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory { initializer { EditorViewModel(container) } }
    }
}
