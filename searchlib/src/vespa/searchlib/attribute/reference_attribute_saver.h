// Copyright 2017 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "attributesaver.h"
#include <vespa/document/base/globalid.h>
#include <vespa/searchlib/datastore/unique_store.h>
#include <vespa/searchlib/datastore/unique_store_saver.h>
#include <vespa/searchlib/common/rcuvector.h>
#include "reference_attribute.h"

namespace search {
namespace attribute {

/*
 * Class for saving a reference attribute to disk or memory buffers.
 *
 * .udat file contains sorted unique values after generic header, in
 * host byte order.
 *
 * .dat file contains enum values after generic header, in host byte order.
 *
 * enum value 0 means value not set.
 * enum value 1 means the first unique value.
 * enum value n means the nth unique value.
 */
class ReferenceAttributeSaver : public AttributeSaver
{
private:
    using EntryRef = search::datastore::EntryRef;
    using GlobalId = document::GlobalId;
    using IndicesCopyVector = ReferenceAttribute::IndicesCopyVector;
    using Store = ReferenceAttribute::Store;
    using Reference = ReferenceAttribute::Reference;
    using Saver = Store::Saver;
    IndicesCopyVector _indices;
    const Store &_store;
    Saver _saver;

    virtual bool onSave(IAttributeSaveTarget &saveTarget) override;
public:
    ReferenceAttributeSaver(vespalib::GenerationHandler::Guard &&guard,
                            const attribute::AttributeHeader &header,
                            IndicesCopyVector &&indices,
                            const Store &store);

    virtual ~ReferenceAttributeSaver();
};

} // namespace search::attribute
} // namespace search
