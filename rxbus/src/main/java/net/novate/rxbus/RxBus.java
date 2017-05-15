package net.novate.rxbus;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by gavin on 2017/5/15.
 */

/**
 * 事件总线
 */
public class RxBus {

    private static final String TAG = "RxBus";

    private final Subject<Object> bus;
    private final Map<Class<?>, Object> busMap;
    private final Map<Integer, Object> carMap;

    private RxBus() {
        bus = PublishSubject.create().toSerialized();
        busMap = new ConcurrentHashMap<>();
        carMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取单例
     *
     * @return 事件总线
     */
    public static RxBus get() {
        return Holder.RX_BUS;
    }

    /**
     * 发送数据
     *
     * @param object 数据
     */
    @SuppressWarnings("ConstantConditions")
    public void post(@NonNull Object object) {
        if (object == null) throw new IllegalArgumentException("object is null");
        bus.onNext(object);
    }

    /**
     * 发送粘性数据
     *
     * @param object 数据
     */
    @SuppressWarnings("ConstantConditions")
    public void postSticky(@NonNull Object object) {
        if (object == null) throw new IllegalArgumentException("object is null");
        busMap.put(object.getClass(), object);
        post(object);
    }

    /**
     * 获取数据源
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    @SuppressWarnings("ConstantConditions")
    public <T> Observable<T> toObservable(@NonNull Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type is null");
        return bus.ofType(type);
    }

    /**
     * 获取粘性数据源
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    @SuppressWarnings("ConstantConditions")
    public <T> Observable<T> toObservableSticky(@NonNull final Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type is null");
        return busMap.get(type) == null ?
                toObservable(type) :
                toObservable(type).mergeWith(Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> e) throws Exception {
                        e.onNext(type.cast(busMap.get(type)));
                        busMap.remove(type);
                    }
                }));
    }

    /**
     * 发送带编号的数据
     *
     * @param code   编号
     * @param object 数据
     */
    @SuppressWarnings("ConstantConditions")
    public void post(int code, @NonNull Object object) {
        if (object == null) throw new IllegalArgumentException("object is null");
        Car car = new Car(code, object);
        bus.onNext(car);
    }

    /**
     * 发送带编号的粘性数据
     *
     * @param code   编号
     * @param object 数据
     */
    @SuppressWarnings("ConstantConditions")
    public void postSticky(int code, @NonNull Object object) {
        if (object == null) throw new IllegalArgumentException("object is null");
        carMap.put(code, object);
        post(code, object);
    }

    /**
     * 获取带编号的数据源
     *
     * @param code 编号
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    @SuppressWarnings("ConstantConditions")
    public <T> Observable<T> toObservable(final int code, @NonNull Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type is null");
        return bus.ofType(Car.class)
                .filter(new Predicate<Car>() {
                    @Override
                    public boolean test(Car car) throws Exception {
                        return code == car.code;
                    }
                })
                .map(new Function<Car, Object>() {
                    @Override
                    public Object apply(Car car) throws Exception {
                        return car.object;
                    }
                })
                .ofType(type);
    }

    /**
     * 获取带编号的粘性数据源
     *
     * @param code 编号
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    @SuppressWarnings("ConstantConditions")
    public <T> Observable<T> toObservableSticky(final int code, @NonNull final Class<T> type) {
        if (type == null) throw new IllegalArgumentException("type is null");
        return carMap.get(code) == null ?
                toObservable(code, type) :
                toObservable(code, type).mergeWith(Observable.create(new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> e) throws Exception {
                        e.onNext(type.cast(carMap.get(code)));
                        carMap.remove(code);
                    }
                }));
    }

    /**
     * 取消绑定数据源，防止内存泄漏
     *
     * @param disposable 数据源取绑接口
     */
    public void dispose(Disposable disposable) {
        if (disposable == null) {
            return;
        }
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    /**
     * 批量取消绑定数据源，防止内存泄漏
     *
     * @param disposables 数据源取绑接口列表
     */
    public void dispose(List<Disposable> disposables) {
        if (disposables == null) {
            return;
        }
        for (Disposable disposable : disposables) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    private static class Car {
        private int code;
        private Object object;

        Car(int code, Object object) {
            this.code = code;
            this.object = object;
        }
    }

    private static class Holder {
        private static final RxBus RX_BUS = new RxBus();
    }
}