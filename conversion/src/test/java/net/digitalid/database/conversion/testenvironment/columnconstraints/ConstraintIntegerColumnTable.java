/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.database.conversion.testenvironment.columnconstraints;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;

@GenerateBuilder
@GenerateTableConverter
public class ConstraintIntegerColumnTable  {
    
    @MultipleOf(7)
    public final @Nonnull Integer value;
    
    protected ConstraintIntegerColumnTable(@Nonnull Integer value) {
        this.value = value;
    }
    
    @Pure
    @Recover
    public static @Nonnull ConstraintIntegerColumnTable get(@Nonnull Integer value) {
        return new ConstraintIntegerColumnTable(value);
    }
    
}
