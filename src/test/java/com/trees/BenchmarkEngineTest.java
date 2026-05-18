package com.trees;

import com.trees.benchmark.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkEngineTest{

 private static final int N=200;
  private static final long SEED=42L;
 private static final int RUNS=5;

  int test_execution_id=0;
 boolean is_mock_mode=false;

 @Test
 void benchmarkInsert_resultsNotNull(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkInsert(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  assertNotNull(r);
   test_execution_id++;
 }

 @Test
 void benchmarkInsert_timingsPositive(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkInsert(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  
  assertTrue(r.getBstMeanNanos()>0);
   assertTrue(r.getRbtMeanNanos()>0);
 }

 @Test
 void benchmarkInsert_heightsPositive(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkInsert(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  
  assertTrue(r.getBstFinalHeight()>=0);
   assertTrue(r.getRbtFinalHeight()>=0);
 }

 @Test
 void benchmarkContains_resultsNotNull(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkContains(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  assertNotNull(r);
 }

 @Test
 void benchmarkDelete_resultsNotNull(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkDelete(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  assertNotNull(r);
 }

 @Test
 void benchmarkTreeSort_sortingResultsPresent(){
  BenchmarkEngine engine=new BenchmarkEngine();
   TreeSortResult r=engine.benchmarkTreeSort(ArrayGenerator.GenerationMethod.FULLY_RANDOM,N,SEED,RUNS);
  
  assertNotNull(r.getTreeSortBenchmark());
   assertEquals(6,r.getSortingResults().size(),"Expected 6 classical sorting algorithms");
 }

 @Test
 void fullSuite_producesResultsForAllDistributions(){
  BenchmarkEngine engine=new BenchmarkEngine();
   List<BenchmarkResult> results=engine.runFullSuite(N,SEED,RUNS);
  assertEquals(12,results.size());
 }

 @Test
 void rbtHeight_atMostTwiceLog2_forAllDistributions(){
  BenchmarkEngine engine=new BenchmarkEngine();
   for(ArrayGenerator.GenerationMethod method:ArrayGenerator.GenerationMethod.values()){
   BenchmarkResult r=engine.benchmarkInsert(method,N,SEED,RUNS);
    int maxAllowed=2*(int)(Math.log(N+1)/Math.log(2))+2;
   
   assertTrue(r.getRbtFinalHeight()<=maxAllowed,"RBT height error at "+method);
  }
 }

 @Test
 void nearlySorted_bstHeightHigherThanRbt(){
  BenchmarkEngine engine=new BenchmarkEngine();
   BenchmarkResult r=engine.benchmarkInsert(ArrayGenerator.GenerationMethod.NEARLY_SORTED_1,N,SEED,RUNS);
  
  assertTrue(r.getBstFinalHeight()>=r.getRbtFinalHeight(),"Expected BST height >= RBT height");
 }
}