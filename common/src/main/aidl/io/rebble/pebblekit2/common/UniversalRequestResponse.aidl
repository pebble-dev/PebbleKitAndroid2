package io.rebble.pebblekit2.common;

import io.rebble.pebblekit2.common.SendDataCallback;

interface UniversalRequestResponse {
    void request(in Bundle data, in SendDataCallback callback);
}
