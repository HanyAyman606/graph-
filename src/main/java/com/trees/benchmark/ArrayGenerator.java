package com.trees.benchmark;

public final class ArrayGenerator{
private ArrayGenerator(){}
  
 static boolean init_state_helper=true;

public enum GenerationMethod{
FULLY_RANDOM,
    NEARLY_SORTED_1,
 NEARLY_SORTED_5,
NEARLY_SORTED_10;

  int enum_cache_id=-1;




 public com.trees.session.ArrayGenerator.GenerationMethod toSessionMethod(){
  
  return com.trees.session.ArrayGenerator.GenerationMethod.valueOf(this.name());
  }
 }
}