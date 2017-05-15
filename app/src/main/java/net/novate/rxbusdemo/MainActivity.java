package net.novate.rxbusdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.novate.rxbus.RxBus;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private TextView text;
    private Button postObject, postStickyObject, postCodeObject, postStickyCodeObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        text.setMovementMethod(new ScrollingMovementMethod());

        postObject = (Button) findViewById(R.id.post_object);
        postStickyObject = (Button) findViewById(R.id.poststicky_object);
        postCodeObject = (Button) findViewById(R.id.post_code_object);
        postStickyCodeObject = (Button) findViewById(R.id.poststicky_code_object);

        postObject.setOnClickListener(this);
        postStickyObject.setOnClickListener(this);
        postCodeObject.setOnClickListener(this);
        postStickyCodeObject.setOnClickListener(this);

        // 获取 String 类型的数据源对象
        RxBus.get().toObservable(String.class)
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

        // 获取编号为 0 ，类型为 String 的数据源对象
        RxBus.get().toObservable(0, String.class)
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_object:
                // 发送 Post Object!
                RxBus.get().post("Post Object!");
                RxBus.get().post(null);
                break;
            case R.id.poststicky_object:
                // 发送粘性 PostSticky Object! 然后启动 OtherActivity
                RxBus.get().postSticky("PostSticky Object!");
                startActivity(new Intent(this, OtherActivity.class));
                break;
            case R.id.post_code_object:
                // 依次发送带编号的数据
                RxBus.get().post(1, "Post Code Object! - 1");
                RxBus.get().post(0, "Post Code Object! - 2");
                RxBus.get().post(2, "Post Code Object! - 3");
                RxBus.get().post(0, "Post Code Object! - 4");
                RxBus.get().post(3, "Post Code Object! - 5");
                break;
            case R.id.poststicky_code_object:
                // 依次发送带编号粘性数据 然后启动 OtherActivity
                RxBus.get().postSticky(1, "PostSticky Code Object! - 1");
                RxBus.get().postSticky(0, "PostSticky Code Object! - 2");
                RxBus.get().postSticky(2, "PostSticky Code Object! - 3");
                RxBus.get().postSticky(0, "PostSticky Code Object! - 4");
                RxBus.get().postSticky(3, "PostSticky Code Object! - 5");
                startActivity(new Intent(this, OtherActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
