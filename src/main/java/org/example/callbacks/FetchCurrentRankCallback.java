package org.example.callbacks;

public interface FetchCurrentRankCallback {
    void onSuccess(String currentRank, String rankIcon);
    void onFailure(String errorMessage);
}
