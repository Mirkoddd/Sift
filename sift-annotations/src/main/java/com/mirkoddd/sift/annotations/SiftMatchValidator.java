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
package com.mirkoddd.sift.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

import java.util.regex.Pattern;

/**
 * Engine that validates a {@link CharSequence} against a rule defined by a {@link SiftRegexProvider}.
 * <p>
 * For performance optimization, this validator compiles the regular expression into a
 * {@link java.util.regex.Pattern} only once during initialization, rather than on every validation call.
 * <p>
 * <b>Engine Note:</b> This validator always uses the standard JDK regex engine
 * ({@code java.util.regex}), regardless of the engine configured elsewhere in the application.
 * This is an architectural constraint of the Jakarta Validation lifecycle: the validator
 * is instantiated by the validation container without any Sift execution context.
 * <p>
 * As a consequence, ReDoS protection provided by alternative engines (such as
 * {@code Re2jEngine}) does <b>not</b> apply here. If you are validating untrusted
 * input against a structurally complex pattern in a security-sensitive context,
 * consider wrapping your pattern with {@link com.mirkoddd.sift.core.dsl.SiftPattern#preventBacktracking()}
 * before exposing it via {@link SiftRegexProvider#getRegex()}.
 *
 * @author Mirko Dimartino
 * @since 1.1.0
 */
public final class SiftMatchValidator implements ConstraintValidator<SiftMatch, CharSequence> {

    private Pattern compiledPattern;

    @Override
    @SuppressWarnings("MagicConstant")
    public void initialize(SiftMatch constraintAnnotation) {
        try {
            SiftRegexProvider provider = constraintAnnotation.value().getDeclaredConstructor().newInstance();
            String rawRegex = provider.getRegex();

            int combinedFlags = 0;
            for (SiftMatchFlag flag : constraintAnnotation.flags()) {
                combinedFlags |= flag.getValue();
            }

            this.compiledPattern = Pattern.compile(rawRegex, combinedFlags);
        } catch (ReflectiveOperationException e) {
            throw new ValidationException("Failed to initialize SiftRegexProvider: " + constraintAnnotation.value().getName() + ". Ensure it has a public no-args constructor.", e);
        }
    }

    // Note: This validator intentionally uses Matcher.matches() for strict full-field validation.
    // This enforces the exact same semantics as SiftPattern.matchesEntire(),
    // ensuring the entire input strictly conforms to the of rather than just containing it (containsMatchIn).
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return compiledPattern.matcher(value).matches();
    }
}