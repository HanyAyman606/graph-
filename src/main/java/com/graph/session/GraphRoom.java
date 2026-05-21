package com.graph.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graph.core.graphsekelton.GraphGenerator;
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

public class GraphRoom {

    private static final Logger       log        = LoggerFactory.getLogger(GraphRoom.class);
    private static final GraphRoom    INSTANCE   = new GraphRoom();
    private static final Path         INDEX_FILE = Paths.get("graph_pool_index.json");
    private static final ObjectMapper mapper     = new ObjectMapper();

    private final Map<String, GraphBluePrint> pool = new ConcurrentHashMap<>();

    int     total_adds_ever = 0;
    boolean index_dirty     = false;

    private GraphRoom() {
        loadIndex();
    }

    public static GraphRoom getInstance() {
        return INSTANCE;
    }

    // add a blueprint to the pool, throws if label is already taken
    public void add(GraphBluePrint bp) {
        if (pool.containsKey(bp.getLabel())) {
            throw new IllegalArgumentException(
                    "a graph with label '" + bp.getLabel() + "' already exists in the pool");
        }
        pool.put(bp.getLabel(), bp);
        total_adds_ever++;
        index_dirty = true;
        saveIndex();
        log.debug("GraphRoom: added '{}'", bp.getLabel());
    }

    // remove by label, silently does nothing if not found
    public void remove(String lbl) {
        GraphBluePrint removed = pool.remove(lbl);
        if (removed != null) {
            index_dirty = true;
            saveIndex();
            log.debug("GraphRoom: removed '{}'", lbl);
        }
    }

    public GraphBluePrint get(String lbl) {
        return pool.get(lbl);
    }

    public List<GraphBluePrint> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(pool.values()));
    }

    // write the pool to disk so it survives restarts
    private void saveIndex() {
        try {
            List<GraphBluePrintState> stash = new ArrayList<>();
            for (GraphBluePrint bp : pool.values()) {
                stash.add(new GraphBluePrintState(bp));
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(INDEX_FILE.toFile(), stash);
            index_dirty = false;
        } catch (IOException e) {
            log.error("GraphRoom: failed to save index to {}", INDEX_FILE, e);
        }
    }

    // rebuild pool from last session on startup
    private void loadIndex() {
        if (!Files.exists(INDEX_FILE)) {
            return;
        }
        try {
            List<GraphBluePrintState> stash = mapper.readValue(
                    INDEX_FILE.toFile(), new TypeReference<>() {});
            int how_many_loaded = 0;
            for (GraphBluePrintState state : stash) {
                GraphBluePrint bp = new GraphBluePrint(
                        state.label, state.topology, state.vertCount, state.fixedSeed);
                pool.put(bp.getLabel(), bp);
                how_many_loaded++;
            }
            log.debug("GraphRoom: loaded {} graphs from index", how_many_loaded);
        } catch (IOException e) {
            log.error("GraphRoom: failed to load index from {}", INDEX_FILE, e);
        }
    }

    @SuppressWarnings("unused")
    private String debugDumpPool() {
        StringBuilder sb = new StringBuilder();
        for (GraphBluePrint bp : pool.values()) {
            sb.append(bp.toString()).append("\n");
        }
        return sb.toString();
    }

    private static class GraphBluePrintState {
        public String                  label;
        public GraphGenerator.Topology topology;
        public int                     vertCount;
        public long                    fixedSeed;
        public long                    builtAt;
        public GraphBluePrint.Stage    currentStage;

        public GraphBluePrintState() {}

        public GraphBluePrintState(GraphBluePrint bp) {
            this.label        = bp.getLabel();
            this.topology     = bp.getTopology();
            this.vertCount    = bp.getVertCount();
            this.fixedSeed    = bp.getFixedSeed();
            this.builtAt      = bp.getBuiltAt();
            this.currentStage = bp.getCurrentStage();
        }
    }
}
