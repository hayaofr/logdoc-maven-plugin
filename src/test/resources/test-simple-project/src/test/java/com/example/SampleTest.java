package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SampleTest {

    @ParameterizedTest(name = "Testing with {0} and {1}")
    @MethodSource(value = {"provideArguments1", "provideArguments2"})
    @DisplayName("Parameterized Test with MethodSource")
    void parameterizedTestWithMethodSource(String input1, String input2) {}

    @ParameterizedTest(name = "Testing Enum with {0}")
    @EnumSource(names = {"ENUM1", "ENUM2"})
    @DisplayName("Parameterized Test with EnumSource")
    void parameterizedTestWithEnumSource(String test) {}

    static Stream<Arguments> provideArguments1() {
        return Stream.of(
                arguments(Named.of("Label 1", "value1"), "param2")
        );
    }

    static Stream<Arguments> provideArguments2() {
        return Stream.of(
                arguments(Named.of("Label 2", "value2"), "param2")
        );
    }


}
