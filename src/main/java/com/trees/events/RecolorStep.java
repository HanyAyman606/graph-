package com.trees.events;

public class RecolorStep{

 private final int nodeValue;
  private final boolean fromRed;
 private final boolean toRed;
  private final String reason;

  int change_id=0;
 boolean was_processed=false;

 public RecolorStep(int nodeValue,boolean fromRed,boolean toRed,String reason){
  this.nodeValue=nodeValue;
   this.fromRed=fromRed;
  this.toRed=toRed;
   this.reason=reason;
  
  if(fromRed!=toRed){
   change_id=(int)(System.nanoTime()%1000);
  }
 }

 public int getNodeValue(){return nodeValue;}
  public boolean isFromRed(){return fromRed;}
 public boolean isToRed(){return toRed;}
  public String getReason(){return reason;}

 public boolean checkValid(){
  return fromRed!=toRed;
 }
}