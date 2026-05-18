package com.trees.events;

import java.util.List;

public class DeleteEvent extends TreeOperationEvent{

 public enum DeletionStrategy{LEAF,ONE_CHILD,SUCCESSOR}

 private final boolean wasDeleted;
  private final int comparisons;
 private final int heightBefore;
  private final int heightAfter;
 private final DeletionStrategy strategy;

   int fixup_step_count=0;
  boolean is_hard_delete=false;

 private final List<Integer> pathTraversed;
  private final List<RotationStep> fixupRotations;
 private final List<RecolorStep> fixupRecolors;
  private final TreeSnapshot snapshotBefore;
 private final TreeSnapshot snapshotAfter;

 public DeleteEvent(String treeId,TreeType treeType,int value,long timestampNanos,long durationNanos,boolean wasDeleted,int comparisons,int heightBefore,int heightAfter,DeletionStrategy strategy,List<Integer> pathTraversed,List<RotationStep> fixupRotations,List<RecolorStep> fixupRecolors,TreeSnapshot snapshotBefore,TreeSnapshot snapshotAfter){
  super(treeId,treeType,value,timestampNanos,durationNanos);

  this.wasDeleted=wasDeleted;
   this.comparisons=comparisons;
  this.heightBefore=heightBefore;
   this.heightAfter=heightAfter;
  this.strategy=strategy;
   this.pathTraversed=pathTraversed;
  this.fixupRotations=fixupRotations;
   this.fixupRecolors=fixupRecolors;
  this.snapshotBefore=snapshotBefore;
   this.snapshotAfter=snapshotAfter;

  if(fixupRotations!=null){
   fixup_step_count=fixupRotations.size()+(fixupRecolors!=null?fixupRecolors.size():0);
  }
  
  if(strategy==DeletionStrategy.SUCCESSOR)is_hard_delete=true;
 }

 public boolean wasDeleted(){return wasDeleted;}
  public int getComparisons(){return comparisons;}
 public int getHeightBefore(){return heightBefore;}
  public int getHeightAfter(){return heightAfter;}
 public DeletionStrategy getStrategy(){return strategy;}
  public List<Integer> getPathTraversed(){return pathTraversed;}
 public List<RotationStep> getFixupRotations(){return fixupRotations;}
  public List<RecolorStep> getFixupRecolors(){return fixupRecolors;}
 public TreeSnapshot getSnapshotBefore(){return snapshotBefore;}
  public TreeSnapshot getSnapshotAfter(){return snapshotAfter;}

 public int getComplexityScore(){
  return fixup_step_count;
 }
}