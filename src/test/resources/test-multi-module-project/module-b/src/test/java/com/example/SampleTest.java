package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Sample Unit Test Module B")
public class SampleTest {
    @Nested
    @DisplayName("Addition Tests")
    class Addition {
        @Test
        @DisplayName("adds 2 numbers")
        void addsTwoNumbers() {}
    }

    @Nested
    @DisplayName("Subtraction Tests")
    class Subtraction {
        @ParameterizedTest(name = "subtract {0} - {1} = {2}")
        @CsvSource({"2,1,1", "5,3,2"})
        void subtractionParameterized(int a, int b, int expected) {}
    }
}
