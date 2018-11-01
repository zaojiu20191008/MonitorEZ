package com.easygo.tv.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easygo.monitor.R;
import com.easygo.monitor.common.EZOpenConstant;
import com.easygo.monitor.model.EZOpenCameraInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.realm.OrderedRealmCollection;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.PreviewViewHolder> {

    public int count = 2;
//    public int count = 1;

    private Context context;
    private OrderedRealmCollection<EZOpenCameraInfo> data;
    public HashMap<Integer, Subscription> subscrptions = new HashMap<>();


    public PreviewAdapter(Context context, OrderedRealmCollection<EZOpenCameraInfo> data) {
        this.context = context;
        this.data = data;

        start = System.currentTimeMillis();
    }

    @Override
    public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_preview, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PreviewViewHolder holder, final int position) {
        final EZOpenCameraInfo openCameraInfo = data.get(position);
        holder.title.setText(openCameraInfo.getCameraName());
        holder.loading.setVisibility(View.GONE);
        holder.cover.setImageResource(R.drawable.images_cache_bg2);

//        capture(holder, openCameraInfo);


//        Observable.timer(1, TimeUnit.SECONDS)
        Subscription subscribe = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if(aLong > 60) {
                            subscrptions.get(position).unsubscribe();
                            Log.i("test", "call: 停止");
                            return;
                        }

                        capture(holder, openCameraInfo);
                    }
                });

        subscrptions.put(position, subscribe);


        if (openCameraInfo.getStatus() == EZOpenConstant.DEVICE_ONLINE) {
            holder.offline_layout.setVisibility(View.GONE);
        } else {
            holder.offline_layout.setVisibility(View.VISIBLE);
        }
    }


    public boolean stop = false;

    public void capture(final PreviewViewHolder holder, final EZOpenCameraInfo cameraInfo) {
        final String deviceSerial = cameraInfo.getDeviceSerial();
        final int cameraNo = cameraInfo.getCameraNo();


        if (stop) {
            Log.i("test", "captureCamera: captureCount  -->  停止stop, " + (System.currentTimeMillis()-start));
            subscrptions.get(holder.getAdapterPosition()).unsubscribe();
            return;
        }

        if(System.currentTimeMillis()-start > 60000) {
            Log.i("test", "captureCamera: captureCount  -->  停止, " + (System.currentTimeMillis()-start));
            return;
        }
        captureCount++;
        Log.i("test", "captureCamera: 捕获摄像头画面 - camera: " + cameraInfo.getCameraName() +
                ", position: " + holder.getAdapterPosition() +
                ", captureCount --> " + captureCount);

        Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                subscriber.onStart();

                String url = null;
                try {
                    url = EZOpenSDK.captureCamera(deviceSerial, cameraNo);
//                    url = EZOpenSDK.captureCamera("182365469", 1);
                } catch (BaseException e) {
                    e.printStackTrace();
                    Log.i("test", "call: msg --> " + e.getMessage());
                    if(e.getMessage().startsWith("达到")) {
                        stop = true;
                    }
                }

                Bitmap myBitmap = null;
                long s = System.currentTimeMillis();
                try {
                    myBitmap = Glide.with(context)
                            .load(url)
                            .asBitmap()
                            .into(640,360)
                            .get();
//                    Log.i("time", "call: 解析位图 time --> " + (System.currentTimeMillis() - s));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                subscriber.onNext(myBitmap);
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
//                        holder.loading.setVisibility(View.VISIBLE);
                    }
                })
                .subscribe(new Observer<Bitmap>() {


            @Override
            public void onCompleted() {
//                Observable.timer(1, TimeUnit.SECONDS)
//                Observable.timer(0, TimeUnit.SECONDS)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1<Long>() {
//                    @Override
//                    public void call(Long aLong) {
//                        capture(holder, cameraInfo);
//                    }
//                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Bitmap bitmap) {
//                Log.i("test", "onNext: url --> " + url);
                if(bitmap != null) {
                    holder.cover.setImageBitmap(bitmap);
                } else {
                    Log.i("test", "onNext: captureCount --> " + captureCount);
                }
//                holder.loading.setVisibility(View.GONE);
            }
        });

    }

    private int captureCount = 0;
    private long start = 0;

    @Override
    public int getItemCount() {
        return count;
    }

    public class PreviewViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView cover;
        TextView loading;
        RelativeLayout offline_layout;

        PreviewViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.camera_title);
            cover = (ImageView) itemView.findViewById(R.id.camera_cover_img);
            offline_layout = (RelativeLayout) itemView.findViewById(R.id.offline_layout);
            loading = (TextView) itemView.findViewById(R.id.text_loading);
        }

//        public void startTimer(final int position) {
//
//            new CountDownTimer(1, 1) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                }
//
//                @Override
//                public void onFinish() {
//                    captureCamera(PreviewViewHolder.this, data.get(position), position);
//                }
//            }.start();
//        }
    }


}
