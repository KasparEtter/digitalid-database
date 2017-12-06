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
package net.digitalid.database.android.encoder;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;

import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.encoder.SQLActionEncoder;

import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
public abstract class AndroidInsertEncoder extends AndroidInsertUpdateEncoder implements SQLActionEncoder {
    
    protected AndroidInsertEncoder(@Nonnull SQLiteDatabase sqLiteDatabase, @Nonnull String tableName, @Nonnull String[] columnNames) {
        super(sqLiteDatabase, tableName, columnNames);
    }
    
    @Override
    @PureWithSideEffects
    public void execute() throws DatabaseException {
        sqliteDatabase.insert(tableName, null, contentValues);
    }
    
}
