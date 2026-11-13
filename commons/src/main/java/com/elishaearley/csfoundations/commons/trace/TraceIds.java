package com.elishaearley.csfoundations.commons.trace;

import java.util.UUID;

public final class TraceIds {
    public static final String HEADER = "X-Trace-Id";

    private TraceIds() {}

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
