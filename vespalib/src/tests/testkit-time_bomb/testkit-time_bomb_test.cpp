// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include <vespa/vespalib/testkit/test_kit.h>

TEST_MT_F("use time bomb in multi-threaded test", 4, vespalib::TimeBomb(60)) {
    EXPECT_TRUE(true);
}

TEST_MAIN() { TEST_RUN_ALL(); }
