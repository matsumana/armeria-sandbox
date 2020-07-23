package info.matsumana.armeria.util;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.reactivex.Single;

public final class RxInteropUtil {

    private RxInteropUtil() {}

    public static <T> Single<T> fromListenableFutureToSingle(ListenableFuture<? extends T> future) {
        return Single.defer(() -> Single.create(e -> Futures.addCallback(
                future,
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        e.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        e.onError(t);
                    }
                },
                MoreExecutors.directExecutor())));
    }
}
