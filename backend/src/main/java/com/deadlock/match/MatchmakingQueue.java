package com.deadlock.match;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class MatchmakingQueue {

    // userId -> QueueEntry. Only one entry per user.
    private final ConcurrentMap<Long, QueueEntry> entries = new ConcurrentHashMap<>();

    public boolean add(QueueEntry entry) {
        return entries.putIfAbsent(entry.userId(), entry) == null;
    }

    public Optional<QueueEntry> remove(Long userId) {
        return Optional.ofNullable(entries.remove(userId));
    }

    public boolean contains(Long userId) {
        return entries.containsKey(userId);
    }

    public Optional<QueueEntry> get(Long userId) {
        return Optional.ofNullable(entries.get(userId));
    }

    public List<QueueEntry> snapshotByJoinTime() {
        return entries.values().stream()
                .sorted((a, b) -> a.joinedAt().compareTo(b.joinedAt()))
                .toList();
    }

    public int size() {
        return entries.size();
    }
}
