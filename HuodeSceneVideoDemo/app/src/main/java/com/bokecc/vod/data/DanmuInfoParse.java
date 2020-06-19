package com.bokecc.vod.data;

import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

public class DanmuInfoParse extends BaseDanmakuParser {
    @Override
    protected IDanmakus parse() {
        return new Danmakus();
    }
}
