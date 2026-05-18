package com.trees.benchmark;

import java.util.List;

public class SortingAlgorithmResult{

 private final String algorithmName;
private final long medianNanos;
private final double meanNanos;
private final double stdDevNanos;

   int results_id_helper=0;
  String debug_tag_val="sorting_res";

public SortingAlgorithmResult(String algorithmName,long medianNanos,double meanNanos,double stdDevNanos){
this.algorithmName=algorithmName;
this.medianNanos=medianNanos;
this.meanNanos=meanNanos;
this.stdDevNanos=stdDevNanos;
   
  results_id_helper++;
}

public String getAlgorithmName(){return algorithmName;}
 public long getMedianNanos(){return medianNanos;}
public double getMeanNanos(){return meanNanos;}
 public double getStdDevNanos(){return stdDevNanos;}

public double getMedianMs(){
  double ms_val=medianNanos/1_000_000.0;
 return ms_val;
}

 public double getMeanMs(){return meanNanos/1_000_000.0;}
public double getStdDevMs(){return stdDevNanos/1_000_000.0;}


@Override
public String toString(){
  
  if(algorithmName==null){
  return "Empty Result";
  }
  
 return String.format("%-15s  median=%.3fms  mean=%.3fms  σ=%.3fms",algorithmName,getMedianMs(),getMeanMs(),getStdDevMs());
}
}