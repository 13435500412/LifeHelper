package com.ns.yc.lifehelper.ui.other.myNews.wxNews.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.ns.yc.lifehelper.R;
import com.ns.yc.lifehelper.api.ConstantALiYunApi;
import com.ns.yc.lifehelper.base.BaseFragment;
import com.ns.yc.lifehelper.ui.main.view.activity.WebViewActivity;
import com.ns.yc.lifehelper.ui.other.myNews.wxNews.WxNewsActivity;
import com.ns.yc.lifehelper.ui.other.myNews.wxNews.adapter.WxNewsAdapter;
import com.ns.yc.lifehelper.ui.other.myNews.wxNews.bean.WxNewsDetailBean;
import com.ns.yc.lifehelper.ui.other.myNews.wxNews.model.WxNewsModel;
import com.ns.yc.lifehelper.ui.weight.itemLine.RecycleViewItemLine;

import butterknife.Bind;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：杨充
 * 版    本：1.0
 * 创建日期：2017/8/31
 * 描    述：微信新闻页面
 * 修订历史：
 * ================================================
 */
public class WxNewsFragment extends BaseFragment {

    @Bind(R.id.recyclerView)
    EasyRecyclerView recyclerView;
    private static String TYPE = "wx";
    private String mType;
    private WxNewsActivity activity;
    private int num = 11;
    private int start = 1;
    private WxNewsAdapter adapter;

    public static WxNewsFragment newInstance(String param1) {
        WxNewsFragment fragment = new WxNewsFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (WxNewsActivity) context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(TYPE);
        }
        if(mType==null || mType.length()==0){
            mType = "1";
        }
    }


    @Override
    public int getContentView() {
        return R.layout.base_easy_recycle;
    }

    @Override
    public void initView() {
        initRecycleView();
    }

    @Override
    public void initListener() {
        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position>-1 && adapter.getAllData().size()>position){
                    Intent intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra("name",adapter.getAllData().get(position).getWeixinname());
                    intent.putExtra("url",adapter.getAllData().get(position).getUrl());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void initData() {
        recyclerView.showProgress();
        getWxNews(mType,num,start);
    }


    private void initRecycleView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new WxNewsAdapter(activity);
        final RecycleViewItemLine line = new RecycleViewItemLine(activity, LinearLayout.HORIZONTAL,
                SizeUtils.dp2px(5), Color.parseColor("#f5f5f7"));
        recyclerView.addItemDecoration(line);
        recyclerView.setAdapter(adapter);

        //加载更多
        adapter.setMore(R.layout.view_recycle_more, new RecyclerArrayAdapter.OnMoreListener() {
            @Override
            public void onMoreShow() {
                if(NetworkUtils.isConnected()){
                    start = num + 1;
                    num = start + 10;
                    getWxNews(mType,num,start);
                } else {
                    adapter.pauseMore();
                    ToastUtils.showShortSafe("没有网络");
                }
            }

            @Override
            public void onMoreClick() {

            }
        });

        //设置没有数据
        adapter.setNoMore(R.layout.view_recycle_no_more, new RecyclerArrayAdapter.OnNoMoreListener() {
            @Override
            public void onNoMoreShow() {
                if (NetworkUtils.isConnected()) {
                    adapter.resumeMore();
                } else {
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNoMoreClick() {
                if (NetworkUtils.isConnected()) {
                    adapter.resumeMore();
                } else {
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //设置错误
        adapter.setError(R.layout.view_recycle_error, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                adapter.resumeMore();
            }

            @Override
            public void onErrorClick() {
                adapter.resumeMore();
            }
        });

        //刷新
        recyclerView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkUtils.isConnected()) {
                    num = 11;
                    start = 1;
                    getWxNews(mType,num,start);
                } else {
                    recyclerView.setRefreshing(false);
                    Toast.makeText(activity, "网络不可用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void getWxNews(String mType, int num, final int start) {
        WxNewsModel model = WxNewsModel.getInstance();
        model.getWxNewsDetail(ConstantALiYunApi.Key,mType,String.valueOf(num),String.valueOf(start))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WxNewsDetailBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(WxNewsDetailBean wxNewsDetailBean) {
                        if(adapter==null){
                            adapter = new WxNewsAdapter(activity);
                        }

                        if(wxNewsDetailBean!=null){
                            if(start==1){
                                if(wxNewsDetailBean.getResult()!=null && wxNewsDetailBean.getResult().getList()!=null && wxNewsDetailBean.getResult().getList().size()>0){
                                    adapter.clear();
                                    adapter.addAll(wxNewsDetailBean.getResult().getList());
                                    adapter.notifyDataSetChanged();
                                    recyclerView.showRecycler();
                                } else {
                                    recyclerView.showEmpty();
                                    recyclerView.setEmptyView(R.layout.view_custom_empty_data);
                                }
                            } else {
                                if(wxNewsDetailBean.getResult()!=null && wxNewsDetailBean.getResult().getList()!=null && wxNewsDetailBean.getResult().getList().size()>0){
                                    adapter.addAll(wxNewsDetailBean.getResult().getList());
                                    adapter.notifyDataSetChanged();
                                    recyclerView.showRecycler();
                                } else {
                                    adapter.stopMore();
                                }
                            }
                        }
                    }
                });
    }
}
