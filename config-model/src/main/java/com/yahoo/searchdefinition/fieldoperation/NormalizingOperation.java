// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.searchdefinition.fieldoperation;

import com.yahoo.searchdefinition.document.NormalizeLevel;
import com.yahoo.searchdefinition.document.SDField;

/**
 * @author <a href="mailto:einarmr@yahoo-inc.com">Einar M R Rosenvinge</a>
 */
public class NormalizingOperation implements FieldOperation {
    private NormalizeLevel.Level level;

    public NormalizingOperation(String setting) {
        if ("none".equals(setting)) {
            this.level = NormalizeLevel.Level.NONE;
        } else if ("codepoint".equals(setting)) {
            this.level = NormalizeLevel.Level.CODEPOINT;
        } else if ("lowercase".equals(setting)) {
            this.level = NormalizeLevel.Level.LOWERCASE;
        } else if ("accent".equals(setting)) {
            this.level = NormalizeLevel.Level.ACCENT;
        } else if ("all".equals(setting)) {
            this.level = NormalizeLevel.Level.ACCENT;
        } else {
            throw new IllegalArgumentException("invalid normalizing setting: "+setting);
        }
    }

    public void apply(SDField field) {
        field.setNormalizing(new NormalizeLevel(level, true));
    }
}
