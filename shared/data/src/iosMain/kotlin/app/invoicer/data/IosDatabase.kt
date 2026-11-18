package app.invoicer.data

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.invoicer.data.db.InvoicerDatabase

fun createIosDatabase(): InvoicerDatabase =
    createDatabase(NativeSqliteDriver(InvoicerDatabase.Schema, "invoicer.db"))
