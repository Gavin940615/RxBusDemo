package net.novate.rxbusdemo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import net.novate.rxbus.RxBus;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class OtherActivity extends BaseActivity {

    private static final String TAG = "OtherActivity";

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);

        text = (TextView) findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());

        // 获取 String 类型的粘性数据源对象
        RxBus.get().toObservableSticky(String.class)
                // 切换到主线程
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // 添加解绑持有类到解绑列表
                        addDisposeList(d);
                    }

                    @Override
                    public void onNext(String value) {
                        // 在 TextView 追加显示接收到的数据
                        text.setText(text.getText().toString() + (text.getText().toString().length() > 0 ? "\n" : "") + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        // 获取编号为 0 ，类型为 String 的粘性数据源对象
        RxBus.get().toObservableSticky(0, String.class)
                // 切换到主线程
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // 添加解绑持有类到解绑列表
                        addDisposeList(d);
                    }

                    @Override
                    public void onNext(String value) {
                        // 在 TextView 追加显示接收到的数据
                        text.setText(text.getText().toString() + (text.getText().toString().length() > 0 ? "\n" : "") + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
