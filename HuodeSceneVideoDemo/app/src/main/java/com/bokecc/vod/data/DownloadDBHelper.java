package com.bokecc.vod.data;


import com.bokecc.vod.download.DownloadController;
import com.bokecc.vod.download.DownloadWrapper;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

public class DownloadDBHelper {

    Box<DownloadInfo> box;

    public DownloadDBHelper(BoxStore boxStore) {
        box = boxStore.boxFor(DownloadInfo.class);
    }

    public void saveDownloadData() {
        ArrayList<DownloadInfo> downloadInfos = new ArrayList<>();

        for (DownloadWrapper wrapper : DownloadController.downloadingList) {
            DownloadInfo downloadInfo = wrapper.getDownloadInfo();
            downloadInfos.add(downloadInfo);
        }

        for (DownloadWrapper wrapper : DownloadController.downloadedList) {
            DownloadInfo downloadInfo = wrapper.getDownloadInfo();
            downloadInfos.add(downloadInfo);
        }

        box.put(downloadInfos);
    }

    public List<DownloadInfo> getDownloadInfos() {
        List<DownloadInfo> lists = box.getAll();
        return lists;
    }

    public boolean hasDownloadInfo(String title){
        return (findDownloadInfo(title) == null)? false: true;
    }

    public DownloadInfo getDownloadInfo(String title) {
        return findDownloadInfo(title);
    }

    private DownloadInfo findDownloadInfo(String title) {
        Query<DownloadInfo> query = box.query().equal(DownloadInfo_.title, title).build();
        return query.findFirst();
    }

    public void addDownloadInfo(DownloadInfo downloadInfo){
        synchronized (box) {
            box.put(downloadInfo);
        }
    }

    public void removeDownloadInfo(DownloadInfo downloadInfo){
        synchronized (box) {
            box.remove(downloadInfo);
        }
    }

    public void updateDownloadInfo(DownloadInfo downloadInfo){
        synchronized (box) {
            box.put(downloadInfo);
        }
    }
}
