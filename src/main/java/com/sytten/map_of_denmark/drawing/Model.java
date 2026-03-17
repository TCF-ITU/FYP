package com.sytten.map_of_denmark.drawing;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Model {
    List<String> lines = new ArrayList<String>();
    public Model(String path) {
        File f = new File(path);
        try {
            try (Scanner s = new Scanner(f)) {
                while (s.hasNext()) {
                    lines.add(s.nextLine());
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void add(double lastX, double lastY, double x, double y) {
        String s = "LINE".concat(" ").concat(lastX+"").concat(" ").concat(lastY+"").concat(" ").concat(x+"").concat(" ").concat(y+"");
        lines.add(s);
    }
}
