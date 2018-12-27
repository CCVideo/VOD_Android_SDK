package com.bokecc.sdk.mobile.demo.downloadutil;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.bokecc.sdk.mobile.demo.model.DownloadInfo;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.play.MediaMode;

/**
 * 下载列表核心控制类
 */
public class DownloadController {
    //下载中列表
    public static ArrayList<DownloaderWrapper> downloadingList = new ArrayList<>();

    //下载完成列表
    public static ArrayList<DownloaderWrapper> downloadedList = new ArrayList<>();

    //声明授权码
    private static String verificationCode;
    //初始化，需要在程序入口执行
    public static void init(String veriCode) {
        verificationCode = veriCode;

        if (isBackDownload) {
            return;
        }

        List<DownloadInfo> list = DataSet.getDownloadInfos();

        // 清空数据
        downloadingList.clear();
        downloadedList.clear();
        observers.clear();

        for(DownloadInfo info: list) {
            DownloaderWrapper wrapper = new DownloaderWrapper(info,verificationCode);

            if (info.getStatus() == Downloader.FINISH) {
                downloadedList.add(wrapper);
            } else {
                downloadingList.add(wrapper);
            }
        }
    }

    //新增下载信息
    public static void insertDownloadInfo(String videoId,String veriCode, String title) {
        insertDownloadInfo(videoId,veriCode, title, 0, MediaUtil.DOWNLOAD_MODE);
    }

    //新增下载信息
    public static void insertDownloadInfo(String videoId,String veriCode, String title, int definition, MediaMode downloadMode) {
        DownloadInfo info = new DownloadInfo(videoId, title, Downloader.WAIT, 0, 0, new Date());
        info.setDefinition(definition);
        info.setDownloadMode(downloadMode.getMode());
        DownloaderWrapper wrapper = new DownloaderWrapper(info,veriCode);
        downloadingList.add(wrapper);
        DataSet.addDownloadInfo(info);
    }

    //删除下载中信息
    public static void deleteDownloadingInfo(int position) {
        DownloaderWrapper wrapper = downloadingList.remove(position);
        wrapper.cancel();
        DataSet.removeDownloadInfo(wrapper.getDownloadInfo().getTitle());
    }

    //删除已下载的信息
    public static void deleteDownloadedInfo(int position) {
        DownloaderWrapper wrapper = downloadedList.remove(position);
        wrapper.cancel();
        DataSet.removeDownloadInfo(wrapper.getDownloadInfo().getTitle());
    }

    //更新下载状态信息
    public static void update() {
        synchronized(downloadingList) {
            Iterator<DownloaderWrapper> iterator = downloadingList.iterator();
            int downloadCount = 0;

            //列表里有下载完成的，则需要更新列表
            while(iterator.hasNext()) {
                DownloaderWrapper wrapper = iterator.next();
                if (wrapper.getStatus() == Downloader.FINISH) {
                    iterator.remove();
                    downloadedList.add(wrapper);
                } else if (wrapper.getStatus() == Downloader.DOWNLOAD) {
                    downloadCount++;
                }
            }

            //开启新的下载
            if (downloadCount < ConfigUtil.DOWNLOADING_MAX) {
                for (DownloaderWrapper wrapper : downloadingList) {
                    if (wrapper.getStatus() == Downloader.WAIT) {
                        wrapper.start();
                        DataSet.updateDownloadInfo(wrapper.getDownloadInfo());
                        break;
                    }
                }
            }

            notifyUpdate();
        }
    }

    //处理暂停和开始下载
    public static void parseItemClick(int position) {
        synchronized(downloadingList) {
            DownloaderWrapper wrapper = downloadingList.get(position);
            if (wrapper.getStatus() == Downloader.DOWNLOAD) {
                wrapper.pause();
            } else if (wrapper.getStatus() == Downloader.PAUSE) {
                int count = getDownloadingCount();
                if (count < ConfigUtil.DOWNLOADING_MAX)     {
                    wrapper.resume();
                } else {
                    wrapper.setToWait();
                }
            }
            
            DataSet.updateDownloadInfo(wrapper.getDownloadInfo());
        }
    }

    //获取下载中的个数
    private static int getDownloadingCount() {
        int downloadCount = 0;

        for (DownloaderWrapper wrapper : downloadingList) {
            if (wrapper.getStatus() == Downloader.DOWNLOAD) {
                downloadCount++;
            }
        }

        return downloadCount;
    }

    private static boolean isBackDownload = false;

    //如果设置为true，那么说明是后台下载中，list就不能被初始化，否则会导致出现野的downloader，无法控制
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
    		for (Observer o: observers) {
    			o.update();
    		}
    	}
    }
    
    //观察者
    public static interface Observer {
    	void update();
    }
    
}
