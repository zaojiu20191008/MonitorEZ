package com.easygo.tv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constant {

    public static class CMD {
        public static final String REMOVE = " 移    除 ";
        public static final String ZOOM_IN = " 全    屏 ";

        static String[] cmds = new String[] {
                REMOVE,
                ZOOM_IN,
        };
        public static List<String> data = new ArrayList<>();

        static {
            Collections.addAll(data, cmds);
        }
    }
}
