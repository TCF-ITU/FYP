package com.sytten.map_of_denmark.osm_parsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TernarySearchTree implements Serializable {
    /*Here we make our algorithm.
    Our algorithm will take the current key we are at
    and check if it is lesser, equal or greater than the
    character at the root*/
    private TernaryNode root;

    // Her tilføjer vi et ord/prefix ind i træet. (Et ord som et prefix kan autocompletes til)
    public void put(String word) {
        root = put(root, word, 0);
    }

    //Her er algoritmen der tilføjer ord ind i træet (Med andre ord, det fra parseren)
    private TernaryNode put(TernaryNode node, String word, int index) {
        if (index == word.length()) return node;
        char inputChar = word.charAt(index);
        char ch = Character.toLowerCase(inputChar); //sammenligner nuværende bogstav i prefix med
                                      //det nuværende bogstav i noden

        if (node == null)
            node = new TernaryNode(ch); //gemmer lowercase i nodes

        if (ch < node.c) {
            node.left = put(node.left, word, index);
        } else if (ch > node.c) {
            node.right = put(node.right, word, index);
        } else if (index + 1 == word.length()) {
            node.finishedWord = true;  //Ordet er fundet!
            node.fullword = word;
        } else {
            node.middle = put(node.middle, word, index + 1);
        }
        return node; //Returnering så det kan bruges hvis man kalder hele denne funktion.

        //Der er måske nogle problemer med static eller ikke static, fordi intellij hele
        //tiden foreslog det, men det er nok easyfix og man kan nok bare gpt'er det.


        //prefix hvad user har typed so far
        //index hvilket bogstav af prefix vi lige pt skal matche med det næste
    }

    //Her er funktion der læser det indtastede prefix om det findes i træet
    //og findes det, så returner den node i slutningen af det.
    private TernaryNode searchPrefix(TernaryNode node, String prefix, int index) {
        if (node == null || index == prefix.length())
            return node; //Hvis der ikke findes en node i træet der matcher prefix(seneste

        char inputChar = prefix.charAt(index);
        char ch = Character.toLowerCase(inputChar); //Sammenligner nuværende character i indtastet prefix
                                        //med den nuværende nodes character.
        char nodeCharacter = Character.toLowerCase(node.c);

        if (ch < nodeCharacter) //Hvis nuværende character i prefix er mindre end characteren i noden
            return searchPrefix(node.left, prefix, index);
        else if (ch > nodeCharacter) //hvis højere
            return searchPrefix(node.right, prefix, index);
        else //hvis det er det samme bogstav igen
            if (index + 1 == prefix.length()) {
                return node;  //  stopper her hvis man søger præcis rigtigt på en adresse
            }
            return searchPrefix(node.middle, prefix, index + 1);
    }



    public List<String> autocomplete(String prefix) {
        List<String> results = new ArrayList<>(); //Listen med resultater
        TernaryNode node = searchPrefix(root, prefix, 0); /*Søg træet for at finde den
        node der matcher det sidste bogstav af prefix, Så i "bæk" er dette "k" */

        if (node != null) { //Hvis vi har et matchende prefix, hvis ikke returneres intet
            if (node.finishedWord) {
                results.add(node.fullword);
            }
            collectWords(node.middle, prefix, new StringBuilder(), results);
        }
        return results; /*Returnere alle forslag af addresser der kommer ud hvis prefix er
                        eks: "Bæk" Så returneres "Bækkelunden", "Bækkegrunden", "Bækkesunden".
                         */
    }

    private void collectWords(TernaryNode node, String prefix, StringBuilder current, List<String> out) {
        if (node == null) return; //prevents that we get a nullpointer-exception if left,
                                // middle or right child is null

        collectWords(node.left, prefix, current, out); //Checks earlier letters, branches off to the left

        current.append(node.c);
        if (node.finishedWord) {
            out.add(node.fullword); //Adds characters to the stringbuilder
        } //Example: "Bækkelunden", B --> Bæ --> Bæk --> Bækk....

        collectWords(node.middle, prefix, current, out); //we go down to the next character in a word
        current.deleteCharAt(current.length() - 1); //remove this next character, to see other branches too

        collectWords(node.right, prefix, current, out); //checks later letters, branches off to the right
    }




}