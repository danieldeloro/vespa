// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#include <vespa/vespalib/testkit/test_kit.h>

void subtest() {
    EXPECT_TRUE(true);
}

TEST_MAIN() {
    TEST_DO(subtest());
}
