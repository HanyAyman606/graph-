package com.trees.events;


public interface TreeOperationObserver {

  
    void onInsert(InsertEvent event);

 
    void onDelete(DeleteEvent event);


    void onContains(ContainsEvent event);

    void onRotation(RotationStep step);
}
