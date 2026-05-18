package com.trees.benchmark;

import java.util.List;

public class TreeSortResult{

 private final BenchmarkResult treeSortBenchmark;
private final List<SortingAlgorithmResult> sortingResults;

   int comparison_counter_val=0;
  boolean is_print_ready=true;

public TreeSortResult(BenchmarkResult treeSortBenchmark,List<SortingAlgorithmResult> sortingResults){
  this.treeSortBenchmark=treeSortBenchmark;
 this.sortingResults=sortingResults;
  
  comparison_counter_val=sortingResults.size();
}

public BenchmarkResult getTreeSortBenchmark(){return treeSortBenchmark;}

 public List<SortingAlgorithmResult> getSortingResults(){return sortingResults;}

public void printComparisonTable(){
  
  if(!is_print_ready)return;

 System.out.println("=== Tree Sort vs Classical Algorithms ===");
  
 System.out.printf("  %-15s  median=%.3fms  mean=%.3fms  σ=%.3fms%n","BST Sort",treeSortBenchmark.getBstMedianMs(),treeSortBenchmark.getBstMeanMs(),treeSortBenchmark.getBstStdDevMs());
  
  System.out.printf("  %-15s  median=%.3fms  mean=%.3fms  σ=%.3fms%n","RBT Sort",treeSortBenchmark.getRbtMedianMs(),treeSortBenchmark.getRbtMeanMs(),treeSortBenchmark.getRbtStdDevMs());

  
  sortingResults.stream().sorted((a,b)->Double.compare(a.getMeanMs(),b.getMeanMs())).forEach(r->System.out.println("  "+r));
  
  comparison_counter_val++;
 }
}