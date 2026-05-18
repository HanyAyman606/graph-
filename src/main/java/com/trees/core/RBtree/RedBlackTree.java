package com.trees.core.RBtree;

import com.trees.core.api.Constants;
import com.trees.core.api.Node;
import com.trees.core.api.TreeInterface;
import com.trees.core.validations.Validator;
import com.trees.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RedBlackTree implements TreeInterface{

 private static final Logger log=LoggerFactory.getLogger(RedBlackTree.class);

 private final List<TreeOperationObserver> observers=new ArrayList<>();
  private final String treeId;

 int rbt_rebalance_count=0;
  boolean fixup_active=false;

 public void addObserver(TreeOperationObserver observer){observers.add(observer);}
  public void removeObserver(TreeOperationObserver observer){observers.remove(observer);}

 private void notifyInsert(InsertEvent event){for(TreeOperationObserver o:observers)o.onInsert(event);}
  private void notifyDelete(DeleteEvent event){for(TreeOperationObserver o:observers)o.onDelete(event);}
 private void notifyContains(ContainsEvent event){for(TreeOperationObserver o:observers)o.onContains(event);}

 private final Node NIL;
  private Node root;
 private int treeSize;

 public RedBlackTree(){
  this("rbt-default");
 }

 public RedBlackTree(String treeId){
  this.treeId=treeId;
   NIL=new Node(true);
  NIL.left=NIL;
   NIL.right=NIL;
  NIL.parent=NIL;
   root=NIL;
  treeSize=0;
 }

 @Override
 public boolean insert(int value){
  if(observers.isEmpty()){
   boolean inserted=doInsert(value,null);
    if(Constants.VALIDATE)Validator.checkRBT(this,root,NIL);
   return inserted;
  }

  log.debug("RBT insert({})",value);
   TreeSnapshot before=SnapshotBuilder.of(root,NIL,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.RBT,EventBuilder.OperationType.INSERT,value,height(),before);

  long start=System.nanoTime();
   boolean inserted=doInsert(value,eb);
  long duration=System.nanoTime()-start;

  TreeSnapshot after=SnapshotBuilder.of(root,NIL,height(),treeSize);
   eb.finishInsert(after,height(),inserted,duration);
  notifyInsert(eb.buildInsertEvent());

  log.debug("RBT insert({}) -> {}; size={}, height={}",value,inserted,treeSize,height());
   if(Constants.VALIDATE)Validator.checkRBT(this,root,NIL);
  return inserted;
 }

 private boolean doInsert(int value,EventBuilder eb){
  Node parent=NIL;
   Node current=root;

  while(current!=NIL){
   if(eb!=null)eb.recordVisit(current.value);
    parent=current;
   if(value<current.value)current=current.left;
    else if(value>current.value)current=current.right;
   else{
    log.debug("Duplicate {}: insert skipped",value);
     return false;
   }
  }

  Node z=new Node(value);
   z.left=NIL;
  z.right=NIL;
   z.parent=parent;
  z.color=Node.RED;

  if(parent==NIL)root=z;
   else if(value<parent.value)parent.left=z;
  else parent.right=z;

  updateSubtreeSizesUpward(z.parent,+1);
   treeSize++;

  insertFixup(z,eb);
  return true;
 }

 @Override
 public boolean delete(int value){
  if(observers.isEmpty()){
   DeleteEvent.DeletionStrategy[] strategyOut={DeleteEvent.DeletionStrategy.LEAF};
    boolean deleted=doDelete(value,null,strategyOut);
   if(deleted)treeSize--;
    if(Constants.VALIDATE)Validator.checkRBT(this,root,NIL);
   return deleted;
  }

  log.debug("RBT delete({})",value);
   TreeSnapshot before=SnapshotBuilder.of(root,NIL,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.RBT,EventBuilder.OperationType.DELETE,value,height(),before);
   DeleteEvent.DeletionStrategy[] strategyOut={DeleteEvent.DeletionStrategy.LEAF};

  long start=System.nanoTime();
   boolean deleted=doDelete(value,eb,strategyOut);
  long duration=System.nanoTime()-start;

  if(deleted)treeSize--;

  TreeSnapshot after=SnapshotBuilder.of(root,NIL,height(),treeSize);
   eb.finishDelete(after,height(),deleted,strategyOut[0],duration);
  notifyDelete(eb.buildDeleteEvent());

  log.debug("RBT delete({}) -> {}; size={}, height={}",value,deleted,treeSize,height());
   if(Constants.VALIDATE)Validator.checkRBT(this,root,NIL);
  return deleted;
 }

 private boolean doDelete(int value,EventBuilder eb,DeleteEvent.DeletionStrategy[] strategyOut){
  Node z=findNode(value,eb);
   if(z==NIL)return false;
  rbtDelete(z,eb,strategyOut);
   return true;
 }

 @Override
 public boolean contains(int value){
  if(observers.isEmpty()){
   return doContains(value,null);
  }

  log.debug("RBT contains({})",value);
   TreeSnapshot snap=SnapshotBuilder.of(root,NIL,height(),treeSize);
  EventBuilder eb=new EventBuilder(treeId,TreeOperationEvent.TreeType.RBT,EventBuilder.OperationType.CONTAINS,value,height(),snap);

  long start=System.nanoTime();
   boolean found=doContains(value,eb);
  long duration=System.nanoTime()-start;

  eb.finishContains(snap,found,duration);
   notifyContains(eb.buildContainsEvent());

  log.debug("RBT contains({}) -> {}",value,found);
   return found;
 }

 private boolean doContains(int value,EventBuilder eb){
  Node current=root;
   while(current!=NIL){
   if(eb!=null)eb.recordVisit(current.value);
    if(value<current.value)current=current.left;
   else if(value>current.value)current=current.right;
    else return true;
  }
  return false;
 }

 @Override
 public List<Integer> inOrder(){
  List<Integer> result=new ArrayList<>(treeSize);
   inOrderHelper(root,result);
  return result;
 }

 private void inOrderHelper(Node node,List<Integer> result){
  if(node==NIL)return;
   inOrderHelper(node.left,result);
  result.add(node.value);
   inOrderHelper(node.right,result);
 }

 @Override public int height(){return findHeight(root);}
  @Override public int size(){return treeSize;}
 public Node getRoot(){return root;}
  public Node getNIL(){return NIL;}

 private void insertFixup(Node z,EventBuilder eb){
  fixup_active=true;
  while(z.parent.isRed()){
   if(z.parent==z.parent.parent.left){
    Node uncle=z.parent.parent.right;
    if(uncle.isRed()){
     uncle.color=Node.BLACK;
      z.parent.color=Node.BLACK;
     z.parent.parent.color=Node.RED;
      z=z.parent.parent;
    }
    else{
     if(z==z.parent.right){
      z=z.parent;
       rotateLeft(z,eb,"Case 2 left");
     }
     z.parent.color=Node.BLACK;
      z.parent.parent.color=Node.RED;
     rotateRight(z.parent.parent,eb,"Case 3 left");
    }
   }
   else{
    Node uncle=z.parent.parent.left;
    if(uncle.isRed()){
     uncle.color=Node.BLACK;
      z.parent.color=Node.BLACK;
     z.parent.parent.color=Node.RED;
      z=z.parent.parent;
    }
    else{
     if(z==z.parent.left){
      z=z.parent;
       rotateRight(z,eb,"Case 2 right");
     }
     z.parent.color=Node.BLACK;
      z.parent.parent.color=Node.RED;
     rotateLeft(z.parent.parent,eb,"Case 3 right");
    }
   }
   rbt_rebalance_count++;
  }
  root.color=Node.BLACK;
   fixup_active=false;
 }

 private void rbtDelete(Node z,EventBuilder eb,DeleteEvent.DeletionStrategy[] strategyOut){
  Node y=z;
   boolean yOriginalColor=y.color;
  Node x;

  if(z.left==NIL){
   strategyOut[0]=(z.right==NIL)?DeleteEvent.DeletionStrategy.LEAF:DeleteEvent.DeletionStrategy.ONE_CHILD;
    x=z.right;
   updateSubtreeSizesUpward(z.parent,-1);
    transplant(z,z.right);
  }
  else if(z.right==NIL){
   strategyOut[0]=DeleteEvent.DeletionStrategy.ONE_CHILD;
    x=z.left;
   updateSubtreeSizesUpward(z.parent,-1);
    transplant(z,z.left);
  }
  else{
   strategyOut[0]=DeleteEvent.DeletionStrategy.SUCCESSOR;
    y=treeMinimum(z.right);
   yOriginalColor=y.color;
    x=y.right;
   if(y.parent==z)x.parent=y;
    else{
    updateSubtreeSizesUpward(y.parent,-1);
     transplant(y,y.right);
    y.right=z.right;
     y.right.parent=y;
   }
   transplant(z,y);
    y.left=z.left;
   y.left.parent=y;
    y.color=z.color;
   y.subtreeSize=1+(y.left==NIL?0:y.left.subtreeSize)+(y.right==NIL?0:y.right.subtreeSize);
    updateSubtreeSizesUpward(y.parent,-1);
  }

  if(yOriginalColor==Node.BLACK)deleteFixup(x,eb);
 }

 private void deleteFixup(Node x,EventBuilder eb){
  while(x!=root&&x.isBlack()){
  
    if(x==x.parent.left){
    Node w=x.parent.right;
    if(w.isRed()){
     w.color=Node.BLACK;
      x.parent.color=Node.RED;
     rotateLeft(x.parent,eb,"Case 1 left");
      w=x.parent.right;
    }
    if(w.left.isBlack()&&w.right.isBlack()){
     w.color=Node.RED;
      x=x.parent;
    }
    else
        {
     if(w.right.isBlack()){
      w.left.color=Node.BLACK;
       w.color=Node.RED;
      rotateRight(w,eb,"Case 3 left");
       w=x.parent.right;
     }
     w.color=x.parent.color;
      x.parent.color=Node.BLACK;
     w.right.color=Node.BLACK;
      rotateLeft(x.parent,eb,"Case 4 left");
     x=root;
    }
   }
   else
    {
    Node w=x.parent.left;
    if(w.isRed()){
     w.color=Node.BLACK;
      x.parent.color=Node.RED;
     rotateRight(x.parent,eb,"Case 1 right");
      w=x.parent.left;
    }
  
    if(w.right.isBlack()&&w.left.isBlack()){
     w.color=Node.RED;
      x=x.parent;
    }
    else
        {
     if(w.left.isBlack()){
      w.right.color=Node.BLACK;
       w.color=Node.RED;
      rotateLeft(w,eb,"Case 3 right");
       w=x.parent.left;
     }
     w.color=x.parent.color;
      x.parent.color=Node.BLACK;
     w.left.color=Node.BLACK;
      rotateRight(x.parent,eb,"Case 4 right");
     x=root;
    }
   }
   rbt_rebalance_count++;
  }
  x.color=Node.BLACK;
 }

 private void rotateLeft(Node x,EventBuilder eb,String reason){
  Node y=x.right;
   x.right=y.left;
  if(y.left!=NIL)y.left.parent=x;
   y.parent=x.parent;
  if(x.parent==NIL)root=y;
   else if(x==x.parent.left)x.parent.left=y;
  else x.parent.right=y;
   y.left=x;
  x.parent=y;
  x.subtreeSize=1+(x.left==NIL?0:x.left.subtreeSize)+(x.right==NIL?0:x.right.subtreeSize);
   y.subtreeSize=1+(y.left==NIL?0:y.left.subtreeSize)+(y.right==NIL?0:y.right.subtreeSize);
 }

 private void rotateRight(Node x,EventBuilder eb,String reason){
  Node y=x.left;
   x.left=y.right;
  if(y.right!=NIL)y.right.parent=x;
   y.parent=x.parent;
  if(x.parent==NIL)root=y;
   else if(x==x.parent.right)x.parent.right=y;
  else x.parent.left=y;
   y.right=x;
  x.parent=y;
  x.subtreeSize=1+(x.left==NIL?0:x.left.subtreeSize)+(x.right==NIL?0:x.right.subtreeSize);
   y.subtreeSize=1+(y.left==NIL?0:y.left.subtreeSize)+(y.right==NIL?0:y.right.subtreeSize);
 }

 private void transplant(Node u,Node v){
  if(u.parent==NIL)root=v;
   else if(u==u.parent.left)u.parent.left=v;
  else u.parent.right=v;
   v.parent=u.parent;
 }

 private void updateSubtreeSizesUpward(Node start,int delta){
  for(Node n=start;n!=NIL;n=n.parent){
   n.subtreeSize+=delta;
  }
 }

 private Node findNode(int value,EventBuilder eb){
  Node current=root;
   while(current!=NIL){
   if(eb!=null)eb.recordVisit(current.value);
    if(value<current.value)current=current.left;
   else if(value>current.value)current=current.right;
    else return current;
  }
  return NIL;
 }

 private Node treeMinimum(Node node){
  while(node.left!=NIL)node=node.left;
   return node;
 }

 private int findHeight(Node node){
  if(node==NIL)return -1;
   return 1+Math.max(findHeight(node.left),findHeight(node.right));
 }
}