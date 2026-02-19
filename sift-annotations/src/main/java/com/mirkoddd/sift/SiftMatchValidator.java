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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Engine that validates a {@link CharSequence} against a rule defined by a {@link SiftRegexProvider}.
 * <p>
 * For performance optimization, this validator compiles the regular expression into a
 * {@link java.util.regex.Pattern} only once during initialization, rather than on every validation call.
 * @author Mirko Dimartino
 * @since 1.1.0
 */
public final class SiftMatchValidator implements ConstraintValidator<SiftMatch, CharSequence> {

    private Pattern compiledPattern;

    @Override
    public void initialize(SiftMatch constraintAnnotation) {
        try {
            SiftRegexProvider provider = constraintAnnotation.value().getDeclaredConstructor().newInstance();
            String rawRegex = provider.getRegex();
            this.compiledPattern = Pattern.compile(rawRegex);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SiftRegexProvider: " + constraintAnnotation.value().getName() + ". Ensure it has a public no-args constructor.", e);
        }
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return compiledPattern.matcher(value).matches();
    }
}