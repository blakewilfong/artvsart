package com.artvsart.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class BalancedPoolSelector {

    public <T, K> List<T> select(
            List<T> candidates,
            int limit,
            Function<T, K> bucketKey
    ) {
        if (limit < 0) {
            throw new IllegalArgumentException(
                    "Selection limit cannot be negative"
            );
        }

        Map<K, ArrayDeque<T>> buckets = new LinkedHashMap<>();

        for (T candidate : candidates) {
            buckets.computeIfAbsent(
                    bucketKey.apply(candidate),
                    ignored -> new ArrayDeque<>()
            ).add(candidate);
        }

        List<T> selected = new ArrayList<>();
        boolean selectedFromBucket;

        do {
            selectedFromBucket = false;

            for (ArrayDeque<T> bucket : buckets.values()) {
                if (selected.size() >= limit) {
                    return selected;
                }

                T candidate = bucket.pollFirst();

                if (candidate != null) {
                    selected.add(candidate);
                    selectedFromBucket = true;
                }
            }
        } while (selectedFromBucket);

        return selected;
    }
}
