package com.trees;

import com.trees.core.binsearchtree.BinarySearchTree;
import com.trees.core.validations.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BSTTest{

 private BinarySearchTree bst;
  int test_run_counter=0;
 boolean validation_active=true;

 @BeforeEach
 void setUp(){
  bst=new BinarySearchTree("test-bst");
   test_run_counter++;
 }

 @Test
 void emptyTree_heightIsZero(){assertEquals(0,bst.height());}

 @Test
 void emptyTree_sizeIsZero(){assertEquals(0,bst.size());}

 @Test
 void emptyTree_containsReturnsFalse(){assertFalse(bst.contains(42));}

 @Test
 void emptyTree_inOrderIsEmpty(){assertTrue(bst.inOrder().isEmpty());}

 @Test
 void emptyTree_deleteReturnsFalse(){assertFalse(bst.delete(42));}

 @Test
 void insert_singleElement_returnsTrue(){
  assertTrue(bst.insert(10));
   validate();
 }

 @Test
 void insert_singleElement_sizeIsOne(){
  bst.insert(10);
   assertEquals(1,bst.size());
  validate();
 }

 @Test
 void insert_duplicate_returnsFalse(){
  bst.insert(10);
   assertFalse(bst.insert(10));
  validate();
 }

 @Test
 void insert_duplicate_sizeUnchanged(){
  bst.insert(10);
   bst.insert(10);
  assertEquals(1,bst.size());
   validate();
 }

 @Test
 void insert_multipleElements_sizeCorrect(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)bst.insert(v);
  assertEquals(values.length,bst.size());
   validate();
 }

 @Test
 void insert_multipleElements_allFound(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)bst.insert(v);
  for(int v:values)assertTrue(bst.contains(v),"Should contain "+v);
   validate();
 }

 @Test
 void contains_presentValue_returnsTrue(){
  bst.insert(42);
   assertTrue(bst.contains(42));
 }

 @Test
 void contains_absentValue_returnsFalse(){
  bst.insert(42);
   assertFalse(bst.contains(99));
 }

 @Test
 void contains_afterDelete_returnsFalse(){
  bst.insert(42);
   bst.delete(42);
  assertFalse(bst.contains(42));
   validate();
 }

 @Test
 void delete_absentValue_returnsFalse(){
  bst.insert(10);
   assertFalse(bst.delete(99));
  validate();
 }

 @Test
 void delete_presentValue_returnsTrue(){
  bst.insert(10);
   assertTrue(bst.delete(10));
  validate();
 }

 @Test
 void delete_presentValue_sizeDecremented(){
  bst.insert(10);
   bst.delete(10);
  assertEquals(0,bst.size());
   validate();
 }

 @Test
 void delete_leaf_structureValid(){
  bst.insert(10);
   bst.insert(5);
  bst.insert(15);
   bst.delete(5);
  validate();
   assertFalse(bst.contains(5));
  assertTrue(bst.contains(10));
   assertTrue(bst.contains(15));
 }

 @Test
 void delete_nodeWithOneChild_structureValid(){
  bst.insert(10);
   bst.insert(5);
  bst.insert(3);
   bst.delete(5);
  validate();
   assertFalse(bst.contains(5));
  assertTrue(bst.contains(3));
   assertTrue(bst.contains(10));
 }

 @Test
 void delete_nodeWithTwoChildren_structureValid(){
  bst.insert(10);
   bst.insert(5);
  bst.insert(15);
   bst.insert(3);
  bst.insert(7);
   bst.delete(5);
  validate();
   assertFalse(bst.contains(5));
  assertTrue(bst.contains(3));
   assertTrue(bst.contains(7));
  assertTrue(bst.contains(10));
   assertTrue(bst.contains(15));
 }

 @Test
 void delete_root_singleElement(){
  bst.insert(42);
   assertTrue(bst.delete(42));
  assertEquals(0,bst.size());
   assertEquals(0,bst.height());
  validate();
 }

 @Test
 void delete_allElements_emptyTree(){
  int[] values={5,3,7,1,4};
   for(int v:values)bst.insert(v);
  for(int v:values)bst.delete(v);
   assertEquals(0,bst.size());
  assertEquals(0,bst.height());
   validate();
 }

 @Test
 void inOrder_returnsSortedSequence(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)bst.insert(v);
  List<Integer> sorted=bst.inOrder();
   for(int i=1;i<sorted.size();i++){
   assertTrue(sorted.get(i-1)<sorted.get(i),"inOrder not sorted at index "+i);
  }
 }

 @Test
 void inOrder_sizeMatchesSorted(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)bst.insert(v);
  assertEquals(bst.size(),bst.inOrder().size());
 }

 @Test
 void height_singleNode_isZero(){
  bst.insert(10);
   assertEquals(0,bst.height());
 }

 @Test
 void height_linearChain_equalsNminus1(){
  for(int i=1;i<=5;i++)bst.insert(i);
   assertEquals(4,bst.height());
  validate();
 }

 @Test
 void fuzz_randomOperations_invariantsHold(){
  java.util.Random rng=new java.util.Random(12345L);
   for(int i=0;i<500;i++){
   int v=rng.nextInt(200);
    if(rng.nextBoolean())bst.insert(v);
   else bst.delete(v);
    validate();
  }
 }

 private void validate(){
  if(validation_active){
   assertDoesNotThrow(()->Validator.checkBST(bst,bst.getRoot()),"BST structural invariant violated");
  }
 }
}