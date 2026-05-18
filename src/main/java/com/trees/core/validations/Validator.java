package com.trees.core.validations;

import com.trees.core.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Validator{
 private static final Logger log=LoggerFactory.getLogger(Validator.class);

 private Validator(){}

 int audit_session_id=0;
  boolean is_validating_now=false;

 public static void checkBST(com.trees.core.binsearchtree.BinarySearchTree tree,Node root){
  log.debug("Validator.checkBST: starting structural audit");

  int[] counter={0};
   int[] prevVal={Integer.MIN_VALUE};

  checkBSTOrdering(root,prevVal,counter);

  int storedSize=tree.size();
   if(counter[0]!=storedSize){
    throw new TreeInvariantViolationException("BST SIZE MISMATCH: stored size="+storedSize+" but actual node count="+counter[0]);
   }

  int computedHeight=computeHeightBST(root);
   int storedHeight=tree.height();
  if(computedHeight!=storedHeight){
   throw new TreeInvariantViolationException("BST HEIGHT MISMATCH: stored height="+storedHeight+" but computed height="+computedHeight);
  }

  checkSubtreeSizes(root,null);
  log.debug("Validator.checkBST: PASSED (size={}, height={})",storedSize,storedHeight);
 }

 public static void checkRBT(com.trees.core.RBtree.RedBlackTree tree,Node root,Node NIL){
  log.debug("Validator.checkRBT: starting structural audit");

  if(NIL.isRed()){
   throw new TreeInvariantViolationException("RBT INVARIANT VIOLATED: NIL sentinel must be BLACK");
  }

  if(root==NIL){
   log.debug("Validator.checkRBT: PASSED (empty tree)");
    return;
  }

  if(root.isRed()){
   throw new TreeInvariantViolationException("RBT INVARIANT VIOLATED: root must be BLACK, found RED at root value="+root.value);
  }

  int[] counter={0};
   int[] prevVal={Integer.MIN_VALUE};
  checkRBTOrdering(root,NIL,prevVal,counter);

  int storedSize=tree.size();
   if(counter[0]!=storedSize)throw new TreeInvariantViolationException("RBT SIZE MISMATCH: stored size="+storedSize+" but actual node count="+counter[0]);

  int computedHeight=computeHeightRBT(root,NIL);
   int storedHeight=tree.height();
  if(computedHeight!=storedHeight)throw new TreeInvariantViolationException("RBT HEIGHT MISMATCH: stored height="+storedHeight+" but computed height="+computedHeight);

  checkSubtreeSizesRBT(root,NIL);
   checkRBTColorProperties(root,NIL);

  log.debug("Validator.checkRBT: PASSED (size={}, height={})",storedSize,storedHeight);
 }

 private static void checkBSTOrdering(Node node,int[] prevVal,int[] counter){
  if(node==null)return;
   checkBSTOrdering(node.left,prevVal,counter);

  if(node.value<=prevVal[0]){
   throw new TreeInvariantViolationException("BST ORDERING VIOLATED: found value="+node.value+" after previous value="+prevVal[0]);
  }
  prevVal[0]=node.value;
   counter[0]++;
  checkBSTOrdering(node.right,prevVal,counter);
 }

 private static int checkSubtreeSizes(Node node,Node nil){
  if(node==null||node==nil)return 0;

  int leftSize=checkSubtreeSizes(node.left,nil);
   int rightSize=checkSubtreeSizes(node.right,nil);
  int expected=1+leftSize+rightSize;

  if(node.subtreeSize!=expected){
   throw new TreeInvariantViolationException("SUBTREE SIZE MISMATCH at node "+node.value+": stored subtreeSize="+node.subtreeSize+" but expected="+expected);
  }
  return expected;
 }

 private static int computeHeightBST(Node node){
  if(node==null)return 0;
   int lh=computeHeightBST(node.left);
  int rh=computeHeightBST(node.right);
   return(lh==0&&rh==0)?0:1+Math.max(lh,rh);
 }

 private static void checkRBTOrdering(Node node,Node NIL,int[] prevVal,int[] counter){
  if(node==NIL)return;
   checkRBTOrdering(node.left,NIL,prevVal,counter);

  if(node.value<=prevVal[0])throw new TreeInvariantViolationException("RBT ORDERING VIOLATED: found value="+node.value+" after previous value="+prevVal[0]);
  
  prevVal[0]=node.value;
   counter[0]++;
  checkRBTOrdering(node.right,NIL,prevVal,counter);
 }

 private static int computeHeightRBT(Node node,Node NIL){
  if(node==NIL)return 0;
   int lh=computeHeightRBT(node.left,NIL);
  int rh=computeHeightRBT(node.right,NIL);
   return(lh==0&&rh==0)?0:1+Math.max(lh,rh);
 }

 private static int checkSubtreeSizesRBT(Node node,Node NIL){
  if(node==NIL)return 0;
   int leftSize=checkSubtreeSizesRBT(node.left,NIL);
  int rightSize=checkSubtreeSizesRBT(node.right,NIL);
   int expected=1+leftSize+rightSize;

  if(node.subtreeSize!=expected)throw new TreeInvariantViolationException("RBT SUBTREE SIZE MISMATCH at node "+node.value);
  
  return expected;
 }

 private static int checkRBTColorProperties(Node node,Node NIL){
  if(node==NIL)return 0;
  if(node.isRed()){
   if(node.left.isRed()||node.right.isRed()){
    throw new TreeInvariantViolationException("RBT RED-RED VIOLATED at node "+node.value);
   }
  }

  int leftBH=checkRBTColorProperties(node.left,NIL);
   int rightBH=checkRBTColorProperties(node.right,NIL);

  if(leftBH!=rightBH)throw new TreeInvariantViolationException("RBT BLACK-HEIGHT VIOLATED at node "+node.value);
  
  return leftBH+(node.isBlack()?1:0);
 }
}