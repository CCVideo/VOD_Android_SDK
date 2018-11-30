package com.bokecc.sdk.mobile.demo.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.bokecc.sdk.mobile.demo.R;
import com.bokecc.sdk.mobile.demo.adapter.VideoListViewAdapter;
import com.bokecc.sdk.mobile.demo.downloadutil.DownloadController;
import com.bokecc.sdk.mobile.demo.play.BtnCancelOrSureListener;
import com.bokecc.sdk.mobile.demo.play.CustomDialogInputVerifyCode;
import com.bokecc.sdk.mobile.demo.util.ConfigUtil;
import com.bokecc.sdk.mobile.demo.util.DataSet;
import com.bokecc.sdk.mobile.demo.util.MediaUtil;
import com.bokecc.sdk.mobile.demo.view.VideoListView;
import com.bokecc.sdk.mobile.download.Downloader;
import com.bokecc.sdk.mobile.download.OnProcessDefinitionListener;
import com.bokecc.sdk.mobile.exception.DreamwinException;
import com.bokecc.sdk.mobile.play.MediaMode;

/**
 * 下载列表标签页，用于展示待下载的视频ID
 *
 * @author CC视频
 */
public class DownloadFragment extends Fragment {

    final String POPUP_DIALOG_MESSAGE = "dialogMessage";
    final String DEFAULT_DEFINITION = "defaultDefinition";

    final String GET_DEFINITION_ERROR = "getDefinitionError";

    final String VERIFY_ERROR = "verifyError";

    //定义hashmap存储downloader信息
    public static HashMap<String, Downloader> downloaderHashMap = new HashMap<String, Downloader>();

    AlertDialog definitionDialog;

    private List<Pair<String, Integer>> pairs;

    private DownloadListViewAdapter downloadListViewAdapter;

    //TODO 待下载视频ID，可根据需求自定义
    public String[] downloadVideoIds = new String[]{};
    private ListView downloadListView;
    private Context context;
    private FragmentActivity activity;
    private String videoId;
    private String title;
    int[] definitionMapKeys;
    HashMap<Integer, String> hm;

    private String verificationCode;
    private boolean isDefaultDefinition = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        context = activity.getApplicationContext();

        RelativeLayout downloadRelativeLayout = new RelativeLayout(context);
        downloadRelativeLayout.setBackgroundColor(Color.WHITE);
        downloadRelativeLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        downloadListView = new ListView(context);
        downloadListView.setPadding(10, 10, 10, 10);
        downloadListView.setDivider(getResources().getDrawable(R.drawable.line));
        LayoutParams listViewLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        downloadRelativeLayout.addView(downloadListView, listViewLayout);

        // 生成动态数组，加入数据
        pairs = new ArrayList<Pair<String, Integer>>();
        for (int i = 0; i < downloadVideoIds.length; i++) {
            Pair<String, Integer> pair = new Pair<String, Integer>(downloadVideoIds[i], R.drawable.download);
            pairs.add(pair);
        }

        downloadListViewAdapter = new DownloadListViewAdapter(context, pairs);
        downloadListView.setAdapter(downloadListViewAdapter);
        downloadListView.setOnItemClickListener(onItemClickListener);

