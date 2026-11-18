package app.invoicer.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FieldValidationTest {
    @Test
    fun `parseScaled accepts comma and dot and rejects too many decimals`() {
        assertEquals(7500L, parseScaled("7,5", 3))
        assertEquals(8500L, parseScaled("85", 2))
        assertEquals(1050L, parseScaled("10.5", 2))
        assertNull(parseScaled("1,2345", 3))
        assertNull(parseScaled("abc", 2))
        assertNull(parseScaled("-1", 2))
    }

    @Test
    fun `required and range rules`() {
        assertNotNull(FieldValidation.client(" "))
        assertNotNull(FieldValidation.client("A"))
        assertNull(FieldValidation.client("Acme"))
        assertNotNull(FieldValidation.quantity("0"))
        assertNotNull(FieldValidation.quantity("100000"))
        assertNull(FieldValidation.quantity("7,5"))
        assertNotNull(FieldValidation.rate("0,00"))
        assertNotNull(FieldValidation.rate("1,234"))
        assertNull(FieldValidation.rate("85"))
        assertNotNull(FieldValidation.description(""))
    }

    @Test
    fun `client name rejects special characters but allows accents and business punctuation`() {
        assertEquals(FieldError.ClientInvalidChars, FieldValidation.client("Acme @#!"))
        assertEquals(FieldError.ClientInvalidChars, FieldValidation.client("Acme<script>"))
        assertNull(FieldValidation.client("Café Müller"))
        assertNull(FieldValidation.client("Bakker & Zn."))
        assertNull(FieldValidation.client("O'Neil-Jansen 2"))
    }
}
