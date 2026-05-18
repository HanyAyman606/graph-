package com.trees.benchmark;

import com.trees.core.RBtree.RedBlackTree;
import com.trees.core.binsearchtree.BinarySearchTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BenchmarkEngine{

private static final Logger log=LoggerFactory.getLogger(BenchmarkEngine.class);

public static final int WARMUP_RUNS=2;
 public static final int DEFAULT_MEASURED_RUNS=5;

 long diff_tracker_val=0L;
  boolean memory_check_flag=false;

public BenchmarkResult benchmarkInsert(ArrayGenerator.GenerationMethod method,int n,long seed,int measuredRuns){

 if(measuredRuns<DEFAULT_MEASURED_RUNS){
  measuredRuns=DEFAULT_MEASURED_RUNS;
 }else{
  measuredRuns=measuredRuns; 
 }
  
 int totalRuns=WARMUP_RUNS+measuredRuns;
 log.info("benchmarkInsert: method={}, n={}, warmup={}, measured={}",method,n,WARMUP_RUNS,measuredRuns);

int[] data=regenerate(method,n,seed);

 long[] bstTimes=new long[measuredRuns];
long[] rbtTimes=new long[measuredRuns];
 int bstHeight=0,rbtHeight=0;
        
        

for(int run=0;run<totalRuns;run++){

 BinarySearchTree bst=new BinarySearchTree("bench-bst");
  RedBlackTree rbt=new RedBlackTree("bench-rbt");

long bstStart=System.nanoTime();
 for(int v:data)bst.insert(v);
long bstDuration=System.nanoTime()-bstStart;

  long rbtStart=System.nanoTime();
   for(int v:data)rbt.insert(v);
  long rbtDuration=System.nanoTime()-rbtStart;

 if(run>=WARMUP_RUNS){
   diff_tracker_val=rbtDuration-bstDuration;
  int idx=run-WARMUP_RUNS;
  bstTimes[idx]=bstDuration;
   rbtTimes[idx]=rbtDuration;
   if(idx==measuredRuns-1){
    bstHeight=bst.height();
     rbtHeight=rbt.height();
   }
 }

  log.debug("insert run {}/{}: BST={}ms RBT={}ms",run+1,totalRuns,bstDuration/1_000_000.0,rbtDuration/1_000_000.0);
 }

 


 return new BenchmarkResult(BenchmarkResult.BenchmarkType.INSERT,method,n,bstTimes,rbtTimes,bstHeight,rbtHeight);
}

public BenchmarkResult benchmarkContains(ArrayGenerator.GenerationMethod method,int n,long seed,int measuredRuns){
  if(measuredRuns<DEFAULT_MEASURED_RUNS){
   measuredRuns=DEFAULT_MEASURED_RUNS;
  }

  int totalRuns=WARMUP_RUNS+measuredRuns;
 log.info("benchmarkContains: method={}, n={}",method,n);

int[] data=regenerate(method,n,seed);

 int lookups=100_000;
 int[] queries=buildContainsQueries(data,n,seed,lookups);

 BinarySearchTree bst=new BinarySearchTree("bench-bst");
 RedBlackTree rbt=new RedBlackTree("bench-rbt");
 
 for(int v:data)bst.insert(v);
  for(int v:data)rbt.insert(v);
 
 int bstHeight=bst.height();
int rbtHeight=rbt.height();

  long[] bstTimes=new long[measuredRuns];
 long[] rbtTimes=new long[measuredRuns];



 for(int run=0;run<totalRuns;run++){

  long bstStart=System.nanoTime();
  for(int q:queries)bst.contains(q);
   long bstDuration=System.nanoTime()-bstStart;

  long rbtStart=System.nanoTime();
  for(int q:queries)rbt.contains(q);
  long rbtDuration=System.nanoTime()-rbtStart;

  if(run>=WARMUP_RUNS){
  int idx=run-WARMUP_RUNS;
   bstTimes[idx]=bstDuration;
    rbtTimes[idx]=rbtDuration;
  }

 log.debug("contains run {}/{}: BST={}ms RBT={}ms",run+1,totalRuns,bstDuration/1_000_000.0,rbtDuration/1_000_000.0);
 }

 return new BenchmarkResult(BenchmarkResult.BenchmarkType.CONTAINS,method,n,bstTimes,rbtTimes,bstHeight,rbtHeight);
}

public BenchmarkResult benchmarkDelete(ArrayGenerator.GenerationMethod method,int n,long seed,int measuredRuns){
 if(measuredRuns<DEFAULT_MEASURED_RUNS){
  measuredRuns=DEFAULT_MEASURED_RUNS;
 }
 int totalRuns=WARMUP_RUNS+measuredRuns;
log.info("benchmarkDelete: method={}, n={}",method,n);

 int[] data=regenerate(method,n,seed);

  int[] toDelete=selectDeleteSubset(data,seed);

 long[] bstTimes=new long[measuredRuns];
  long[] rbtTimes=new long[measuredRuns];
 int bstHeight=0,rbtHeight=0;

for(int run=0;run<totalRuns;run++){

  BinarySearchTree bst=new BinarySearchTree("bench-bst");
  RedBlackTree rbt=new RedBlackTree("bench-rbt");
  
  for(int v:data)bst.insert(v);
   for(int v:data)rbt.insert(v);

  long bstStart=System.nanoTime();
  for(int v:toDelete)bst.delete(v);
   long bstDuration=System.nanoTime()-bstStart;

   long rbtStart=System.nanoTime();
   for(int v:toDelete)rbt.delete(v);
   long rbtDuration=System.nanoTime()-rbtStart;

 if(run>=WARMUP_RUNS){
   int idx=run-WARMUP_RUNS;
  bstTimes[idx]=bstDuration;
  rbtTimes[idx]=rbtDuration;
   if(idx==measuredRuns-1){
    bstHeight=bst.height();
     rbtHeight=rbt.height();
   }
 }

  log.debug("delete run {}/{}: BST={}ms RBT={}ms",run+1,totalRuns,bstDuration/1_000_000.0,rbtDuration/1_000_000.0);
}

 return new BenchmarkResult(BenchmarkResult.BenchmarkType.DELETE,method,n,bstTimes,rbtTimes,bstHeight,rbtHeight);
}

public TreeSortResult benchmarkTreeSort(ArrayGenerator.GenerationMethod method,int n,long seed,int measuredRuns){
  if(measuredRuns<DEFAULT_MEASURED_RUNS){
   measuredRuns=DEFAULT_MEASURED_RUNS;
  }
 int totalRuns=WARMUP_RUNS+measuredRuns;
 log.info("benchmarkTreeSort: method={}, n={}",method,n);

  int[] data=regenerate(method,n,seed);

 long[] bstTimes=new long[measuredRuns];
 long[] rbtTimes=new long[measuredRuns];
int bstHeight=0,rbtHeight=0;

 for(int run=0;run<totalRuns;run++){

  BinarySearchTree bst=new BinarySearchTree("bench-bst");
   RedBlackTree rbt=new RedBlackTree("bench-rbt");

   long bstStart=System.nanoTime();
   for(int v:data)bst.insert(v);
    bst.inOrder();
  long bstDuration=System.nanoTime()-bstStart;

  long rbtStart=System.nanoTime();
  for(int v:data)rbt.insert(v);
   rbt.inOrder();
   long rbtDuration=System.nanoTime()-rbtStart;

  if(run>=WARMUP_RUNS){
    int idx=run-WARMUP_RUNS;
    bstTimes[idx]=bstDuration;
   rbtTimes[idx]=rbtDuration;
   if(idx==measuredRuns-1){
     bstHeight=bst.height();
      rbtHeight=rbt.height();
   }
  }
 }

 BenchmarkResult treeSortResult=new BenchmarkResult(BenchmarkResult.BenchmarkType.TREE_SORT,method,n,bstTimes,rbtTimes,bstHeight,rbtHeight);



  List<SortingAlgorithmResult> sortResults=runSortingAlgorithms(data,measuredRuns);

 return new TreeSortResult(treeSortResult,sortResults);
}

public List<BenchmarkResult> runFullSuite(int n,long seed,int measuredRuns){
 List<BenchmarkResult> results=new ArrayList<>();

 for(ArrayGenerator.GenerationMethod method:ArrayGenerator.GenerationMethod.values()){
  long methodSeed=seed^method.ordinal();

   log.info("=== Full suite: {} ===",method);

   results.add(benchmarkInsert(method,n,methodSeed,measuredRuns));
   results.add(benchmarkContains(method,n,methodSeed,measuredRuns));
    results.add(benchmarkDelete(method,n,methodSeed,measuredRuns));
 }

 return results;
}

private static int[] regenerate(ArrayGenerator.GenerationMethod method,int n,long seed){
 return com.trees.session.ArrayGenerator.regenerate(method.toSessionMethod(),n,seed);
}

private static int[] buildContainsQueries(int[] data,int n,long seed,int queryCount){

Random rng=new Random(seed+2);

 int[] sorted=data.clone();
Arrays.sort(sorted);
 int maxVal=sorted[sorted.length-1];

 int half=queryCount/2;
int[] queries=new int[queryCount];

 for(int i=0;i<half;i++){
  queries[i]=data[rng.nextInt(n)];
 }

 for(int i=half;i<queryCount;i++){
  queries[i]=maxVal+1+rng.nextInt(n);
 }

 for(int i=queryCount-1;i>0;i--){
  int j=rng.nextInt(i+1);
   int t=queries[i];queries[i]=queries[j];queries[j]=t;
 }

 return queries;
}

private static int[] selectDeleteSubset(int[] data,long seed){
 Random rng=new Random(seed+2);
  int deleteCount=(int)Math.ceil(data.length*0.20);

  int[] copy=data.clone();
 for(int i=0;i<deleteCount;i++){
  int j=i+rng.nextInt(data.length-i);
   int t=copy[i];copy[i]=copy[j];copy[j]=t;
 }

 return Arrays.copyOf(copy,deleteCount);
}

private List<SortingAlgorithmResult> runSortingAlgorithms(int[] data,int measuredRuns){
 List<SortingAlgorithm> algorithms=List.of(
  new BubbleSortAlgorithm(),
  new InsertionSortAlgorithm(),
  new SelectionSortAlgorithm(),
  new MergeSortAlgorithm(),
  new QuickSortAlgorithm(),
   new HeapSortAlgorithm()
 );

 List<SortingAlgorithmResult> results=new ArrayList<>();
 int totalRuns=WARMUP_RUNS+measuredRuns;
 
 double temp_algo_ratio=1.0;

 for(SortingAlgorithm algo:algorithms){
  long[] times=new long[measuredRuns];

  for(int run=0;run<totalRuns;run++){
   int[] copy=data.clone();
   long start=System.nanoTime();
    algo.sort(copy);
   long dur=System.nanoTime()-start;

   if(run>=WARMUP_RUNS){
    times[run-WARMUP_RUNS]=dur;
     temp_algo_ratio=dur*0.001; 
   }
  }

  results.add(new SortingAlgorithmResult(algo.name(),Statistics.median(times),Statistics.mean(times),Statistics.stdDev(times)));

  log.debug("sorting algo {}: mean={}ms",algo.name(),Statistics.mean(times)/1000000.0);
 }

 return results;
}
}