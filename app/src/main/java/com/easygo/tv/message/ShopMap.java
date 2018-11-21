package com.easygo.tv.message;

import java.util.HashMap;

public class ShopMap {

    public static HashMap<Integer, String> sShop = new HashMap<>();
    public static String[] sName;
    public static HashMap<String, String[]> sCamera;

    /**
     * 没有的门店
     * 悦万城
     * 咖啡厅二楼技术部
     * 时代地产八楼健身房   不确定
     * 三元里洪泰大厦19楼冰箱
     * 广州国际媒体港
     * 办公室门口
     */

    static {
        sShop.put(413, "182364991");//美的海岸花园海星居1
        sShop.put(414, "182365528");//美的新海岸花园店1
        sShop.put(433, "182364305");//广大商业中心店2
        sShop.put(323, "201104588");//新城花园(华润联合)1

        sShop.put(412, "C16958382");//美的海岸花园大学生公寓店2
        sShop.put(389, "203995603");//中山星汇云锦店2
        sShop.put(319, "201104586");//力迅上筑
        sShop.put(39, "201104673");//中山坦洲海伦印象店

        sShop.put(292, "201104636");//依绿山庄1
        sShop.put(276, "201104695");//怡翠宏璟
        sShop.put(310, "201104852");//中欧中心1
        sShop.put(332, "201104594");//磨碟沙花苑1

        sShop.put(65, "201104597");//南沙海景城1
        sShop.put(324, "201104624");//城市花园(华润联合)
        sShop.put(27, "201104630");//海伦堡华景新城店1
        sShop.put(334, "201104726");//美的高尔夫一店2

        sShop.put(41, "201104766");//东山雅筑商务中心
        sShop.put(336, "201104853");//美的高尔夫二店
        sShop.put(279, "201104985");//518创意园店
        sShop.put(335, "201105013");//美的君兰江山

        sShop.put(273, "201104891");//时代依云小镇店1
        sShop.put(24, "201104993");//时代南湾店1
        sShop.put(68, "201104914");//怡翠花园1
        sShop.put(314, "201105004");//岭南天地店1

        sShop.put(57, "201104994");//时代廊桥增城店
        sShop.put(318, "201105092");//星河湾半岛
        sShop.put(298, "203894222");//时代地产中心8楼店 ， 时代地产八楼健身房
        sShop.put(420, "203893493");//时代地产中心15楼店

        sShop.put(449, "C16407971");//289数字半岛店1    加密
        sShop.put(419, "203893578");//时代地产中心17楼店
        sShop.put(408, "C16408236");//移动全球通大厦店1
        sShop.put(361, "C16407967");//星航华府

        sShop.put(343, "C16407792");//白云机场店1
        sShop.put(299, "203619784");//广州花城汇北区店
        sShop.put(363, "C16408095");//阳光酒店1
        sShop.put(392, "C16408383");//海逸锦绣蓝湾店2

        sShop.put(360, "C33369758");//麒邻公寓2
        sShop.put(407, "C16408133");//美的御海东郡店2
        sShop.put(341, "C16958301");//怡翠馨园1
        sShop.put(272, "C16408259");//南沙奥园1  不在线
////        sShop.put(272, "C16408262");//南沙奥园2  不在线


        /**
         * key为 照到门方向的摄像头序列号，value为 该门店内其他摄像头序列号
         */
        sCamera = new HashMap<>();
        sCamera.put("203995603", new String[]{"182365547"});//中山星汇云锦店
        sCamera.put("182364305", new String[]{"C16408131"});//广大商业中心店
        sCamera.put("C16407792", new String[]{"C16408308", "C16407969"});//白云机场
        sCamera.put("C16407971", new String[]{"203994414"});//289数字半岛
        sCamera.put("C16407967", new String[]{"C16410791"});//星航华府
        sCamera.put("C16408095", new String[]{"C16408179"});//阳光酒店
        sCamera.put("C16408133", new String[]{"C16408223"});//美的御海东郡店
        sCamera.put("201104852", new String[]{"201104988"});//中欧中心
        sCamera.put("C16408383", new String[]{"C16407994"});//海逸锦绣蓝湾店
        sCamera.put("C33369758", new String[]{"C16408228"});//麒邻公寓
        sCamera.put("C16958301", new String[]{"182364245"});//怡翠馨园

    }
}