        return downloadRelativeLayout;
    }

    Downloader downloader;
    OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //点击item时，downloader初始化使用的是设置清晰度方式
            Pair<String, Integer> pair = (Pair<String, Integer>) parent.getItemAtPosition(position);
            videoId = pair.first;

            downloader = new Downloader(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode);
            downloader.setOnProcessDefinitionListener(onProcessDefinitionListener);
            downloader.setDownloadMode(MediaUtil.DOWNLOAD_MODE); //设置下载模式

            downloader.getDefinitionMap();
        }
    };

    //输入下载授权码
    public void showInputCodeDialog() {
        CustomDialogInputVerifyCode customDialogInputVerifyCode = new CustomDialogInputVerifyCode(getActivity(), new BtnCancelOrSureListener() {
            @Override
            public void btnSure(String verifyCode) {
                verificationCode = verifyCode;
                if (TextUtils.isEmpty(verifyCode)) {
                    Toast.makeText(context, "没有输入授权码", Toast.LENGTH_SHORT).show();
                } else {
                    DownloadController.init(verificationCode);
                    Toast.makeText(context, "输入的授权码是：" + verificationCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void btnCancel() {

            }
        });
        customDialogInputVerifyCode.show();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            if (message.equals(POPUP_DIALOG_MESSAGE)) {
                String[] definitionMapValues = new String[hm.size()];
                definitionMapKeys = new int[hm.size()];
                Set<Entry<Integer, String>> set = hm.entrySet();
                Iterator<Entry<Integer, String>> iterator = set.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    Entry<Integer, String> entry = iterator.next();
                    definitionMapKeys[i] = entry.getKey();
                    definitionMapValues[i] = entry.getValue();
                    i++;
                }

                Builder builder = new Builder(activity);
                builder.setTitle("选择下载清晰度");
                builder.setSingleChoiceItems(definitionMapValues, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int definition = definitionMapKeys[which];

                        title = videoId + "-" + definition;

                        title = addMediaSuffix(title);

                        if (DataSet.hasDownloadInfo(title)) {
                            Toast.makeText(context, "文件已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DownloadController.insertDownloadInfo(videoId, verificationCode, title, definition, MediaUtil.DOWNLOAD_MODE);

                        definitionDialog.dismiss();
                        Toast.makeText(context, "文件已加入下载队列", Toast.LENGTH_SHORT).show();
                    }
                });
                definitionDialog = builder.create();
                definitionDialog.show();
            } else if (message.equals(DEFAULT_DEFINITION)) {
                DownloadController.insertDownloadInfo(videoId, verificationCode, videoId);
                Toast.makeText(context, "文件已加入下载队列--默认清晰度", Toast.LENGTH_SHORT).show();
            }

            if (message.equals(GET_DEFINITION_ERROR)) {
                Toast.makeText(context, "网络异常，请重试", Toast.LENGTH_LONG).show();
            }

            if (message.equals(VERIFY_ERROR)) {
                Toast.makeText(context, "授权验证失败", Toast.LENGTH_LONG).show();
            }
            super.handleMessage(msg);
        }
    };

    private String addMediaSuffix(String title) {
        if (MediaUtil.DOWNLOAD_MODE == MediaMode.AUDIO) {
            return title + "_a";
        } else {
            return title;
        }
    }

    private OnProcessDefinitionListener onProcessDefinitionListener = new OnProcessDefinitionListener() {

        String info = null;

        @Override
        public void onProcessDefinition(HashMap<Integer, String> definitionMap) {
            hm = definitionMap;
            if (hm != null && !hm.isEmpty()) {
                Message msg = new Message();
                if (isDefaultDefinition) {
                    msg.obj = DEFAULT_DEFINITION;
                    isDefaultDefinition = false;
                } else {
                    msg.obj = POPUP_DIALOG_MESSAGE;
                }
                handler.sendMessage(msg);
            } else {
                if (hm == null) {
                    info = "获取清晰度失败";
                } else {
                    info = "清晰度节点不存在";
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onProcessException(DreamwinException exception) {
            int value = exception.getErrorCode().Value();
            Message msg = new Message();
            if (value == -13) {
                msg.obj = VERIFY_ERROR;
            } else {
                msg.obj = GET_DEFINITION_ERROR;
            }
            handler.sendMessage(msg);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class DownloadListViewAdapter extends VideoListViewAdapter {

        public DownloadListViewAdapter(Context context, List<Pair<String, Integer>> pairs) {
            super(context, pairs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Pair<String, Integer> pair = pairs.get(position);
            DownloadListView downloadListView = new DownloadListView(context, pair.first, pair.second);
            downloadListView.setTag(pair.first);
            return downloadListView;
        }

    }

    public class DownloadListView extends VideoListView {
        private Context context;

        public DownloadListView(Context context, String text, int resId) {
            super(context, text, resId);
            this.context = context;
            setImageListener();
        }

        //设置图片点击事件，点击图片的下载方式使用默认下载方式
        void setImageListener() {
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    title = videoView.getText().toString();
                    videoId = title;

                    title = addMediaSuffix(title);

                    if (DataSet.hasDownloadInfo(title)) {
                        Toast.makeText(context, "文件已存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isDefaultDefinition = true;
                    downloader = new Downloader(videoId, ConfigUtil.USERID, ConfigUtil.API_KEY, verificationCode);
                    downloader.setOnProcessDefinitionListener(onProcessDefinitionListener);
                    downloader.getDefinitionMap();
                }
            });
        }
    }
}