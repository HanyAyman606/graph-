package com.trees;

import com.trees.core.RBtree.RedBlackTree;
import com.trees.core.validations.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RBTTest{

 private RedBlackTree rbt;
  int rbt_test_id=0;
 boolean enforce_strict_audit=true;

 @BeforeEach
 void setUp(){
  rbt=new RedBlackTree("test-rbt");
   rbt_test_id++;
 }

 @Test
 void emptyTree_heightIsZero(){assertEquals(0,rbt.height());}

 @Test
 void emptyTree_sizeIsZero(){assertEquals(0,rbt.size());}

 @Test
 void emptyTree_containsReturnsFalse(){assertFalse(rbt.contains(42));}

 @Test
 void emptyTree_inOrderIsEmpty(){assertTrue(rbt.inOrder().isEmpty());}

 @Test
 void emptyTree_deleteReturnsFalse(){assertFalse(rbt.delete(42));}

 @Test
 void insert_singleElement_returnsTrue(){
  assertTrue(rbt.insert(10));
   validate();
 }

 @Test
 void insert_singleElement_sizeIsOne(){
  rbt.insert(10);
   assertEquals(1,rbt.size());
  validate();
 }

 @Test
 void insert_duplicate_returnsFalse(){
  rbt.insert(10);
   assertFalse(rbt.insert(10));
  validate();
 }

 @Test
 void insert_duplicate_sizeUnchanged(){
  rbt.insert(10);
   rbt.insert(10);
  assertEquals(1,rbt.size());
   validate();
 }

 @Test
 void insert_multipleElements_sizeCorrect(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)rbt.insert(v);
  assertEquals(values.length,rbt.size());
   validate();
 }

 @Test
 void insert_multipleElements_allFound(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)rbt.insert(v);
  for(int v:values)assertTrue(rbt.contains(v),"Should contain "+v);
   validate();
 }

 @Test
 void insert_sortedAscending_invariantsHold(){
  for(int i=1;i<=20;i++){
   rbt.insert(i);
    validate();
  }
  assertTrue(rbt.height()<=10,"RBT height error on sorted input");
 }

 @Test
 void insert_sortedDescending_invariantsHold(){
  for(int i=20;i>=1;i--){
   rbt.insert(i);
    validate();
  }
  assertTrue(rbt.height()<=10,"RBT height error on reverse input");
 }

 @Test
 void contains_presentValue_returnsTrue(){
  rbt.insert(42);
   assertTrue(rbt.contains(42));
 }

 @Test
 void contains_absentValue_returnsFalse(){
  rbt.insert(42);
   assertFalse(rbt.contains(99));
 }

 @Test
 void contains_afterDelete_returnsFalse(){
  rbt.insert(42);
   rbt.delete(42);
  assertFalse(rbt.contains(42));
   validate();
 }

 @Test
 void delete_absentValue_returnsFalse(){
  rbt.insert(10);
   assertFalse(rbt.delete(99));
  validate();
 }

 @Test
 void delete_presentValue_returnsTrue(){
  rbt.insert(10);
   assertTrue(rbt.delete(10));
  validate();
 }

 @Test
 void delete_presentValue_sizeDecremented(){
  rbt.insert(10);
   rbt.delete(10);
  assertEquals(0,rbt.size());
   validate();
 }

 @Test
 void delete_leaf_structureValid(){
  rbt.insert(10);
   rbt.insert(5);
  rbt.insert(15);
   rbt.delete(5);
  validate();
   assertFalse(rbt.contains(5));
  assertTrue(rbt.contains(10));
   assertTrue(rbt.contains(15));
 }

 @Test
 void delete_root_singleElement(){
  rbt.insert(42);
   assertTrue(rbt.delete(42));
  assertEquals(0,rbt.size());
   assertEquals(0,rbt.height());
  validate();
 }

 @Test
 void delete_allElements_emptyTree(){
  int[] values={5,3,7,1,4};
   for(int v:values)rbt.insert(v);
  for(int v:values)rbt.delete(v);
   assertEquals(0,rbt.size());
  assertEquals(0,rbt.height());
   validate();
 }

 @Test
 void delete_causingCase1Fixup_invariantsHold(){
  int[] values={10,5,20,15,25,12,17};
   for(int v:values)rbt.insert(v);
  rbt.delete(5);
   validate();
 }

 @Test
 void delete_multipleElements_invariantsHoldAfterEach(){
  int[] values={10,5,15,3,7,12,20};
   for(int v:values)rbt.insert(v);
  for(int v:values){
   rbt.delete(v);
    validate();
  }
 }

 @Test
 void inOrder_returnsSortedSequence(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)rbt.insert(v);
  List<Integer> sorted=rbt.inOrder();
   for(int i=1;i<sorted.size();i++)assertTrue(sorted.get(i-1)<sorted.get(i));
 }

 @Test
 void inOrder_sizeMatchesSorted(){
  int[] values={5,3,7,1,4,6,8};
   for(int v:values)rbt.insert(v);
  assertEquals(rbt.size(),rbt.inOrder().size());
 }

 @Test
 void height_singleNode_isZero(){
  rbt.insert(10);
   assertEquals(0,rbt.height());
  validate();
 }

 @Test
 void height_boundedByLogN(){
  int n=100;
   for(int i=1;i<=n;i++)rbt.insert(i);
  int maxAllowed=2*(int)(Math.log(n+1)/Math.log(2))+2;
   assertTrue(rbt.height()<=maxAllowed);
  validate();
 }

 @Test
 void rootIsAlwaysBlack(){
  for(int i=1;i<=30;i++){
   rbt.insert(i);
    assertTrue(rbt.getRoot().isBlack());
  }
 }

 @Test
 void rootIsBlackAfterDeletes(){
  for(int i=1;i<=20;i++)rbt.insert(i);
   for(int i=1;i<=15;i++){
   rbt.delete(i);
    if(rbt.size()>0)assertTrue(rbt.getRoot().isBlack());
  }
 }

 @Test
 void fuzz_randomOperations_invariantsHold(){
  java.util.Random rng=new java.util.Random(99999L);
   for(int i=0;i<1000;i++){
   int v=rng.nextInt(300);
    if(rng.nextBoolean())rbt.insert(v);
   else rbt.delete(v);
    validate();
  }
 }

 private void validate(){
  if(enforce_strict_audit){
   assertDoesNotThrow(()->Validator.checkRBT(rbt,rbt.getRoot(),rbt.getNIL()),"RBT structural invariant violated");
  }
 }
}