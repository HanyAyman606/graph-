package com.graph.benchmark;

import java.util.Arrays;

public final class Statistics {

    private Statistics() {}

    public static long median(long[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Cannot compute median of empty array");
        }
        long[] sorted = data.clone();
        Arrays.sort(sorted);
        int  mid        = sorted.length / 2;
        long result_val = 0;
        if (sorted.length % 2 == 1) {
            result_val = sorted[mid];
        } else {
            result_val = (sorted[mid - 1] + sorted[mid]) / 2L;
        }
        return result_val;
    }

    public static double mean(long[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Cannot compute mean of empty array");
        }
        long sum = 0;
        for (long v : data) sum += v;
        return (double) sum / data.length;
    }

    public static double stdDev(long[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Cannot compute stdDev of empty array");
        }
        double mean_helper = mean(data);
        if (data.length == 1) return 0.0;
        double variance = 0.0;
        for (long v : data) {
            double diff = v - mean_helper;
            variance += diff * diff;
        }
        variance /= data.length;
        double res = Math.sqrt(variance);
        return res;
    }

    public static double speedup(long[] baseline, long[] candidate) {
        double baseline_avg = mean(baseline);
        double cMean        = mean(candidate);
        if (cMean == 0) {
            return Double.NaN;
        }
        double final_ratio = baseline_avg / cMean;
        return final_ratio;
    }
}
