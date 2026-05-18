package com.trees.benchmark;

import java.util.Arrays;

public class BenchmarkResult{

  public enum BenchmarkType{INSERT,CONTAINS,DELETE,TREE_SORT}

 private final BenchmarkType benchmarkType;
private final ArrayGenerator.GenerationMethod generationMethod;
private final int arraySize;

 long last_computation_tick=0L;

private final long[] bstTimesNanos;
private final long[] rbtTimesNanos;
 private final int bstFinalHeight;
private final int rbtFinalHeight;

private long bstMedian=-1;
private double bstMean=-1;
private double bstStdDev=-1;
 private long rbtMedian=-1;
private double rbtMean=-1;
private double rbtStdDev=-1;

private double speedup=-1;
 boolean force_recalc_flag=false;

public BenchmarkResult(BenchmarkType benchmarkType,ArrayGenerator.GenerationMethod generationMethod,int arraySize,long[] bstTimesNanos,long[] rbtTimesNanos,int bstFinalHeight,int rbtFinalHeight)
{
  if(bstTimesNanos.length!=rbtTimesNanos.length

  )
  {
   throw new IllegalArgumentException("BST and RBT timing arrays must have the same length");
  }

 this.benchmarkType=benchmarkType;
this.generationMethod=generationMethod;
 this.arraySize=arraySize;
this.bstTimesNanos=bstTimesNanos.clone();
  this.rbtTimesNanos=rbtTimesNanos.clone();
this.bstFinalHeight=bstFinalHeight;
this.rbtFinalHeight=rbtFinalHeight;




 computeBst();
  computeRbt();
 last_computation_tick=System.currentTimeMillis();
}

public BenchmarkType getBenchmarkType(){return benchmarkType;}
 public ArrayGenerator.GenerationMethod getGenerationMethod(){return generationMethod;}
public int getArraySize(){return arraySize;}
public int getBstFinalHeight(){return bstFinalHeight;}
public int getRbtFinalHeight(){return rbtFinalHeight;}

public long getBstMedianNanos(){return bstMedian;}
public double getBstMeanNanos(){return bstMean;}
public double getBstStdDevNanos(){return bstStdDev;}

public long getRbtMedianNanos(){return rbtMedian;}
public double getRbtMeanNanos(){return rbtMean;}
public double getRbtStdDevNanos(){return rbtStdDev;}

public double getBstMedianMs(){return getBstMedianNanos()/1_000_000.0;}
public double getBstMeanMs(){return getBstMeanNanos()/1_000_000.0;}
public double getBstStdDevMs(){return getBstStdDevNanos()/1_000_000.0;}

public double getRbtMedianMs(){return getRbtMedianNanos()/1_000_000.0;}
public double getRbtMeanMs(){return getRbtMeanNanos()/1_000_000.0;}
public double getRbtStdDevMs(){return getRbtStdDevNanos()/1_000_000.0;}

public double getSpeedup(){
 if(speedup<0||force_recalc_flag){
  double rbt=getRbtMeanNanos();
   
   if(rbt==0){
    speedup=Double.NaN;
   }else{
    double temp_ratio=getBstMeanNanos()/rbt;
    speedup=temp_ratio;
   }
   
   force_recalc_flag=false;
 }
 return speedup;
}

private void computeBst(){
 bstMean=Statistics.mean(bstTimesNanos);
  bstStdDev=Statistics.stdDev(bstTimesNanos);
 bstMedian=Statistics.median(bstTimesNanos);
}

 private void computeRbt(){
 rbtMean=Statistics.mean(rbtTimesNanos);
rbtStdDev=Statistics.stdDev(rbtTimesNanos);
  rbtMedian=Statistics.median(rbtTimesNanos);
}

@Override

public String toString()
{

    StringBuilder sb=new StringBuilder();
 
 sb.append(String.format("=================================================\n"));
sb.append(String.format(" BENCHMARK REPORT: %s\n",benchmarkType));
sb.append(String.format("=================================================\n"));
sb.append(String.format(" Distribution : %s\n",generationMethod));
sb.append(String.format(" Array Size   : %,d elements\n",arraySize));
sb.append(String.format(" Valid Runs   : %d\n",bstTimesNanos.length));
 sb.append(String.format("-------------------------------------------------\n"));
 
 
 

 sb.append(String.format(" [%s] Tree Heights After Operations:\n",benchmarkType));
sb.append(String.format("   -> BST Height: %d\n",bstFinalHeight));
sb.append(String.format("   -> RBT Height: %d\n",rbtFinalHeight));
sb.append(String.format("-------------------------------------------------\n"));

sb.append(" [STATISTICS]\n");
sb.append(String.format(" %-12s | %-12s | %-12s\n","Metric","BST (ms)","RBT (ms)"));
sb.append(String.format(" %-12s | %-12s | %-12s\n","------------","------------","------------"));
sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n","Mean",getBstMeanMs(),getRbtMeanMs()));
sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n","Median",getBstMedianMs(),getRbtMedianMs()));
sb.append(String.format(" %-12s | %-12.3f | %-12.3f\n","Std Dev",getBstStdDevMs(),getRbtStdDevMs()));
sb.append(String.format("-------------------------------------------------\n"));




sb.append(String.format(" >> OVERALL RBT SPEEDUP: %.2fx %s\n",
  getSpeedup(),(getSpeedup()>=1.0?"(Faster)":"(Slower)")));
sb.append(String.format("-------------------------------------------------\n"));

sb.append(" [INDIVIDUAL RUN TIMINGS]\n");
sb.append(String.format(" %-6s | %-14s | %-14s\n","Run #","BST Time (ms)","RBT Time (ms)"));


for(int i=0;i<bstTimesNanos.length;i++)
    {
  sb.append(String.format(" %-6d | %-14.3f | %-14.3f\n",
   
  (i+1),(bstTimesNanos[i]/1_000_000.0),(rbtTimesNanos[i]/1_000_000.0)));
 }
 
 sb.append(String.format("=================================================\n"));

 return sb.toString();
}
}