#!/bin/sh
# Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
cd src/cpp
make FASTOS_DIR=${autobuild_installroot} INSTALL_DIR=${autobuild_installroot}
