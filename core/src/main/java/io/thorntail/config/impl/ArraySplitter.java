package io.thorntail.config.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bob on 2/1/18.
 */
public class ArraySplitter {

    public static List<String> split(String value) {
        List<String> list = new ArrayList<>();

        int start = 0;
        int cur = 0;

        while (start < value.length()) {
            int commaLoc = value.indexOf(',', cur);
            if (commaLoc < 0) {
                list.add(value.substring(start).replace("\\",""));
                break;
            }
            int slashLoc = value.indexOf('\\');
            if (slashLoc + 1 == commaLoc) {
                // ignore, the comma is escaped
                cur = commaLoc + 1;
            } else {
                list.add(value.substring(start, commaLoc).replace("\\", ""));
                start = commaLoc + 1;
                cur = start;
            }
        }

        return list;
    }
}
