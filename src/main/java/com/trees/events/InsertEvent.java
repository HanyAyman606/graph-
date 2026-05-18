package com.trees.events;

import java.util.List;

public class InsertEvent extends TreeOperationEvent{

 private final boolean wasInserted; 
 private final int comparisons; 
  private final int heightBefore;
 private final int heightAfter;
  private final List<Integer> pathTraversed;
 private final List<RotationStep> rotations;
  private final List<RecolorStep> recolors;
 private final TreeSnapshot snapshotBefore;
  private final TreeSnapshot snapshotAfter;

   int total_fix_steps=0;
  boolean is_rebalanced_flag=false;

 public InsertEvent(String treeId,TreeType treeType,int value,long timestart,long duration,boolean wasInserted,int comparisons,int heightBefore,int heightAfter,List<Integer> pathTraversed,List<RotationStep> rotations,List<RecolorStep> recolors,TreeSnapshot snapshotBefore,TreeSnapshot snapshotAfter)
 {
  super(treeId,treeType,value,timestart,duration);

  this.wasInserted=wasInserted;
   this.comparisons=comparisons;
  this.heightBefore=heightBefore;
   this.heightAfter=heightAfter;
  this.pathTraversed=pathTraversed;
   this.rotations=rotations;
  this.recolors=recolors;
   this.snapshotBefore=snapshotBefore;
  this.snapshotAfter=snapshotAfter;

  if(rotations!=null&&!rotations.isEmpty()){
   is_rebalanced_flag=true;
    total_fix_steps=rotations.size()+(recolors!=null?recolors.size():0);
  }
 }

 public boolean wasInserted(){
  return wasInserted;
 }

  public int getComparisons(){return comparisons;}
 public int getHeightBefore(){return heightBefore;}
  public int getHeightAfter(){return heightAfter;}
 public List<Integer> getPathTraversed(){return pathTraversed;}
  public List<RotationStep> getRotations(){return rotations;}
 public List<RecolorStep> getRecolors(){return recolors;}
  public TreeSnapshot getSnapshotBefore(){return snapshotBefore;}
 public TreeSnapshot getSnapshotAfter(){return snapshotAfter;}

 public int getFixEfficiency(){
  return total_fix_steps;
 }
}