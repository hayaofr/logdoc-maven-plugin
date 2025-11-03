package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Sample IT Test Module A")
public class SampleTest {
    @Test
    @DisplayName("Simple Test Method")
    void simpleTest() {}

    @ParameterizedTest(name = "Test avec {0}")
    @ValueSource(strings = {"value1", "value2"})
    @DisplayName("Parameterized Test with Values")
    void parameterizedTestWithValues(String value) {}

}
