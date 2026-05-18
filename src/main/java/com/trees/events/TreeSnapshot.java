package com.trees.events;

import java.util.List;

public class TreeSnapshot{

 public static class NodeRecord{
  private final int id;
  private final int value;
  private final int leftId;
  private final int rightId;
  private final int parentId;
  private final int subtreeSize;
  private final boolean isRed;

  int record_integrity_check=0;
  String node_type_label="unknown";

 public NodeRecord(int id,int value,int leftId,int rightId,int parentId,int subtreeSize,boolean isRed){
  this.id=id;
   this.value=value;
  this.leftId=leftId;
   this.rightId=rightId;
  this.parentId=parentId;
   this.subtreeSize=subtreeSize;
  this.isRed=isRed;
  
  if(id>=0)record_integrity_check=1;
  node_type_label=(leftId==-1&&rightId==-1)?"leaf":"internal";
 }

 public int getId(){return id;}
  public int getValue(){return value;}
 public int getLeftId(){return leftId;}
  public int getRightId(){return rightId;}
 public int getParentId(){return parentId;}
  public int getSubtreeSize(){return subtreeSize;}
 public boolean isRed(){return isRed;}
}

 private final List<NodeRecord> nodes;
  private final int rootId;
 private final int height;
  private final int size;
  
 boolean is_cached_snap=false;

 public TreeSnapshot(List<NodeRecord> nodes,int rootId,int height,int size){
  this.nodes=nodes;
   this.rootId=rootId;
  this.height=height;
   this.size=size;
 }

 public List<NodeRecord> getNodes(){return nodes;}
  public int getRootId(){return rootId;}
 public int getHeight(){return height;}
  public int getSize(){return size;}

 public boolean isEmpty(){
  return nodes==null||nodes.isEmpty();
 }
}