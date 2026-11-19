package app.invoicer.data

import app.invoicer.data.db.Business_profile
import app.invoicer.data.db.Client
import app.invoicer.data.db.Invoice
import app.invoicer.data.db.InvoicerDatabase
import app.invoicer.data.db.Invoice_line

/** Raw row access for the sync layer: keeps the stored `updated_at` and `version` instead of bumping them. */
class SyncStore(private val db: InvoicerDatabase) {
    private val q get() = db.invoicerQueries

    fun clients(): List<Client> = q.selectClients().executeAsList()

    fun invoices(): List<Invoice> = q.selectInvoices().executeAsList()

    fun lines(): List<Invoice_line> = q.selectAllLines().executeAsList()

    fun profile(): Business_profile? = q.selectProfile().executeAsOneOrNull()

    fun putClient(c: Client) {
        q.upsertClient(c.id, c.name, c.email, c.kvk_number, c.btw_number, c.payment_term_days, c.updated_at, c.version)
    }

    fun putInvoice(i: Invoice, lines: List<Invoice_line>) {
        q.transaction {
            q.upsertInvoice(i.id, i.number, i.client_id, i.client_name, i.status, i.issue_date, i.due_date, i.updated_at, i.version)
            q.deleteLines(i.id)
            lines.forEach {
                q.insertLine(i.id, it.position, it.description, it.quantity_milli, it.unit_price_cents, it.vat_percent)
            }
        }
    }

    fun putProfile(p: Business_profile) {
        q.upsertProfile(p.name, p.address, p.kvk_number, p.btw_number, p.iban, p.default_payment_term_days, p.updated_at, p.version)
    }
}
