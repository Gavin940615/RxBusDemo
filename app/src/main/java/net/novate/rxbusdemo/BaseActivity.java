package net.novate.rxbusdemo;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.novate.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by gavin on 2017/5/15.
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    /**
     * 解绑持有类列表
     */
    private List<Disposable> disposables = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在 onDestroy 时解绑
        RxBus.get().dispose(disposables);
    }

    /**
     * 添加解绑持有类到列表
     *
     * @param disposable 解绑持有类
     */
    public final void addDisposeList(Disposable disposable) {
        disposables.add(disposable);
    }
}
