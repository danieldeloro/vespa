#!/bin/sh
# Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

class=$1
guard=`echo $class | tr 'a-z' 'A-Z'`

cat <<EOF
// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

namespace search {
namespace features {

class $class
{
private:
    $class(const $class &);
    $class &operator=(const $class &);
public:
    $class();
    virtual ~$class();
};

} // namespace features
} // namespace search

EOF
