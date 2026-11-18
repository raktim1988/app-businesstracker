package app.invoicer.data

import app.cash.sqldelight.db.SqlDriver
import app.invoicer.data.db.InvoicerDatabase

fun createDatabase(driver: SqlDriver): InvoicerDatabase = InvoicerDatabase(driver)
