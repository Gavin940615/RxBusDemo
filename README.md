# RxBus

RxBus是通过RxJava实现的EventBus，是一个事件总线，可以实现事件的调度。使用RxBus需要对RxJava有所了解。

> RxJava最核心的两个东西是Observable（被观察者）和Observer（观察者）。Observable发出一系列事件，Observer处理这些事件。

## 实现原理

#### 添加依赖

- RxBus基于RxJava 2.0实现
- Android上使用最新版的RxAndroid:2.0.1

```groovy
compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
```

#### 构造方法：单例模式

```java
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

    private static class Car {
        private int code;
        private Object object;
    }

    private static class Holder {
        private static final RxBus RX_BUS = new RxBus();
    }
}
```

#### 基本功能：发送数据

- 最基本的发送数据功能
- post():  发送数据，需先订阅，再发送数据，订阅者才能接收到数据
- toObservable():  获取Observable对象，使用RxJava语法订阅数据源

```java
    /**
     * 发送数据
     *
     * @param object 数据
     */
    public void post(@NonNull Object object) {
        bus.onNext(object);
    }

    /**
     * 获取数据源
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    public <T> Observable<T> toObservable(@NonNull Class<T> type) {
        return bus.ofType(type);
    }
```

#### 拓展功能：发送粘性数据

- 支持发送粘性数据，即：先发送数据，再订阅，订阅者可以收到订阅前的最近一条数据
- postSticky():  发送粘性数据，发送数据前先将数据按照<类型，数据>的格式存入Map，同种类型的数据会被最新的数据覆盖
- toObservableSticky():  获取粘性数据源，从Map中按照类型查找数据，如果存在则将数据合并发送出去，然后移除这条数据
- 注意：基于内存泄漏的考虑，目前数据一旦被发送出去就会从Map中移除，所以发送一条粘性数据时，当它首次被订阅时，可以收到数据，而再此订阅时，不会收到之前的数据，后续版本可能会改变此机制

```java
    /**
     * 发送粘性数据
     *
     * @param object 数据
     */
    public void postSticky(@NonNull Object object) {
        busMap.put(object.getClass(), object);
        post(object);
    }

    /**
     * 获取粘性数据源
     *
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    public <T> Observable<T> toObservableSticky(@NonNull final Class<T> type) {
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
```

拓展功能：发送带编号的数据

```java
    /**
     * 发送带编号的数据
     *
     * @param code   编号
     * @param object 数据
     */
    public void post(int code, @NonNull Object object) {
        Car car = new Car();
        car.code = code;
        car.object = object;
        bus.onNext(car);
    }

    /**
     * 获取带编号的数据源
     *
     * @param code 编号
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    public <T> Observable<T> toObservable(final int code, @NonNull Class<T> type) {
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
```

拓展功能：发送带编号的粘性数据

```java
    /**
     * 发送带编号的粘性数据
     *
     * @param code   编号
     * @param object 数据
     */
    public void postSticky(int code, @NonNull Object object) {
        carMap.put(code, object);
        post(code, object);
    }

    /**
     * 获取带编号的粘性数据源
     *
     * @param code 编号
     * @param type 数据类型
     * @param <T>  数据类型
     * @return 数据源
     */
    public <T> Observable<T> toObservableSticky(final int code, @NonNull final Class<T> type) {
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
```

基本功能：取消绑定

```java
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
```