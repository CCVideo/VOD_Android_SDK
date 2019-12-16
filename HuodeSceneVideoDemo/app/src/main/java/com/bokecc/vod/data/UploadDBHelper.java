package com.bokecc.vod.data;


import com.bokecc.vod.upload.UploadController;
import com.bokecc.vod.upload.UploadWrapper;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

public class UploadDBHelper {

    Box<UploadInfo> box;

    public UploadDBHelper(BoxStore boxStore) {
        box = boxStore.boxFor(UploadInfo.class);
    }

    public void saveUploadData() {
        ArrayList<UploadInfo> uploadInfos = new ArrayList<>();

        for (UploadWrapper wrapper : UploadController.uploadingList) {
            UploadInfo uploadInfo = wrapper.getUploadInfo();
            uploadInfos.add(uploadInfo);
        }

        for (UploadWrapper wrapper : UploadController.uploadDoneList) {
            UploadInfo uploadInfo = wrapper.getUploadInfo();
            uploadInfos.add(uploadInfo);
        }

        box.put(uploadInfos);
    }

    public List<UploadInfo> getUploadInfos() {
        List<UploadInfo> lists = box.getAll();
        return lists;
    }

    public boolean hasUploadInfo(String uploadId){
        return (findUploadInfo(uploadId) == null)? false: true;
    }

    public UploadInfo getUploadInfo(String uploadId) {
        return findUploadInfo(uploadId);
    }

    private UploadInfo findUploadInfo(String uploadId) {
        Query<UploadInfo> query = box.query().equal(UploadInfo_.uploadId, uploadId).build();
        return query.findFirst();
    }

    public void addUploadInfo(UploadInfo newUploadInfo){
        synchronized (box) {
            box.put(newUploadInfo);
        }
    }

    public void removeUploadInfo(UploadInfo newUploadInfo){
        synchronized (box) {
            box.remove(newUploadInfo);
        }
    }

    public void updateUploadInfo(UploadInfo newUploadInfo){
        synchronized (box) {
            box.put(newUploadInfo);
        }
    }
}
