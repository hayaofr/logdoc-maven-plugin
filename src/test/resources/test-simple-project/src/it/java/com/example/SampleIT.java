package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName(value = "Sample IT Test Simple Project")
public class SampleTest {
    @Test
    @DisplayName("Simple Test Method")
    void simpleTest() {}

    @ParameterizedTest(name = "Test avec {0}")
    @ValueSource(strings = {"value1", "value2"})
    @DisplayName("Parameterized Test with Values")
    void parameterizedTestWithValues(String value) {}

    @ParameterizedTest(name = "Test avec {0}")
    @EnumSource(MyEnum.class)
    @DisplayName("Parameterized Test with Enum Source Class")
    void parameterizedTestWithEnumSourceClass(String value) {}

}
