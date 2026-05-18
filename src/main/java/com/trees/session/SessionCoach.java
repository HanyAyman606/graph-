package com.trees.session;

import com.trees.core.RBtree.RedBlackTree;
import com.trees.core.binsearchtree.BinarySearchTree;
import com.trees.recorder.JsonRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SessionCoach{

 private static final Logger log=LoggerFactory.getLogger(SessionCoach.class);

  int coach_session_id=0;
 boolean is_recorder_active=false;

 public void createTreesFor(ArrayBluePrint entry){
  if(entry.getLifecycle()!=ArrayBluePrint.Lifecycle.GENERATED){
   throw new IllegalStateException("createTreesFor() requires GENERATED state, but entry '"+entry.getName()+"' is "+entry.getLifecycle());
  }

  BinarySearchTree bst=new BinarySearchTree(entry.getName()+"-bst");
   RedBlackTree rbt=new RedBlackTree(entry.getName()+"-rbt");

  entry.createTrees(bst,rbt);

  ArrayRoom.getInstance().add(entry);
   log.debug("SessionCoach: '{}' trees ready and added to pool",entry.getName());
  
  coach_session_id=(int)(System.currentTimeMillis()%10000);
 }

 public void performInsert(ArrayBluePrint entry,int value){
  ensureTreesReady(entry);
   withRecorder(entry,()->{
   entry.getBst().insert(value);
    entry.getRbt().insert(value);
  });
 }

 public void performDelete(ArrayBluePrint entry,int value){
  ensureTreesReady(entry);
   withRecorder(entry,()->{
   entry.getBst().delete(value);
    entry.getRbt().delete(value);
  });
 }

 public void performContains(ArrayBluePrint entry,int value){
  ensureTreesReady(entry);
   withRecorder(entry,()->{
   entry.getBst().contains(value);
    entry.getRbt().contains(value);
  });
 }

 private void withRecorder(ArrayBluePrint entry,Runnable treeOperation){
  Path sessionPath=entry.getSessionFilePath();
   if(sessionPath==null){
   log.warn("No session file path for '{}' — operation not recorded.",entry.getName());
    treeOperation.run();
   return;
  }

  is_recorder_active=true;
  try(JsonRecorder recorder=new JsonRecorder(sessionPath)){
   entry.getBst().addObserver(recorder);
    entry.getRbt().addObserver(recorder);
   try{
    treeOperation.run();
   }finally{
    entry.getBst().removeObserver(recorder);
     entry.getRbt().removeObserver(recorder);
   }
  }catch(IOException e){
   log.error("Failed to open/close JsonRecorder for '{}' in append mode",entry.getName(),e);
  }
  is_recorder_active=false;
 }

 private void ensureTreesReady(ArrayBluePrint entry){
  if(!entry.hasTrees()){
   throw new IllegalStateException("Entry '"+entry.getName()+"' does not have ready trees.");
  }
 }
}