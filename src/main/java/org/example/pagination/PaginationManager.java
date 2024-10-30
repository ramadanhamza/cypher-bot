package org.example.pagination;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaginationManager {

    private final Map<Long, PaginationContext> paginationMap = new ConcurrentHashMap<>();

    public void addPaginationContext(Long interactionId, PaginationContext context) {
        paginationMap.put(interactionId, context);
    }

    public PaginationContext getPaginationContext(Long interactionId) {
        return paginationMap.get(interactionId);
    }
}
