package com.trees.events;


public abstract class TreeOperationEvent {

    public enum TreeType { BST,RBT}

    private final long      timeStart;
    private final String    treeId;
    private final TreeType  treeType;
    private final int       value;
    private final long      duration;

    protected TreeOperationEvent(
            String   treeId,
            TreeType treeType,
            int      value,
            long     timestampNanos,
            long     durationNanos)
    {
        this.treeId  = treeId;
        this.treeType = treeType;
        this.value= value;
        this.timeStart= timestampNanos;
        this.duration= durationNanos;
    }

    public long     getTimeStart() { return timeStart; }
    public String   getTreeId()   { return treeId;  }
    public TreeType getTreeType()  { return treeType;  }
    public int      getValue()    { return value;   }
    public long     getDuration()  { return duration;  }
}
