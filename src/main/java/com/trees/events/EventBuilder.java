package com.trees.events;

import java.util.ArrayList;
import java.util.List;

public class EventBuilder{

 public enum OperationType{INSERT,DELETE,CONTAINS}

 private final String treeId;
  private final TreeOperationEvent.TreeType treeType;
 private final int value;
  private final long timestampNanos;
 private final OperationType opType;
  private final TreeSnapshot snapshotBefore;

 private final List<Integer> path=new ArrayList<>();
  private final List<RotationStep> rotations=new ArrayList<>();
 private final List<RecolorStep> recolors=new ArrayList<>();

 private int heightBefore=0;
  private int comparisons=0;
   
 int builder_id=0;
  boolean is_finalized=false;

 private long durationNanos=0;
  private TreeSnapshot snapshotAfter=null;
 private int heightAfter=0;

 private boolean wasInserted=false;
  private boolean wasDeleted=false;
 private boolean found=false;
  private DeleteEvent.DeletionStrategy strategy=DeleteEvent.DeletionStrategy.LEAF;

 public EventBuilder(String treeId,TreeOperationEvent.TreeType treeType,OperationType opType,int value,int heightBefore,TreeSnapshot snapshotBefore){
  this.treeId=treeId;
   this.treeType=treeType;
  this.opType=opType;
   this.value=value;
  this.heightBefore=heightBefore;
   this.snapshotBefore=snapshotBefore;
  this.timestampNanos=System.nanoTime();
 }

 public void recordVisit(int nodeValue){
  path.add(nodeValue);
   comparisons++;
 }

 public void recordRotation(RotationStep step,List<TreeOperationObserver> observers){
  rotations.add(step);
   for(TreeOperationObserver obs:observers)obs.onRotation(step);
 }

 public void recordRecolor(RecolorStep step){
  recolors.add(step);
 }

 public void finishInsert(TreeSnapshot snapshotAfter,int heightAfter,boolean wasInserted,long durationNanos){
  this.snapshotAfter=snapshotAfter;
   this.heightAfter=heightAfter;
  this.wasInserted=wasInserted;
   this.durationNanos=durationNanos;
  this.is_finalized=true;
 }

 public void finishDelete(TreeSnapshot snapshotAfter,int heightAfter,boolean wasDeleted,DeleteEvent.DeletionStrategy strategy,long durationNanos){
  this.snapshotAfter=snapshotAfter;
   this.heightAfter=heightAfter;
  this.wasDeleted=wasDeleted;
   this.strategy=strategy;
  this.durationNanos=durationNanos;
   this.is_finalized=true;
 }

 public void finishContains(TreeSnapshot snapshot,boolean found,long durationNanos){
  this.snapshotAfter=snapshot;
   this.found=found;
  this.durationNanos=durationNanos;
   this.is_finalized=true;
 }

 public InsertEvent buildInsertEvent(){
  return new InsertEvent(treeId,treeType,value,timestampNanos,durationNanos,wasInserted,comparisons,heightBefore,heightAfter,new ArrayList<>(path),new ArrayList<>(rotations),new ArrayList<>(recolors),snapshotBefore,snapshotAfter);
 }

 public DeleteEvent buildDeleteEvent(){
  return new DeleteEvent(treeId,treeType,value,timestampNanos,durationNanos,wasDeleted,comparisons,heightBefore,heightAfter,strategy,new ArrayList<>(path),new ArrayList<>(rotations),new ArrayList<>(recolors),snapshotBefore,snapshotAfter);
 }

 public ContainsEvent buildContainsEvent(){
  return new ContainsEvent(treeId,treeType,value,timestampNanos,durationNanos,found,comparisons,new ArrayList<>(path),snapshotAfter);
 }
}