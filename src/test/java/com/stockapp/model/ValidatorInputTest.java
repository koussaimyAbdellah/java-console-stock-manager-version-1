package com.stockapp.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.stockapp.exception.InvalidInputException;
import com.stockapp.model.ProductAuditLog.OPERATIONTYPE;

class ValidatorInputTest {

    @Test
    void acceptsValidProductValues() {
        assertDoesNotThrow(() -> ValidatorInput.inputCheekValidionProduct(
                "Test Product", 10.50, 0));
    }

    @Test
    void rejectsZeroPrice() {
        assertThrows(InvalidInputException.class,
                () -> ValidatorInput.inputCheekValidionProduct("Test Product", 0.0, 1));
    }

    @Test
    void rejectsNegativeStock() {
        assertThrows(InvalidInputException.class,
                () -> ValidatorInput.inputstockValidation(-1));
    }

    @Test
    void rejectsInvalidEmail() {
        assertThrows(InvalidInputException.class,
                () -> ValidatorInput.inputEmailValidation("not-an-email"));
    }

    @Test
    void acceptsCompleteAuditLogValues() {
        assertDoesNotThrow(() -> ValidatorInput.nullValuesAdutProductLog(
                1, OPERATIONTYPE.RESTOCK, "console", java.time.LocalDateTime.now()));
    }
}
