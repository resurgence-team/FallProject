package com.jennyni.fallproject.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.PullToRefreshView;
import com.jennyni.fallproject.Bean.UserLoginBean;
import com.jennyni.fallproject.Bean.UserUpdateBean;
import com.jennyni.fallproject.FallProjectApplication;
import com.jennyni.fallproject.R;
import com.jennyni.fallproject.activity.AddDeviceUserInfoActivity;
import com.jennyni.fallproject.adapter.DeviceListAdapter;
import com.jennyni.fallproject.utils.Constant;
import com.jennyni.fallproject.utils.JsonParse;
import com.jennyni.fallproject.utils.UtilsHelper;
import com.jennyni.fallproject.view.WrapRecyclerView;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**..............长按解绑
 * HomeFragment 用户列表 显示添加设备功能，解绑设备，点击列表项进入设备定位
 */

public class HomeFragment extends Fragment {    //implements DeviceListAdapter.LongClickListener
    public static final String TAG ="HomeFragment";
    private RelativeLayout rl_title_bar;
    private TextView tv_main_title, tv_switch;
    private PullToRefreshView mPullToRefreshView;
    private WrapRecyclerView recycleView;
    public static final int REFRESH_DELAY = 1000;
    public static final int MSG_DevUser_OK = 1; //加载设备，获取数据
//    public static final int MSG_DevUser_FAIL = 2;
    public static final int MSG_DelDev_OK= 2; //解绑设备，获取数据
    private DeviceListAdapter adapter;

    public HomeFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sendrequest_initData();         //加载设备用户信息列表数据
        View view = initView(inflater, container);
        return view;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //标题栏
        rl_title_bar = (RelativeLayout) view.findViewById(R.id.title_bar);
        rl_title_bar.setVisibility(View.VISIBLE);
        tv_main_title = (TextView) view.findViewById(R.id.tv_main_title);
        tv_main_title.setText("用户列表");
        rl_title_bar.setBackgroundColor(getResources().getColor(R.color.rdTextColorPress));
        tv_switch = view.findViewById(R.id.tv_save);
        tv_switch.setVisibility(View.VISIBLE);
        tv_switch.setText("添加设备");
        tv_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到添加设备界面
                Intent intent = new Intent(getActivity(), AddDeviceUserInfoActivity.class);
                startActivity(intent);
            }
        });
        //列表框
        recycleView = (WrapRecyclerView) view.findViewById(R.id.recycler_view);
        recycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeviceListAdapter(getActivity());
        recycleView.setAdapter(adapter);
//        //
//        adapter.setOnLongClickLisenter(new adapter.OnLongClickLisenter() {
//            @Override
//            public void onLongClick(int position) {
//                Toast.makeText(getActivity(), "删除成功" + position, Toast.LENGTH_SHORT).show();
//                adapter.removeItem(position);
//            }
//        });
        //下拉刷新
        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.
                pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.
                OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mPullToRefreshView.setRefreshing(false);
                        sendrequest_initData();      //加载设备用户信息列表数据
                    }
                }, REFRESH_DELAY);
            }
        });

        return view;
    }

    /**
     * 事件捕获
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DevUser_OK:
                    if (msg.obj != null) {
                        String result = (String) msg.obj;
                        Log.e(TAG,"handleMessage"+ result);
                        //使用Gson解析数据
                        List<UserUpdateBean.ResultBean> list = JsonParse.getInstance().getuserUpdateInfo(result);
                        adapter.setData(list);
                    }
                    break;
                case MSG_DelDev_OK:         //解绑设备

                    break;
            }
        }
    };


    /**
     * 加载设备用户信息列表数据
     */
    private void sendrequest_initData() {
//        sharedPreferences = getContext().getSharedPreferences("loginInfo", getActivity().MODE_PRIVATE);
//        String account = sharedPreferences.getString("account", "");
        String account = UtilsHelper.readLoginUserName(getActivity());
        String url = Constant.BASE_WEBSITE + Constant.REQUEST_UPDATE_USER_URL+"/account/"+account;
        Log.e(TAG,"initData: "+url );
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        //开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"MSG_DevUser_FAIL"+"请求失败：" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.e(TAG,"MSG_DevUser_OK"+"请求失败：" + res);
                Message msg = new Message();
                msg.what = MSG_DevUser_OK;
                msg.obj = res;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 请求网络，解绑设备
     */
//    private void sendRequest_delDevice() {
//        //String url6 = "http://www.phyth.cn/index/fall/delDevice/account/" + account + "/cardid/" + cardid;
//        String account = UtilsHelper.readLoginUserName(getActivity());
//        String cardid = ;
//        String url = Constant.BASE_WEBSITE + Constant.REQUEST_DEL_DEVICE_URL +"/account/" + account + "/cardid/" + cardid;
//        OkHttpClient okHttpClient = new OkHttpClient();
//        final Request request = new Request.Builder().url(url).build();
//        Call call = okHttpClient.newCall(request);
//        //开启异步访问网络
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                //联网失败
//                Log.e("MSG_FAIL", "请求失败：" + e.getMessage());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                Log.e("MSG_OK", "请求成功：" + response);
//                String str = JsonParse.getInstance().getDelDeviceInfo(response.body().string());
//                Log.e("MSG_OK",str);
//                Message message = new Message();
//                message.what = MSG_DelDev_OK;
//                message.obj = str;
//                handler.sendMessage(message);
//            }
//        });
//
//    }

}
