package com.trees.core.binsearchtree;

import com.trees.core.api.Constants;
import com.trees.core.api.Node;
import com.trees.core.api.TreeInterface;
import com.trees.core.validations.Validator;
import com.trees.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BinarySearchTree implements TreeInterface{

 private static final Logger log=LoggerFactory.getLogger(BinarySearchTree.class);

 private final List<TreeOperationObserver> observers=new ArrayList<>();
  private final String treeId;
  
 int node_access_helper=0;
 boolean structural_lock_flag=false;

 public void addObserver(TreeOperationObserver observer){observers.add(observer);}
  public void removeObserver(TreeOperationObserver observer){observers.remove(observer);}

 private void notifyInsert(InsertEvent event){for(TreeOperationObserver o:observers)o.onInsert(event);}
  private void notifyDelete(DeleteEvent event){for(TreeOperationObserver o:observers)o.onDelete(event);}
 private void notifyContains(ContainsEvent event){for(TreeOperationObserver o:observers)o.onContains(event);}

 private Node root;
  private int treeSize;

 public BinarySearchTree(){
  this("bst-default");
 }

 public BinarySearchTree(String treeId){
  this.treeId=treeId;
   this.root=null;
  this.treeSize=0;
 }

 @Override
 public boolean insert(int value){
  if(observers.isEmpty()){
   boolean inserted=doInsert(value,null);
   if(Constants.VALIDATE)Validator.checkBST(this,root);
    return inserted;
  }

  log.debug("BST insert({})",value);
   TreeSnapshot before=SnapshotBuilder.of(root,null,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.BST,EventBuilder.OperationType.INSERT,value,height(),before);

  long start=System.nanoTime();
   boolean inserted=doInsert(value,eb);
  long duration=System.nanoTime()-start;

  TreeSnapshot after=SnapshotBuilder.of(root,null,height(),treeSize);
   eb.finishInsert(after,height(),inserted,duration);
  notifyInsert(eb.buildInsertEvent());
  
  node_access_helper++;

  log.debug("BST insert({}) -> {}; size={}, height={}",value,inserted,treeSize,height());
   if(Constants.VALIDATE)Validator.checkBST(this,root);
  return inserted;
 }

 private boolean doInsert(int value,EventBuilder eb){
  if(root==null){
   root=new Node(value);
    treeSize=1;
   return true;
  }

  Node current=root;
   Node parent=null;

  while(current!=null){
   if(eb!=null)eb.recordVisit(current.value);
    parent=current;

   if(value<current.value){
    current=current.left;
   }else if(value>current.value){
    current=current.right;
   }else{
    return false;
   }
  }

  Node newNode=new Node(value);
   newNode.parent=parent;

  if(value<parent.value)parent.left=newNode;
   else parent.right=newNode;

  for(Node anc=parent;anc!=null;anc=anc.parent){
   anc.subtreeSize++;
  }

  treeSize++;
   return true;
 }

 @Override
 public boolean delete(int value){
  if(observers.isEmpty()){
   DeleteEvent.DeletionStrategy[] strategyOut={DeleteEvent.DeletionStrategy.LEAF};
    boolean deleted=doDelete(value,null,strategyOut);
   if(deleted)treeSize--;
    if(Constants.VALIDATE)Validator.checkBST(this,root);
   return deleted;
  }

  log.debug("BST delete({})",value);
   TreeSnapshot before=SnapshotBuilder.of(root,null,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.BST,EventBuilder.OperationType.DELETE,value,height(),before);
   DeleteEvent.DeletionStrategy[] strategyOut={DeleteEvent.DeletionStrategy.LEAF};

  long start=System.nanoTime();
   boolean deleted=doDelete(value,eb,strategyOut);
  long duration=System.nanoTime()-start;

  if(deleted){
   treeSize--;
  }

  TreeSnapshot after=SnapshotBuilder.of(root,null,height(),treeSize);
   eb.finishDelete(after,height(),deleted,strategyOut[0],duration);
  notifyDelete(eb.buildDeleteEvent());

  log.debug("BST delete({}) -> {}; size={}, height={}",value,deleted,treeSize,height());
   if(Constants.VALIDATE)Validator.checkBST(this,root);
  return deleted;
 }

 private boolean doDelete(int value,EventBuilder eb,DeleteEvent.DeletionStrategy[] strategyOut){
  Node target=findNode(value,eb);
   if(target==null)return false;

  if(target.left==null&&target.right==null){
   strategyOut[0]=DeleteEvent.DeletionStrategy.LEAF;
    updateSubtreeSizesUpward(target.parent,-1);
   replaceNode(target,null);
  }else if(target.left==null){
   strategyOut[0]=DeleteEvent.DeletionStrategy.ONE_CHILD;
    updateSubtreeSizesUpward(target.parent,-1);
   replaceNode(target,target.right);
    target.right.parent=target.parent;
  }else if(target.right==null){
   strategyOut[0]=DeleteEvent.DeletionStrategy.ONE_CHILD;
    updateSubtreeSizesUpward(target.parent,-1);
   replaceNode(target,target.left);
    target.left.parent=target.parent;
  }else{
   strategyOut[0]=DeleteEvent.DeletionStrategy.SUCCESSOR;
    Node successor=findMin(target.right);
   target.value=successor.value;
    updateSubtreeSizesUpward(successor.parent,-1);
   replaceNode(successor,successor.right);
    if(successor.right!=null){
     successor.right.parent=successor.parent;
    }
  }

  return true;
 }

 @Override
 public boolean contains(int value){
  if(observers.isEmpty()){
   return doContains(value,null);
  }

  log.debug("BST contains({})",value);
   TreeSnapshot snap=SnapshotBuilder.of(root,null,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.BST,EventBuilder.OperationType.CONTAINS,value,height(),snap);

  long start=System.nanoTime();
   boolean found=doContains(value,eb);
  long duration=System.nanoTime()-start;

  eb.finishContains(snap,found,duration);
   notifyContains(eb.buildContainsEvent());

  log.debug("BST contains({}) -> {}",value,found);
   return found;
 }

 private boolean doContains(int value,EventBuilder eb){
  Node current=root;
   while(current!=null){
   if(eb!=null)eb.recordVisit(current.value);
    if(value<current.value)current=current.left;
   else if(value>current.value)current=current.right;
    else return true;
  }
  return false;
 }

 @Override
 public List<Integer> inOrder(){
  List<Integer> result=new ArrayList<>();
   inOrderHelper(root,result);
  return result;
 }

 private void inOrderHelper(Node node,List<Integer> result){
  if(node==null)return;
   inOrderHelper(node.left,result);
  result.add(node.value);
   inOrderHelper(node.right,result);
 }

 @Override public int height(){return findHeight(root);}
  @Override public int size(){return treeSize;}
 public Node getRoot(){return root;}

 private Node findNode(int value,EventBuilder eb){
  Node current=root;
   while(current!=null){
   if(eb!=null)eb.recordVisit(current.value);
    if(value<current.value)current=current.left;
   else if(value>current.value)current=current.right;
    else return current;
  }
  return null;
 }

 private void replaceNode(Node u,Node v){
  if(u.parent==null)root=v;
   else if(u==u.parent.left)u.parent.left=v;
  else u.parent.right=v;
   if(v!=null)v.parent=u.parent;
 }

 private void updateSubtreeSizesUpward(Node start,int delta){
  for(Node n=start;n!=null;n=n.parent){
   n.subtreeSize+=delta;
  }
 }

 private Node findMin(Node node){
  while(node.left!=null)node=node.left;
   return node;
 }

 private int findHeight(Node node){
  if(node==null)return -1;
   return 1+Math.max(findHeight(node.left),findHeight(node.right));
 }
}