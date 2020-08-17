package com.bokecc.vod.upload;

import android.util.Log;

import com.bokecc.sdk.mobile.upload.Uploader;
import com.bokecc.vod.ConfigUtil;
import com.bokecc.vod.data.DataSet;
import com.bokecc.vod.data.UploadInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 上传控制类
 */
public class UploadController {
    //上传中列表
    public static ArrayList<UploadWrapper> uploadingList = new ArrayList<>();

    //上传完成列表
    public static ArrayList<UploadWrapper> uploadDoneList = new ArrayList<>();


    //初始化，需要在程序入口执行
    public static void init() {
        if (isBackDownload) {
            return;
        }

        List<UploadInfo> list = DataSet.getUploadInfos();

        // 清空数据
        uploadingList.clear();
        uploadDoneList.clear();
        observers.clear();

        for (UploadInfo info : list) {
            UploadWrapper wrapper = new UploadWrapper(info);

            if (info.getStatus() == Uploader.FINISH) {
                uploadDoneList.add(wrapper);
            } else {
                uploadingList.add(wrapper);
            }
        }
    }

    //新增上传信息
    public static void insertUploadInfo(UploadInfo newUploadInfo) {
        newUploadInfo.setStatus(Uploader.WAIT);
        newUploadInfo.setStart(0);
        newUploadInfo.setEnd(0);
        UploadWrapper wrapper = new UploadWrapper(newUploadInfo);
        uploadingList.add(wrapper);
        DataSet.addUploadInfo(newUploadInfo);
    }

    //删除上传中信息
    public static void deleteUploadingInfo(int position) {
        UploadWrapper wrapper = uploadingList.remove(position);
        wrapper.cancel();
        DataSet.removeUploadInfo(wrapper.getUploadInfo());
    }

    //删除已上传的信息
    public static void deleteUploadDoneInfo(int position) {
        UploadWrapper wrapper = uploadDoneList.remove(position);
        DataSet.removeUploadInfo(wrapper.getUploadInfo());
    }

    //更新下载状态信息
    public static void update() {
        synchronized (uploadingList) {
            Iterator<UploadWrapper> iterator = uploadingList.iterator();
            int uploadCount = 0;

            //列表里有下载完成的，则需要更新列表
            while (iterator.hasNext()) {
                UploadWrapper wrapper = iterator.next();
                if (wrapper.getStatus() == Uploader.FINISH) {
                    iterator.remove();
                    uploadDoneList.add(wrapper);
                } else if (wrapper.getStatus() == Uploader.UPLOAD) {
                    uploadCount++;
                }
            }

            //开启新的上传
            if (uploadCount < ConfigUtil.UPLOADING_MAX) {
                for (UploadWrapper wrapper : uploadingList) {
                    if (wrapper.getStatus() == Uploader.WAIT) {
                        wrapper.start();
                        DataSet.updateUploadInfo(wrapper.getUploadInfo());
                        break;
                    }
                }
            }

            notifyUpdate();
        }
    }

    //连接网络后恢复上传
    public static void resumeUpLoad() {
        synchronized (uploadingList) {
            Iterator<UploadWrapper> iterator = uploadingList.iterator();
            int uploadCount = 0;
            int resumeCount = 0;

            while (iterator.hasNext()) {
                UploadWrapper wrapper = iterator.next();
                if (wrapper.getStatus() == Uploader.FINISH) {
                    iterator.remove();
                    uploadDoneList.add(wrapper);
                } else if (wrapper.getStatus() == Uploader.UPLOAD) {
                    uploadCount++;
                }
            }

            if (uploadCount < ConfigUtil.UPLOADING_MAX) {
                for (UploadWrapper wrapper : uploadingList) {
                    if (wrapper.getStatus() == Uploader.PAUSE) {
                        if (resumeCount < ConfigUtil.UPLOADING_MAX) {
                            wrapper.start();
                            DataSet.updateUploadInfo(wrapper.getUploadInfo());
                            resumeCount++;
                        } else {
                            wrapper.setToWait();
                            DataSet.updateUploadInfo(wrapper.getUploadInfo());
                        }

                    }
                }
            }

            notifyUpdate();
        }
    }

    //处理暂停和开始上传
    public static void parseItemClick(int position) {
        synchronized (uploadingList) {
            UploadWrapper wrapper = uploadingList.get(position);
            if (wrapper.getStatus() == Uploader.UPLOAD) {
                wrapper.pause();
            } else if (wrapper.getStatus() == Uploader.PAUSE) {
                int count = getUploadingCount();
                if (count < ConfigUtil.UPLOADING_MAX) {
                    wrapper.start();
                } else {
                    wrapper.setToWait();
                }
            }

            DataSet.updateUploadInfo(wrapper.getUploadInfo());
        }
    }

    //开启全部上传
    public static void startAllUpload(int position) {
        synchronized (uploadingList) {
            UploadWrapper wrapper = uploadingList.get(position);
            if (wrapper.getStatus() == Uploader.PAUSE) {
                int count = getUploadingCount();
                if (count < ConfigUtil.UPLOADING_MAX) {
                    wrapper.start();
                } else {
                    wrapper.setToWait();
                }
            }

            DataSet.updateUploadInfo(wrapper.getUploadInfo());
        }
    }

    //暂停全部上传
    public static void pauseAllUpload(int position) {
        synchronized (uploadingList) {
            UploadWrapper wrapper = uploadingList.get(position);
            if (wrapper.getStatus() == Uploader.UPLOAD || wrapper.getStatus() == Uploader.WAIT) {
                wrapper.pause();
            }
            DataSet.updateUploadInfo(wrapper.getUploadInfo());
        }
    }

    //获取上传中的个数
    public static int getUploadingCount() {
        int downloadCount = 0;

        for (UploadWrapper wrapper : uploadingList) {
            if (wrapper.getStatus() == Uploader.UPLOAD) {
                downloadCount++;
            }
        }

        return downloadCount;
    }

    //获取已暂停或等待中的个数
    public static int getPauseAndWaitCount() {
        int downloadCount = 0;

        for (UploadWrapper wrapper : uploadingList) {
            if (wrapper.getStatus() == Uploader.PAUSE || wrapper.getStatus() == Uploader.WAIT) {
                downloadCount++;
            }
        }

        return downloadCount;
    }

    private static boolean isBackDownload = false;

    //如果设置为true，那么说明是后台上传中，list就不能被初始化，否则会导致出现野的uploader，无法控制
    public static void setBackDownload(boolean isBack) {
        isBackDownload = isBack;
    }

    public static List<Observer> observers = new ArrayList<>();

    public static void attach(Observer o) {
        observers.add(o);
    }

    public static void detach(Observer o) {
        observers.remove(o);
    }

    public static void notifyUpdate() {
        if (observers.size() > 0) {
            for (Observer o : observers) {
                o.update();
            }
        }
    }

    //观察者
    public static interface Observer {
        void update();
    }

}
