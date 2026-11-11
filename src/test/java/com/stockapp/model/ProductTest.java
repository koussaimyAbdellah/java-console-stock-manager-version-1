package com.stockapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void calculatesInventoryValueAndInclusiveLowStock() throws Exception {
        Phone product = new Phone(
                "NovaTech", "N1", 128, 1, 10, "Nova Phone", 25.0, 1,
                Categorys.PHONE, LocalDateTime.now(), null, LocalDateTime.now());

        assertEquals(250.0, product.calculateValue());
        assertTrue(product.isLowStock(10));
        assertFalse(product.isLowStock(9));
    }

    @Test
    void rejectsNegativeLowStockThreshold() throws Exception {
        Phone product = new Phone(
                "NovaTech", "N1", 128, 1, 10, "Nova Phone", 25.0, 1,
                Categorys.PHONE, LocalDateTime.now(), null, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> product.isLowStock(-1));
    }
}
