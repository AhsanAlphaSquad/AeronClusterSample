package aeron_cluster.domain;

import java.util.ArrayList;
import java.util.List;

import org.agrona.collections.Object2ObjectHashMap;

public class SimpleMessageStore {
    
    private static SimpleMessageStore instance = null;

    public static SimpleMessageStore getInstance() {
        if (instance == null)
            instance = new SimpleMessageStore();
        return instance;
    }

    private SimpleMessageStore()
    {
    }

    private static final Object2ObjectHashMap<String, List<SimpleMessage>> store = new Object2ObjectHashMap<>();

    public void put(String sessionId, SimpleMessage message) {
        store.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    public List<SimpleMessage> get(String sessionId) {
        return store.get(sessionId);
    }

    public void remove(String sessionId) {
        store.remove(sessionId);
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }

}
