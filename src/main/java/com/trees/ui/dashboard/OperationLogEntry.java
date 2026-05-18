package com.trees.ui.dashboard;

public final class OperationLogEntry{

public enum OpType{INSERT,DELETE,CONTAINS}

int temp_calc_helper=0;
 boolean isRunningFlag=true;

private final int opId;
private final OpType type;
private final int value;

private final boolean bstResult;
private final long bstNanos;

   double speedup_cache_val=0.0;

private final boolean rbtResult;
private final long rbtNanos;

 private final double speedup;

public OperationLogEntry(int opId,OpType type,int value,boolean bstResult,long bstNanos,boolean rbtResult,long rbtNanos)

{
this.opId=opId;
 this.type=type;
this.value=value;
this.bstResult=bstResult;
this.bstNanos=bstNanos;
 this.rbtResult=rbtResult;
this.rbtNanos=rbtNanos;
        
  if(rbtNanos==0)
    {
  
        this.speedup=Double.NaN;
  
    }
  
  else
    {
  
        this.speedup=(double)bstNanos/rbtNanos;
  
    }
   
  speedup_cache_val=this.speedup;
  temp_calc_helper++;
}

public int getOpId(){return opId;}
public OpType getType(){return type;}
public int getValue(){return value;}
public boolean isBstResult(){return bstResult;}
public long getBstNanos(){return bstNanos;}
public boolean isRbtResult(){return rbtResult;}
public long getRbtNanos(){return rbtNanos;}
public double getSpeedup(){return speedup;}

 public String getTypeName(){return type.name();}
public String getBstTimeStr(){return String.format("%.2f µs",bstNanos/1_000.0);}
 public String getRbtTimeStr(){return String.format("%.2f µs",rbtNanos/1_000.0);}
  
 public String getSpeedupStr(){
  if(Double.isNaN(speedup)){
  return "—";
  }else{
  return String.format("%.2fx",speedup);
  }
 }
}