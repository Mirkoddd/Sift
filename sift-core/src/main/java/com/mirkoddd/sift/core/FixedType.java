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

import com.mirkoddd.sift.core.dsl.CharacterClassConnectorStep;
import com.mirkoddd.sift.core.dsl.ConnectorStep;

class FixedType extends AbstractTypeStep<ConnectorStep, CharacterClassConnectorStep> {

    private final ConnectorStep normalConnector;
    private final CharacterClassConnectorStep charClassConnector;

    FixedType(PatternAssembler assembler,
              ConnectorStep normalConnector,
              CharacterClassConnectorStep charClassConnector) {
        super(assembler);
        this.normalConnector = normalConnector;
        this.charClassConnector = charClassConnector;
    }

    @Override
    protected ConnectorStep getNormalConnector() {
        return normalConnector;
    }

    @Override
    protected CharacterClassConnectorStep getCharacterClassConnector() {
        return charClassConnector;
    }
}
