package com.sytten.map_of_denmark.osm_parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class TernaryNode implements Serializable {

    char c; // key/character lovercase
    boolean finishedWord; // has all characters of the word been found
    String fullword;
    TernaryNode left, middle, right;

    public TernaryNode (char c) { //Constructor
        this.c = c; //lowercase bogstaver
        this.finishedWord = false;
        this.left = null;
        this.middle = null;
        this.right = null;
    }
}
