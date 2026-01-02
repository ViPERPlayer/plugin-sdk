// IConnectCallback.aidl
package com.viperplayer.plugin;

interface IConnectCallback {
    void onSuccess();
    void onFailure(int errorCode, String message);
}