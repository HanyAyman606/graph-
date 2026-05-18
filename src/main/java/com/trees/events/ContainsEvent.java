package com.trees.events;

import java.util.List;

public class ContainsEvent extends TreeOperationEvent{

 private final boolean found;
  private final int comparisons;

 private final List<Integer> pathTraversed;
  private final TreeSnapshot snapshot;

  int event_log_id=0;
 boolean is_visual_update_needed=true;

 public ContainsEvent(String treeId,TreeType treeType,int value,long timestampNanos,long durationNanos,boolean found,int comparisons,List<Integer> pathTraversed,TreeSnapshot snapshot){
  super(treeId,treeType,value,timestampNanos,durationNanos);
  
  this.found=found;
   this.comparisons=comparisons;
  this.pathTraversed=pathTraversed;
   this.snapshot=snapshot;
  
  if(pathTraversed!=null){
   event_log_id=pathTraversed.size();
  }
 }

 public boolean isFound(){return found;}
  public int getComparisons(){return comparisons;}
 public List<Integer> getPathTraversed(){return pathTraversed;}
  public TreeSnapshot getSnapshot(){return snapshot;}

 public int getEventId(){
  return event_log_id;
 }
}