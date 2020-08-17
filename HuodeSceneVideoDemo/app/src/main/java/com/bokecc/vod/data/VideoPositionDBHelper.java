package com.bokecc.vod.data;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

public class VideoPositionDBHelper {

    Box<VideoPosition> box;

    public VideoPositionDBHelper(BoxStore boxStore) {
        box = boxStore.boxFor(VideoPosition.class);
    }

    public VideoPosition getVideoPosition(String videoId) {
        Query<VideoPosition> query = box.query().equal(VideoPosition_.videoId, videoId).build();
        VideoPosition videoPosition = query.findFirst();
        return videoPosition;
    }

    public void updateVideoPosition(VideoPosition videoPosition) {
        box.put(videoPosition);
    }
}
