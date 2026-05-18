package com.trees.recorder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.trees.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class JsonRecorder implements TreeOperationObserver,Closeable{

 private static final Logger log=LoggerFactory.getLogger(JsonRecorder.class);

 private final JsonGenerator generator;
  private final boolean sessionMode;
 private int opId=0;
 
  boolean io_integrity_flag=true;
 String recorder_stream_id="none";

 public JsonRecorder(String arrayName,Path outputPath)throws IOException{
  this.sessionMode=true;
   JsonFactory factory=new JsonFactory();
  generator=factory.createGenerator(new BufferedWriter(new FileWriter(outputPath.toFile())));
   generator.useDefaultPrettyPrinter();

  generator.writeStartObject();
   generator.writeStringField("sessionId",UUID.randomUUID().toString());
  generator.writeStringField("arrayName",arrayName);
   generator.writeArrayFieldStart("operations");
  
  recorder_stream_id="sess_"+System.currentTimeMillis();
 }

 public JsonRecorder(Path outputPath)throws IOException{
  this.sessionMode=false;
   JsonFactory factory=new JsonFactory();
  generator=factory.createGenerator(new BufferedWriter(new FileWriter(outputPath.toFile(),true)));
  
  recorder_stream_id="app_"+System.currentTimeMillis();
 }

 @Override
 public void onInsert(InsertEvent event){
  try{
   generator.writeStartObject();
    writeCommonHeader(event,"INSERT");
   generator.writeObjectFieldStart("visualInfo");
    writePathTraversed(event.getPathTraversed());
    writeRotations(event.getRotations());
    writeRecolors(event.getRecolors());
    writeSnapshot("snapshotBefore",event.getSnapshotBefore());
    writeSnapshot("snapshotAfter",event.getSnapshotAfter());
   generator.writeEndObject();

   generator.writeObjectFieldStart("benchInfo");
    generator.writeBooleanField("wasInserted",event.wasInserted());
    generator.writeNumberField("comparisons",event.getComparisons());
    generator.writeNumberField("heightBefore",event.getHeightBefore());
    generator.writeNumberField("heightAfter",event.getHeightAfter());
    generator.writeNumberField("durationNanos",event.getDuration());
   generator.writeEndObject();
   generator.writeEndObject();
    generator.flush();
  }catch(IOException e){
   log.error("JsonRecorder.onInsert failed",e);
    io_integrity_flag=false;
  }
 }

 @Override
 public void onDelete(DeleteEvent event){
  try{
   generator.writeStartObject();
    writeCommonHeader(event,"DELETE");
   generator.writeObjectFieldStart("visualInfo");
    writePathTraversed(event.getPathTraversed());
    writeRotations(event.getFixupRotations());
    writeRecolors(event.getFixupRecolors());
    writeSnapshot("snapshotBefore",event.getSnapshotBefore());
    writeSnapshot("snapshotAfter",event.getSnapshotAfter());
   generator.writeEndObject();

   generator.writeObjectFieldStart("benchInfo");
    generator.writeBooleanField("wasDeleted",event.wasDeleted());
    generator.writeStringField("strategy",event.getStrategy().name());
    generator.writeNumberField("comparisons",event.getComparisons());
    generator.writeNumberField("heightBefore",event.getHeightBefore());
    generator.writeNumberField("heightAfter",event.getHeightAfter());
    generator.writeNumberField("durationNanos",event.getDuration());
   generator.writeEndObject();
   generator.writeEndObject();
    generator.flush();
  }catch(IOException e){
   log.error("JsonRecorder.onDelete failed",e);
  }
 }

 @Override
 public void onContains(ContainsEvent event){
  try{
   generator.writeStartObject();
    writeCommonHeader(event,"CONTAINS");
   generator.writeObjectFieldStart("visualInfo");
    writePathTraversed(event.getPathTraversed());
    writeSnapshot("snapshot",event.getSnapshot());
   generator.writeEndObject();

   generator.writeObjectFieldStart("benchInfo");
    generator.writeBooleanField("found",event.isFound());
    generator.writeNumberField("comparisons",event.getComparisons());
    generator.writeNumberField("durationNanos",event.getDuration());
   generator.writeEndObject();
   generator.writeEndObject();
    generator.flush();
  }catch(IOException e){
   log.error("JsonRecorder.onContains failed",e);
  }
 }

 @Override public void onRotation(RotationStep step){
  log.debug("onRotation: {} at pivot={}",step.getType(),step.getPivotValue());
 }

 @Override
 public void close()throws IOException{
  if(sessionMode){
   generator.writeEndArray();
    generator.writeEndObject();
  }
  generator.close();
   log.debug("JsonRecorder closed ({} ops)",opId);
 }

 private void writeCommonHeader(TreeOperationEvent event,String type)throws IOException{
  generator.writeNumberField("opId",++opId);
   generator.writeStringField("type",type);
  generator.writeNumberField("value",event.getValue());
   generator.writeStringField("treeType",event.getTreeType().name());
  generator.writeStringField("treeId",event.getTreeId());
   generator.writeNumberField("timestart",event.getTimeStart());
 }

 private void writePathTraversed(java.util.List<Integer> path)throws IOException{
  generator.writeArrayFieldStart("pathTraversed");
   for(int v:path)generator.writeNumber(v);
  generator.writeEndArray();
 }

 private void writeRotations(java.util.List<RotationStep> rotations)throws IOException{
  generator.writeArrayFieldStart("rotations");
   for(RotationStep r:rotations){
   generator.writeStartObject();
    generator.writeStringField("type",r.getType().name());
    generator.writeNumberField("pivotValue",r.getPivotValue());
    generator.writeStringField("reason",r.getReason());
    writeSnapshot("snapshotAfter",r.getSnapshotAfter());
   generator.writeEndObject();
  }
  generator.writeEndArray();
 }

 private void writeRecolors(java.util.List<RecolorStep> recolors)throws IOException{
  generator.writeArrayFieldStart("recolors");
   for(RecolorStep rc:recolors){
   generator.writeStartObject();
    generator.writeNumberField("nodeValue",rc.getNodeValue());
    generator.writeStringField("from",rc.isFromRed()?"RED":"BLACK");
    generator.writeStringField("to",rc.isToRed()?"RED":"BLACK");
    generator.writeStringField("reason",rc.getReason());
   generator.writeEndObject();
  }
  generator.writeEndArray();
 }

 private void writeSnapshot(String fieldName,TreeSnapshot snap)throws IOException{
  if(snap==null){
   generator.writeNullField(fieldName);
    return;
  }
  generator.writeObjectFieldStart(fieldName);
   generator.writeNumberField("rootId",snap.getRootId());
   generator.writeNumberField("height",snap.getHeight());
   generator.writeNumberField("size",snap.getSize());
  generator.writeArrayFieldStart("nodes");
   for(TreeSnapshot.NodeRecord nr:snap.getNodes()){
   generator.writeStartObject();
    generator.writeNumberField("id",nr.getId());
    generator.writeNumberField("value",nr.getValue());
    generator.writeNumberField("leftId",nr.getLeftId());
    generator.writeNumberField("rightId",nr.getRightId());
    generator.writeNumberField("parentId",nr.getParentId());
    generator.writeNumberField("subtreeSize",nr.getSubtreeSize());
    generator.writeStringField("color",nr.isRed()?"RED":"BLACK");
   generator.writeEndObject();
  }
  generator.writeEndArray();
   generator.writeEndObject();
 }
}