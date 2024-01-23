package rxhttp.wrapper.param;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Response;
import rxhttp.wrapper.BodyParamFactory;
import rxhttp.wrapper.CallFactory;
import rxhttp.wrapper.callback.ProgressCallback;
import rxhttp.wrapper.entity.Progress;
import rxhttp.wrapper.entity.ProgressT;
import rxhttp.wrapper.utils.LogUtil;

/**
 * User: ljx
 * Date: 2018/04/20
 * Time: 11:15
 */
final class ObservableCallExecute extends ObservableCall {

    private CallFactory callFactory;
    private boolean callbackUploadProgress;

    ObservableCallExecute(CallFactory callFactory) {
        this(callFactory, false);
    }

    ObservableCallExecute(CallFactory callFactory, boolean callbackUploadProgress) {
        this.callFactory = callFactory;
        this.callbackUploadProgress = callbackUploadProgress;
    }

    @Override
    public void subscribeActual(Observer<? super Progress> observer) {
        HttpDisposable d = new HttpDisposable(observer, callFactory, callbackUploadProgress);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        d.run();
    }

    private static class HttpDisposable implements Disposable, ProgressCallback {

        private boolean fusionMode;
        private volatile boolean disposed;

        private final Call call;
        private final Observer<? super Progress> downstream;

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream the Observer to wrap, not null (not verified)
         */
        HttpDisposable(Observer<? super Progress> downstream, CallFactory callFactory, boolean callbackUploadProgress) {
            if (callFactory instanceof BodyParamFactory && callbackUploadProgress) {
                ((BodyParamFactory) callFactory).getParam().setProgressCallback(this);
            }
            this.downstream = downstream;
            this.call = callFactory.newCall();
        }

        @Override
        public void onProgress(int progress, long currentSize, long totalSize) {
            if (!disposed) {
                downstream.onNext(new Progress(progress, currentSize, totalSize));
            }
        }

        public void run() {
            Response value;
            try {
                value = call.execute();
            } catch (Throwable e) {
                LogUtil.log(call.request().url().toString(), e);
                Exceptions.throwIfFatal(e);
                if (!disposed) {
                    downstream.onError(e);
                } else {
                    RxJavaPlugins.onError(e);
                }
                return;
            }
            if (!disposed) {
                downstream.onNext(new ProgressT<>(value));
            }
            if (!disposed) {
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            call.cancel();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}