package app.invoicer.data

import app.invoicer.data.db.InvoicerDatabase
import app.invoicer.domain.repository.BusinessProfileRepository
import app.invoicer.domain.repository.ClientRepository
import app.invoicer.domain.repository.InvoiceRepository
import app.invoicer.domain.usecase.ObserveInvoicesUseCase
import app.invoicer.domain.usecase.ObserveInvoicesUseCaseImpl
import app.invoicer.domain.usecase.SaveInvoiceUseCase
import app.invoicer.domain.usecase.SaveInvoiceUseCaseImpl

/** Manual DI shared by Android and (later) iOS. Swap for Koin when the graph grows. */
class AppContainer(
    val invoiceRepository: InvoiceRepository,
    val clientRepository: ClientRepository,
    val profileRepository: BusinessProfileRepository,
    val syncStore: SyncStore? = null,
) {
    constructor(db: InvoicerDatabase) : this(SqlInvoiceRepository(db), SqlClientRepository(db), SqlBusinessProfileRepository(db), SyncStore(db))

    val observeInvoices: ObserveInvoicesUseCase = ObserveInvoicesUseCaseImpl(invoiceRepository)
    val saveInvoice: SaveInvoiceUseCase = SaveInvoiceUseCaseImpl(invoiceRepository)
}
