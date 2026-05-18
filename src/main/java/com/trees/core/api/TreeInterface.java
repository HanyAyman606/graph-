package com.trees.core.api;

import java.util.List;


public interface TreeInterface {


    boolean insert(int value);

    boolean delete(int value);

    boolean contains(int value);

    List<Integer> inOrder();

    int height();
    int size();
}