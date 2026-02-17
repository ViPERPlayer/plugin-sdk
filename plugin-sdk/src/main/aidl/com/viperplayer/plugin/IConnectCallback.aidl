// IConnectCallback.aidl
package com.viperplayer.plugin;

interface IConnectCallback {
    void onSuccess() = 1;
    void onFailure(int errorCode, String message) = 2;
}