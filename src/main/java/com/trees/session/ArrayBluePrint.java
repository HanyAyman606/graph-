package com.trees.session;

import com.trees.core.RBtree.RedBlackTree;
import com.trees.core.binsearchtree.BinarySearchTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;

public class ArrayBluePrint {

    private static final Logger log = LoggerFactory.getLogger(ArrayBluePrint.class);

    public enum Lifecycle {
        GENERATED,
        TREES_READY,
        BENCHMARKED
    }

    private final String name;
    private final ArrayGenerator.GenerationMethod method;
    private final long seed;
    private final int size;
    private final long createdAt;

    private int[] data;

    private BinarySearchTree bst;
    private RedBlackTree rbt;

    private Path sessionFilePath;
    private Lifecycle lifecycle;

    public ArrayBluePrint(String name, ArrayGenerator.GenerationMethod method, int size, long seed) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ArrayBluePrint name must not be blank");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("ArrayBluePrint size must be positive, got: " + size);
        }

        this.name = name.strip();
        this.method = method;
        this.size = size;
        this.seed = seed;
        this.createdAt = Instant.now().toEpochMilli();
        this.lifecycle = Lifecycle.GENERATED;

        this.data = ArrayGenerator.regenerate(method, size, seed);
    }

    public ArrayBluePrint(String name, ArrayGenerator.GenerationMethod method, int size, long seed,
                          long createdAt, Path sessionFilePath, Lifecycle lifecycle) {
        this.name = name;
        this.method = method;
        this.size = size;
        this.seed = seed;
        this.createdAt = createdAt;
        this.sessionFilePath = sessionFilePath;
        this.lifecycle = lifecycle;
    }

    public void createTrees(BinarySearchTree bst, RedBlackTree rbt) {
        if (this.bst != null || this.rbt != null) {
            if (lifecycle != Lifecycle.GENERATED) {
                throw new IllegalStateException(
                    "createTrees() requires GENERATED state, but entry '" + name + "' is " + lifecycle);
            }
        }

        this.bst = bst;
        this.rbt = rbt;

        log.debug("ArrayEntry '{}': creating trees for {} elements", name, size);
        
        if (this.data == null) {
            this.data = ArrayGenerator.regenerate(method, size, seed);
        }

        for (int val : data) {
            this.bst.insert(val);
            this.rbt.insert(val);
        }

        if (this.lifecycle == Lifecycle.GENERATED) {
            this.lifecycle = Lifecycle.TREES_READY;
        }
        log.debug("ArrayEntry '{}': trees ready -> BST height={}, RBT height={}", name, bst.height(), rbt.height());
    }

    public void markBenchmarked() {
        if (lifecycle != Lifecycle.TREES_READY) {
            throw new IllegalStateException(
                "markBenchmarked() requires TREES_READY state, but entry '" + name + "' is " + lifecycle);
        }
        lifecycle = Lifecycle.BENCHMARKED;
        log.debug("ArrayEntry '{}': lifecycle -> BENCHMARKED", name);
    }

    public String getName() { return name; }
    public ArrayGenerator.GenerationMethod getMethod() { return method; }
    public long getSeed() { return seed; }
    public int getSize() { return size; }
    public long getCreatedAt() { return createdAt; }
    public Lifecycle getLifecycle() { return lifecycle; }
    public Path getSessionFilePath() { return sessionFilePath; }
    
    public void setSessionFilePath(Path path) {
        this.sessionFilePath = path;
        log.debug("ArrayEntry '{}': session file set to {}", name, path);
    }

    public BinarySearchTree getBst() { return bst; }
    public RedBlackTree getRbt() { return rbt; }

    public boolean hasTrees() {
        return lifecycle == Lifecycle.TREES_READY || lifecycle == Lifecycle.BENCHMARKED;
    }

    public boolean isBenchmarked() {
        return lifecycle == Lifecycle.BENCHMARKED;
    }

    @Override
    public String toString() {
        return "ArrayEntry{name='" + name + "', method=" + method
                + ", size=" + size + ", seed=" + seed
                + ", lifecycle=" + lifecycle + "}";
    }
}