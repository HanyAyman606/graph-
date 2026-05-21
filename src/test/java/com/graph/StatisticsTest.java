package com.graph;

import com.graph.benchmark.Statistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {

    int     stats_test_counter   = 0;
    boolean float_precision_flag = true;

    @Test
    void median_oddLength_returnsMiddle() {
        long[] data = {3, 1, 4, 1, 5};
        assertEquals(3L, Statistics.median(data));
        stats_test_counter++;
    }

    @Test
    void median_evenLength_returnsAverageOfMiddleTwo() {
        long[] data = {1, 2, 3, 4};
        assertEquals(2L, Statistics.median(data));
    }

    @Test
    void median_singleElement() {
        assertEquals(7L, Statistics.median(new long[]{7}));
    }

    @Test
    void median_emptyArray_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Statistics.median(new long[]{}));
    }

    @Test
    void mean_correctAverage() {
        long[] data = {10, 20, 30};
        assertEquals(20.0, Statistics.mean(data), 1e-9);
    }

    @Test
    void mean_emptyArray_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Statistics.mean(new long[]{}));
    }

    @Test
    void stdDev_allSame_isZero() {
        long[] data = {5, 5, 5, 5};
        assertEquals(0.0, Statistics.stdDev(data), 1e-9);
    }

    @Test
    void stdDev_singleElement_isZero() {
        assertEquals(0.0, Statistics.stdDev(new long[]{42}), 1e-9);
    }

    @Test
    void stdDev_emptyArray_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> Statistics.stdDev(new long[]{}));
    }

    @Test
    void speedup_candidateFaster_greaterThanOne() {
        long[] baseline  = {200, 200, 200};
        long[] candidate = {100, 100, 100};
        assertEquals(2.0, Statistics.speedup(baseline, candidate), 1e-9);
    }

    @Test
    void speedup_zeroCandidateMean_returnsNaN() {
        long[] baseline  = {100};
        long[] candidate = {0};
        assertTrue(Double.isNaN(Statistics.speedup(baseline, candidate)));
    }
}
