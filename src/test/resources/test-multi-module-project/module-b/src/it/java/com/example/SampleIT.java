package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Sample IT Test Module B")
public class SampleTest {
    @ParameterizedTest(name = "Testing with {0} and {1}")
    @CsvSource({"A,1", "B,2"})
    @DisplayName("Parameterized Test with CSV")
    void testCsv(String a, int b) {}

    @ParameterizedTest(name = "Testing with {0} and {1}")
    @CsvSource(value = {"C,3", "D,4"})
    @DisplayName("Parameterized Test with CSV value")
    void testCsv(String a, int b) {}

}
