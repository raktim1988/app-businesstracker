package app.invoicer.data

import app.invoicer.domain.repository.ClientRepository
import app.invoicer.domain.repository.InvoiceRepository
import app.invoicer.domain.usecase.ObserveInvoicesUseCase
import app.invoicer.domain.usecase.ObserveInvoicesUseCaseImpl
import app.invoicer.domain.usecase.SaveInvoiceUseCase
import app.invoicer.domain.usecase.SaveInvoiceUseCaseImpl

/** Manual DI shared by Android and (later) iOS. Swap for Koin when the graph grows. */
class AppContainer(
    val invoiceRepository: InvoiceRepository = InMemoryInvoiceRepository(),
    val clientRepository: ClientRepository = InMemoryClientRepository(),
) {
    val observeInvoices: ObserveInvoicesUseCase = ObserveInvoicesUseCaseImpl(invoiceRepository)
    val saveInvoice: SaveInvoiceUseCase = SaveInvoiceUseCaseImpl(invoiceRepository)
}
