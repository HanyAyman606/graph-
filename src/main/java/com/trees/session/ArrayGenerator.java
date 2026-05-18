package com.trees.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public final class ArrayGenerator {

    private static final Logger log = LoggerFactory.getLogger(ArrayGenerator.class);

    private static final double SWAP_RATIO_1  = 0.01;
    private static final double SWAP_RATIO_5  = 0.05;
    private static final double SWAP_RATIO_10 = 0.10;

    private ArrayGenerator() {}

    public static int[] fullyRandom(int n, long seed) {
        validateSize(n);
        log.debug("ArrayGenerator.fullyRandom(n={}, seed={})", n, seed);

        Random rng   = new Random(seed);
        int    bound = 10 * n;
        int[]  arr   = new int[n];

        for (int i = 0; i < n; i++) {
            arr[i] = rng.nextInt(bound + 1);
        }

        logFirst3(arr, "fullyRandom done");
        return arr;
    }

    public static int[] nearlySorted1(int n, long seed) {
        return nearlySorted(n, seed, SWAP_RATIO_1, "1%");
    }

    public static int[] nearlySorted5(int n, long seed) {
        return nearlySorted(n, seed, SWAP_RATIO_5, "5%");
    }

    public static int[] nearlySorted10(int n, long seed) {
        return nearlySorted(n, seed, SWAP_RATIO_10, "10%");
    }

    public enum GenerationMethod {
        FULLY_RANDOM,
        NEARLY_SORTED_1,
        NEARLY_SORTED_5,
        NEARLY_SORTED_10
    }

    public static int[] regenerate(GenerationMethod method, int n, long seed) {
        return switch (method) {
            case FULLY_RANDOM     -> fullyRandom(n, seed);
            case NEARLY_SORTED_1  -> nearlySorted1(n, seed);
            case NEARLY_SORTED_5  -> nearlySorted5(n, seed);
            case NEARLY_SORTED_10 -> nearlySorted10(n, seed);
        };
    }

    private static int[] nearlySorted(int n, long seed, double swapRatio, String label) {
        validateSize(n);
        int swaps = (int) Math.floor(swapRatio * n);
        log.debug("ArrayGenerator.nearlySorted{}(n={}, seed={}, swaps={})", label, n, seed, swaps);

        Random rng  = new Random(seed);
        int[]  arr  = new int[n];

        arr[0] = rng.nextInt(100);
        for (int i = 1; i < n; i++) {
            arr[i] = arr[i - 1] + rng.nextInt(5) + 1;
        }

        for (int s = 0; s < swaps; s++) {
            int i    = rng.nextInt(n);
            int j    = rng.nextInt(n);
            int temp = arr[i];
            arr[i]   = arr[j];
            arr[j]   = temp;
        }

        logFirst3(arr, "nearlySorted" + label + " done");
        return arr;
    }

    private static void validateSize(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Array size must be positive, got: " + n);
        }
    }

    private static void logFirst3(int[] arr, String prefix) {
        if (arr.length >= 3) {
            log.debug("{}: first=[{}, {}, {}]", prefix, arr[0], arr[1], arr[2]);
        } else if (arr.length == 2) {
            log.debug("{}: first=[{}, {}]", prefix, arr[0], arr[1]);
        } else {
            log.debug("{}: first=[{}]", prefix, arr[0]);
        }
    }
}