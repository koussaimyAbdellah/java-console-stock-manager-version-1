package com.stockapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.stockapp.exception.InvalidInputException;

class CategorysTest {

    @Test
    void parsesCategoryWithoutCaseSensitivity() throws InvalidInputException {
        assertEquals(Categorys.PHONE, Categorys.getCategoryFrom("PhOnE"));
    }

    @Test
    void rejectsBlankCategory() {
        assertThrows(InvalidInputException.class, () -> Categorys.getCategoryFrom(" "));
    }

    @Test
    void rejectsUnknownCategory() {
        assertThrows(InvalidInputException.class, () -> Categorys.getCategoryFrom("laptop"));
    }
}
