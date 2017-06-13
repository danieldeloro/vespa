// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.document;

import com.yahoo.document.datatypes.CollectionFieldValue;
import com.yahoo.document.datatypes.FieldValue;
import com.yahoo.vespa.objects.Ids;
import com.yahoo.vespa.objects.ObjectVisitor;

import java.util.List;

/**
 * @author <a href="mailto:einarmr@yahoo-inc.com">Einar M R Rosenvinge</a>
 */
public abstract class CollectionDataType extends DataType {
    // The global class identifier shared with C++.
    public static int classId = registerClass(Ids.document + 53, CollectionDataType.class);

    private DataType nestedType;

    protected CollectionDataType(String name, int code, DataType nestedType) {
        super(name, code);
        this.nestedType = nestedType;
    }

    @Override
    public abstract CollectionFieldValue createFieldValue();

    @Override
    public CollectionDataType clone() {
        CollectionDataType type = (CollectionDataType) super.clone();
        type.nestedType = nestedType.clone();
        return type;
    }

    @SuppressWarnings("deprecation")
    public DataType getNestedType() {
        return nestedType;
    }

    @Override
    protected FieldValue createByReflection(Object arg) { return null; }

    /**
     * Sets the nested type of this CollectionDataType.&nbsp;WARNING! Do not use! Only to be used by config system!
     */
    public void setNestedType(DataType nestedType) {
        this.nestedType = nestedType;
    }

    @Override
    public PrimitiveDataType getPrimitiveType() {
        return nestedType.getPrimitiveType();
    }

    @Override
    public boolean isValueCompatible(FieldValue value) {
        if (!(value instanceof CollectionFieldValue)) {
            return false;
        }
        CollectionFieldValue cfv = (CollectionFieldValue) value;
        if (equals(cfv.getDataType())) {
            //the field value if of this type:
            return true;
        }
        return false;
    }

    @Override
    protected void register(DocumentTypeManager manager, List<DataType> seenTypes) {
        seenTypes.add(this);
        if (!seenTypes.contains(getNestedType())) {
            //we haven't seen this one before, register it:
            getNestedType().register(manager, seenTypes);
        }
        super.register(manager, seenTypes);
    }

    @Override
    public void visitMembers(ObjectVisitor visitor) {
        super.visitMembers(visitor);
        visitor.visit("nestedType", nestedType);
    }

    @Override
    public boolean isMultivalue() { return true; }

}
