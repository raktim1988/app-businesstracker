package app.invoicer.android.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.invoicer.data.AppContainer
import app.invoicer.domain.calc.InvoiceCalculator
import app.invoicer.domain.model.Client
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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
import kotlinx.datetime.LocalDate
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

data class CreatedInvoice(val id: String, val number: String)

data class EditorState(
    val clientName: String = "",
    val clientId: String = "",
    val paymentTermDays: Int = 14,
    val issueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val lines: List<LineInput> = listOf(LineInput()),
    val totals: InvoiceTotals = InvoiceCalculator.totals(emptyList()),
    val showErrors: Boolean = false,
    val saved: Boolean = false,
    val editingId: String? = null,
    val editingNumber: String? = null,
) {
    val dueDate get() = issueDate.plus(DatePeriod(days = paymentTermDays))
    val clientError get() = FieldValidation.client(clientName)
    val isValid get() = clientError == null && lines.isNotEmpty() && lines.all { it.isValid }
}

class EditorViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    /** Kept outside [EditorState] so reset() does not wipe it before Home has shown the confirmation. */
    private val _justCreated = MutableStateFlow<CreatedInvoice?>(null)
    val justCreated: StateFlow<CreatedInvoice?> = _justCreated.asStateFlow()

    fun consumeJustCreated() = _justCreated.update { null }

    val clients: StateFlow<List<Client>> = container.clientRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onClientName(value: String) =
        _state.update { it.copy(clientName = value.take(80), clientId = "", paymentTermDays = 14) }

    fun pickClient(client: Client) =
        _state.update { it.copy(clientName = client.name, clientId = client.id, paymentTermDays = client.paymentTermDays) }

    fun onLine(index: Int, line: LineInput) = _state.update { s ->
        val lines = s.lines.toMutableList().also { it[index] = line }
        s.copy(lines = lines, totals = totalsOf(lines))
    }

    fun addLine() = _state.update { it.copy(lines = it.lines + LineInput(quantity = "1"), showErrors = false) }

    fun removeLine(index: Int) = _state.update { s ->
        val lines = s.lines.toMutableList().also { if (it.size > 1) it.removeAt(index) }
        s.copy(lines = lines, totals = totalsOf(lines))
    }

    fun onIssueDate(date: LocalDate) = _state.update { it.copy(issueDate = date) }

    fun reset() = _state.update { EditorState() }

    fun loadDraft(invoice: Invoice) {
        _state.value = EditorState(
            clientName = invoice.clientName,
            clientId = invoice.clientId,
            paymentTermDays = (invoice.dueDate.toEpochDays() - invoice.issueDate.toEpochDays()).toInt(),
            issueDate = invoice.issueDate,
            lines = invoice.lines.map {
                LineInput(it.description, plainQuantity(it.quantityMilli), plainAmount(it.unitPrice.cents), it.vatRate)
            },
            totals = InvoiceCalculator.totals(invoice.lines),
            editingId = invoice.id,
            editingNumber = invoice.number,
        )
    }

    private fun plainQuantity(milli: Long): String {
        val fraction = (milli % 1000).toString().padStart(3, '0').trimEnd('0')
        return if (fraction.isEmpty()) (milli / 1000).toString() else "${milli / 1000}.$fraction"
    }

    private fun plainAmount(cents: Long) = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    @OptIn(ExperimentalUuidApi::class)
    fun save() {
        val s = _state.value
        if (!s.isValid) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            val invoice = Invoice(
                id = s.editingId ?: Uuid.random().toString(),
                number = s.editingNumber ?: container.invoiceRepository.nextNumber(s.issueDate.year),
                clientId = s.clientId,
                clientName = s.clientName.trim(),
                issueDate = s.issueDate,
                dueDate = s.dueDate,
                lines = s.lines.mapNotNull(::toDomain),
            )
            val error = container.saveInvoice(invoice)
            if (error == null && s.editingId == null) _justCreated.value = CreatedInvoice(invoice.id, invoice.number)
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
