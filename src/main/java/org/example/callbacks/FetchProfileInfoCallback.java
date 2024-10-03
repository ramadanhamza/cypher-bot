package org.example.callbacks;

public interface FetchProfileInfoCallback {
    void onSuccess(String name, String tag, String region, int accountLevel, String cardIcon);
    void onFailure(String errorMessage);
}
