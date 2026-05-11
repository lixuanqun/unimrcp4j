package io.github.javamrcp.server;

import io.github.javamrcp.core.MrcpSessionId;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for active MRCP sessions.
 */
public final class MrcpSessionRegistry {
    private final int maxSessions;
    private final Map<MrcpSessionId, MrcpServerSession> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, MrcpSessionId> sessionIdsByCallId = new ConcurrentHashMap<>();

    public MrcpSessionRegistry(int maxSessions) {
        if (maxSessions <= 0) {
            throw new IllegalArgumentException("maxSessions must be positive");
        }
        this.maxSessions = maxSessions;
    }

    public MrcpServerSession register(MrcpServerSession session) {
        if (sessionsById.size() >= maxSessions && !sessionsById.containsKey(session.id())) {
            throw new MrcpSessionLimitExceededException("Maximum MRCP sessions exceeded: " + maxSessions);
        }
        MrcpServerSession existing = sessionsById.putIfAbsent(session.id(), session);
        if (existing != null) {
            return existing;
        }
        sessionIdsByCallId.put(session.callId(), session.id());
        return session;
    }

    public Optional<MrcpServerSession> find(MrcpSessionId sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    public Optional<MrcpServerSession> findByCallId(String callId) {
        MrcpSessionId sessionId = sessionIdsByCallId.get(callId);
        return sessionId == null ? Optional.empty() : find(sessionId);
    }

    public Optional<MrcpServerSession> remove(MrcpSessionId sessionId) {
        MrcpServerSession removed = sessionsById.remove(sessionId);
        if (removed != null) {
            sessionIdsByCallId.remove(removed.callId(), sessionId);
            removed.terminated();
        }
        return Optional.ofNullable(removed);
    }

    public int size() {
        return sessionsById.size();
    }

    public Collection<MrcpServerSession> sessions() {
        return sessionsById.values();
    }
}
