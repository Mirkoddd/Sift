/*
 * Copyright 2026 Mirko Dimartino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mirkoddd.sift;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test suite for the {@link SiftMatch} annotation and its associated {@link SiftMatchValidator}.
 * <p>
 * This class ensures that the custom Jakarta Validation constraint properly instantiates
 * the provided {@link SiftRegexProvider}, compiles the regex only once, and correctly
 * validates object fields using the Hibernate Validator engine.
 */
class SiftAnnotationTest {

    private static Validator validator;

    /**
     * Bootstraps the standard Bean Validation engine before running the tests.
     * We use the default factory, which will pick up Hibernate Validator from the classpath.
     */
    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ===================================================================================
    // TEST FIXTURES (Mocks and DTOs)
    // ===================================================================================

    /**
     * A mock rule simulating a user-defined Sift regex.
     * It enforces a string to contain only digits from start to end.
     */
    public static class NumericOnlyRule implements SiftRegexProvider {
        @Override
        public String getRegex() {
            return Sift.fromStart()
                    .oneOrMore().digits()
                    .shake();
        }
    }

    /**
     * A mock Data Transfer Object (DTO) simulating a real-world usage of the annotation.
     */
    public record MockRegistrationDto(
            @SiftMatch(
                    value = NumericOnlyRule.class,
                    message = "The provided value must contain only numeric characters."
            )
            String pinCode
    ) {}

    // ===================================================================================
    // TEST CASES
    // ===================================================================================

    @Test
    @DisplayName("Should pass validation when the string matches the Sift rule")
    void whenValueMatchesRule_thenValidationPasses() {
        // Arrange
        MockRegistrationDto validDto = new MockRegistrationDto("123456");

        // Act
        Set<ConstraintViolation<MockRegistrationDto>> violations = validator.validate(validDto);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation errors for a valid numeric string.");
    }

    @Test
    @DisplayName("Should fail validation and return custom message when string does not match")
    void whenValueDoesNotMatchRule_thenValidationFailsWithCustomMessage() {
        // Arrange
        MockRegistrationDto invalidDto = new MockRegistrationDto("123ABC");

        // Act
        Set<ConstraintViolation<MockRegistrationDto>> violations = validator.validate(invalidDto);

        // Assert
        assertEquals(1, violations.size(), "Expected exactly one validation error.");

        ConstraintViolation<MockRegistrationDto> violation = violations.iterator().next();
        assertEquals("The provided value must contain only numeric characters.", violation.getMessage());
        assertEquals("pinCode", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("Should pass validation when the value is null (delegating to @NotNull)")
    void whenValueIsNull_thenValidationPasses() {
        // Arrange
        MockRegistrationDto nullDto = new MockRegistrationDto(null);

        // Act
        Set<ConstraintViolation<MockRegistrationDto>> violations = validator.validate(nullDto);

        // Assert
        assertTrue(violations.isEmpty(),
                "Expected null values to pass. Null checks should be handled by standard @NotNull annotations.");
    }

    // ===================================================================================
    // MARKERS FOR ADVANCED JAKARTA FEATURES (Groups & Payloads)
    // ===================================================================================

    // A mock group to test conditional validation
    public interface RegistrationPhase {}

    // A mock payload to test the severity level of the error
    public interface CriticalError extends Payload {}

    /**
     * A mock DTO testing Jakarta's advanced groups and payload features.
     */
    public record AdvancedDto(
            @SiftMatch(
                    value = NumericOnlyRule.class,
                    groups = RegistrationPhase.class,
                    payload = CriticalError.class
            )
            String value
    ) {}

    @Test
    @DisplayName("Should correctly handle Jakarta Validation groups and payloads")
    void whenUsingGroupsAndPayloads_thenHibernateProcessesThemCorrectly() {
        AdvancedDto dto = new AdvancedDto("NotANumber");

        // 1. Test Groups: Validate using the specific "RegistrationPhase" group
        Set<ConstraintViolation<AdvancedDto>> violations = validator.validate(dto, RegistrationPhase.class);

        assertEquals(1, violations.size(), "Should trigger validation for the specified group");

        ConstraintViolation<AdvancedDto> violation = violations.iterator().next();

        // 2. Test Payloads: Verify that the error contains our "CriticalError" payload
        Set<Class<? extends Payload>> payloads = violation.getConstraintDescriptor().getPayload();

        assertTrue(payloads.contains(CriticalError.class), "The violation should carry the CriticalError payload");
    }

    // ===================================================================================
    // ERROR HANDLING TEST (For 100% Line Coverage)
    // ===================================================================================

    /**
     * A mock rule with a private constructor to simulate instantiation failure.
     */
    public static class BrokenRule implements SiftRegexProvider {
        private BrokenRule() {
            // Private constructor will cause reflection to fail
        }

        @Override
        public String getRegex() {
            return ".*";
        }
    }

    /**
     * A mock DTO testing the error handling when a rule is invalid.
     */
    public record BrokenDto(
            @SiftMatch(BrokenRule.class)
            String value
    ) {}

    @Test
    @DisplayName("Should throw an exception when the Rule class cannot be instantiated")
    void whenRuleIsInvalid_thenInitializationThrowsException() {
        BrokenDto badDto = new BrokenDto("test");

        // Hibernate Validator wraps our custom RuntimeException in a ValidationException
        Exception exception = Assertions.assertThrows(
                ValidationException.class,
                () -> validator.validate(badDto)
        );

        // Verify that the cause contains our custom error message from the catch block
        assertTrue(exception.getCause().getMessage().contains("Failed to initialize SiftRegexProvider"),
                "The exception message should match the one thrown in the catch block");
    }

}