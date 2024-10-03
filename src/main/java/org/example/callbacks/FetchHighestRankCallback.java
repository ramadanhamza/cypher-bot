package org.example.callbacks;

public interface FetchHighestRankCallback {
    void onSuccess(String highestRank);
    void onFailure(String errorMessage);
}
