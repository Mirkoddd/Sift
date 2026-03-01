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
package com.mirkoddd.sift.core;

import com.mirkoddd.sift.core.dsl.VariableCharacterClassConnectorStep;
import com.mirkoddd.sift.core.dsl.VariableConnectorStep;

class VariableType extends AbstractTypeStep<VariableConnectorStep, VariableCharacterClassConnectorStep> {

    VariableType(SiftBuilder parentBuilder) {
        super(parentBuilder);
    }

    @Override
    protected VariableConnectorStep getNormalConnector(SiftBuilder clone) {
        return clone;
    }

    @Override
    protected VariableCharacterClassConnectorStep getCharacterClassConnector(SiftBuilder clone) {
        return clone;
    }
}
