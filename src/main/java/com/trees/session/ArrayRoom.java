package com.trees.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArrayRoom {

    private static final Logger log = LoggerFactory.getLogger(ArrayRoom.class);
    private static final Path INDEX_FILE = Paths.get("pool_index.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ArrayRoom INSTANCE = new ArrayRoom();

    private final Map<String, ArrayBluePrint> pool = new ConcurrentHashMap<>();

    
    private ArrayRoom() {
        loadIndex();
    }

    public static ArrayRoom getInstance() {
        return INSTANCE;
    }

    public void add(ArrayBluePrint entry) {
        if (pool.containsKey(entry.getName())) {
            throw new IllegalArgumentException("An array with name '" + entry.getName() + "' already exists.");
        }
        pool.put(entry.getName(), entry);
        saveIndex();
        log.debug("Added new array to room: {}", entry.getName());
    }

    public void remove(String name) {
        ArrayBluePrint removed = pool.remove(name);
        if (removed != null) {
            saveIndex();
            log.debug("Removed array from room: {}", name);
        }
    }

    public ArrayBluePrint get(String name) {
        return pool.get(name);
    }

    public List<ArrayBluePrint> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(pool.values()));
    }

    private void saveIndex() {
        try {
            List<ArrayBluePrintState> states = new ArrayList<>();
            for (ArrayBluePrint entry : pool.values()) {
                states.add(new ArrayBluePrintState(entry));
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(INDEX_FILE.toFile(), states);
        } catch (IOException e) {
            log.error("Failed to save room index to {}", INDEX_FILE, e);
        }
    }

    private void loadIndex() {
        if (!Files.exists(INDEX_FILE)) {
            return;
        }
        try {
            List<ArrayBluePrintState> states = mapper.readValue(INDEX_FILE.toFile(), new TypeReference<>() {});
            for (ArrayBluePrintState state : states) {
                ArrayBluePrint entry = new ArrayBluePrint(
                        state.name,
                        state.method,
                        state.size,
                        state.seed,
                        state.createdAt,
                        state.sessionFilePath != null ? Paths.get(state.sessionFilePath) : null,
                        state.lifecycle
                );
                pool.put(entry.getName(), entry);
            }
            log.debug("Loaded {} arrays from room index.", pool.size());
        } catch (IOException e) {
            log.error("Failed to load room index from {}", INDEX_FILE, e);
        }
    }

    private static class ArrayBluePrintState {
        public String name;
        public ArrayGenerator.GenerationMethod method;
        public int size;
        public long seed;
        public long createdAt;
        public String sessionFilePath;
        public ArrayBluePrint.Lifecycle lifecycle;

        public ArrayBluePrintState() {}

        public ArrayBluePrintState(ArrayBluePrint entry) {
            this.name = entry.getName();
            this.method = entry.getMethod();
            this.size = entry.getSize();
            this.seed = entry.getSeed();
            this.createdAt = entry.getCreatedAt();
            this.sessionFilePath = entry.getSessionFilePath() != null ? entry.getSessionFilePath().toString() : null;
            this.lifecycle = entry.getLifecycle();
        }
    }
}