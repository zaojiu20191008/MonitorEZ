package com.easygo.tv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constant {

    public static class CMD {
        public static final String REMOVE = " 移    除 ";
        public static final String ZOOM_IN = " 全    屏 ";
        public static final String REPLAY = " 重    载 ";

        static String[] cmds = new String[] {
                REMOVE,
                ZOOM_IN,
                REPLAY,
        };
        public static List<String> data = new ArrayList<>();

        static {
            Collections.addAll(data, cmds);
        }
    }

    public static class Alarm {
        /**
         * 在播放此时间后开始获取告警信息
         */
        public static final long GET_ALARM_INFO_AFTER_TIME = 120 * 1000;
    }
}
