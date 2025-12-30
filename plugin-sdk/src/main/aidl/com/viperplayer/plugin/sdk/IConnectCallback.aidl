// IConnectCallback.aidl
package com.viperplayer.plugin.sdk;

interface IConnectCallback {
    void onSuccess();
    void onFailure(int errorCode, String message);
}