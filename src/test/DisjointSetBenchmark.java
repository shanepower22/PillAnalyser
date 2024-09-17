package com.pillanalyser.benchmarks;


import com.pillanalyser.pillanalyser.DisjointSet;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Measurement(iterations=10)
@Warmup(iterations=5)
@Fork(value=1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class DisjointSetBenchmark {

    private int[] arrayForPixels;



    @Setup
    public void prepare() {
        arrayForPixels = new int[1000];
        for (int i = 0; i < arrayForPixels.length; i++) {
            arrayForPixels[i] = i;
        }
    }

    @Benchmark
    public void testUnionFind() {
        DisjointSet.union(arrayForPixels, 10, 20);
        DisjointSet.find(arrayForPixels, 20);
    }
}