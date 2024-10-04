package org.example.callbacks;

import org.example.models.Match;

import java.util.List;

public interface FetchMatchHistoryCallback {
    void onSuccess(List<Match> matches);
    void onFailure(String errorMessage);
}
