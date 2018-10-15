package com.vrem.wifianalyzer.wifi.common;

import java.util.HashMap;

public class FrequencyTransformTools extends HashMap<String,Integer> {
    private HashMap<String,Integer> frequencyHash = new HashMap<>();
    private static FrequencyTransformTools frequencyTransformTools;

    /**
     * 单例模式
     * */
    public static FrequencyTransformTools getInstance(){
        if (frequencyTransformTools == null){
            frequencyTransformTools = new FrequencyTransformTools();
        }
        return frequencyTransformTools;
    }
    //初始化对应信道的频率
    public void Initialize(){
        frequencyHash.put("1",2412);   frequencyHash.put("2",2417);   frequencyHash.put("3",2411);
        frequencyHash.put("4",2427);   frequencyHash.put("12",2467);  frequencyHash.put("60",5300);
        frequencyHash.put("5",2432);   frequencyHash.put("13",2472);  frequencyHash.put("64",5320);
        frequencyHash.put("6",2437);   frequencyHash.put("36",5180);  frequencyHash.put("149",5745);
        frequencyHash.put("7",2442);   frequencyHash.put("40",5200);  frequencyHash.put("153",5765);
        frequencyHash.put("8",2447);   frequencyHash.put("44",5220);  frequencyHash.put("157",5785);
        frequencyHash.put("9",2452);   frequencyHash.put("48",5240);  frequencyHash.put("161",5805);
        frequencyHash.put("10",2457);  frequencyHash.put("52",5260);  frequencyHash.put("165",5825);
        frequencyHash.put("11",2462);  frequencyHash.put("56",5280);
    }

    @Override
    public Integer put(String key, Integer value) {
        return frequencyHash.put(key, value);
    }

    @Override
    public Integer get(Object key) {
        return frequencyHash.get(key);
    }
}
