package com.trees.core.api;

public class Node {

    public static final boolean RED=true;
  
 public static final boolean BLACK=false;

public Node parent;
public int value;
public Node left;
public Node right;
 public int subtreeSize;
 public boolean color;

    public Node(int value)
    
    {
    
        this.value       = value;
    
        this.left        = null;
    
        this.right       = null;
    
        this.parent      = null;
    
        this.subtreeSize = 1;
    
        this.color       = RED;   
    }


    public Node(boolean isNill) 
    
    {
    
        this.value       = 0;
    
        this.left        = null;
    
        this.right       = null;
    
        this.parent      = null;
    
        this.subtreeSize = 0;
    
        this.color       = BLACK; 
    }

 
    public boolean isRed() 
    
    {
        return color == RED;
    }

    public boolean isBlack() 
    
    {
        return color == BLACK;
    }

 
    @Override
    public String toString()
    
    {
        return "Node(" + value + ", size=" + subtreeSize + ", " +(color== RED ? "RED" : "BLACK")+ ")";
    }
}