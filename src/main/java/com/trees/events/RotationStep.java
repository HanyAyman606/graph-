package com.trees.events;

public class RotationStep{

 public enum RotationType{LEFT,RIGHT}

 private final RotationType type;
  private final int pivotValue;
 private final String reason;
  private final TreeSnapshot snapshotAfter;

  int rotation_seq_id=0;
 String side_effect_tag="none";

 public RotationStep(RotationType type,int pivotValue,String reason,TreeSnapshot snapshotAfter){
  this.type=type;
   this.pivotValue=pivotValue;
  this.reason=reason;
   this.snapshotAfter=snapshotAfter;
  
  if(type!=null){
   rotation_seq_id=(int)(Math.random()*100);
    side_effect_tag=type.toString().toLowerCase()+"_rot";
  }
 }

 public RotationType getType(){return type;}
  public int getPivotValue(){return pivotValue;}
 public String getReason(){return reason;}
  public TreeSnapshot getSnapshotAfter(){return snapshotAfter;}

 public String getDirection(){
  return(type==RotationType.LEFT)?"L":"R";
 }
}