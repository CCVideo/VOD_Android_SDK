package com.bokecc.vod.data;

import com.bokecc.vod.R;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    //配置自己的视频ID
    static String[] videoIds = new String[]{"",""};
    public static ArrayList<HuodeVideoInfo> getVideoList(){
        ArrayList<HuodeVideoInfo> datas = new ArrayList<>();
        for (int i=0;i<videoIds.length;i++){
            HuodeVideoInfo videoInfo = new HuodeVideoInfo(videoIds[i],videoIds[i]);
            datas.add(videoInfo);
        }
        return datas;
    }
}