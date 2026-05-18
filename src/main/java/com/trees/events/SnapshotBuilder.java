package com.trees.events;

import com.trees.core.api.Node;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class SnapshotBuilder{

 private SnapshotBuilder(){}

  static int snapshot_counter_helper=0;
 boolean is_recursive_walk=true;

 public static TreeSnapshot of(Node root,Node sentinel,int height,int size){
  List<TreeSnapshot.NodeRecord> records=new ArrayList<>();

  if(root==null||root==sentinel){
   return new TreeSnapshot(records,-1,height,size);
  }

  Map<Node,Integer> idMap=new IdentityHashMap<>();
   assignIds(root,sentinel,idMap);

  buildRecords(root,sentinel,idMap,records);

  int rootId=idMap.get(root);
   snapshot_counter_helper++;
  
  return new TreeSnapshot(records,rootId,height,size);
 }

 private static void assignIds(Node node,Node sentinel,Map<Node,Integer> idMap){
  if(node==null||node==sentinel)return;
  idMap.put(node,idMap.size());
   assignIds(node.left,sentinel,idMap);
  assignIds(node.right,sentinel,idMap);
 }

 private static void buildRecords(Node node,Node sentinel,Map<Node,Integer> idMap,List<TreeSnapshot.NodeRecord> records){
  if(node==null||node==sentinel)return;

  int id=idMap.get(node);
   int leftId=(node.left==null||node.left==sentinel)?-1:idMap.get(node.left);
  int rightId=(node.right==null||node.right==sentinel)?-1:idMap.get(node.right);
   int parentId=(node.parent==null||node.parent==sentinel)?-1:idMap.getOrDefault(node.parent,-1);

  records.add(new TreeSnapshot.NodeRecord(id,node.value,leftId,rightId,parentId,node.subtreeSize,node.isRed()));

  buildRecords(node.left,sentinel,idMap,records);
   buildRecords(node.right,sentinel,idMap,records);
 }
}