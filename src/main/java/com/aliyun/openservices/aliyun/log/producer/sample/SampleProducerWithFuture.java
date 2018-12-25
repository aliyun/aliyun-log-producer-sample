package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.*;
import com.aliyun.openservices.aliyun.log.producer.errors.ResultFailedException;
import com.aliyun.openservices.log.common.LogItem;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SampleProducerWithFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithFuture.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static void main(String[] args) throws InterruptedException {
        Producer producer = Utils.createProducer();
        int n = 100;

        // The number of logs that have finished (either successfully send, or failed)
        final AtomicLong completed = new AtomicLong(0);

        for (int i = 0; i < n; ++i) {
            ListenableFuture<Result> f;
            LogItem logItem = Utils.generateLogItem(i);
            try {
                f = producer.send(System.getenv("PROJECT"), System.getenv("LOG_STORE"), logItem);
            } catch (Exception e) {
                LOGGER.error("Failed to send log, logItem={}, e=", logItem, e);
                continue;
            }
            Futures.addCallback(f, new SampleFutureCallback(logItem, completed), EXECUTOR_SERVICE);
        }

        Utils.doSomething();

        producer.close();

        EXECUTOR_SERVICE.shutdown();
        while (!EXECUTOR_SERVICE.isTerminated()) {
            EXECUTOR_SERVICE.awaitTermination(100, TimeUnit.MILLISECONDS);
        }
        LOGGER.info("All log complete, completed={}", completed.get());
    }

    final private static class SampleFutureCallback implements FutureCallback<Result> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SampleFutureCallback.class);

        private final LogItem logItem;

        private final AtomicLong completed;

        SampleFutureCallback(LogItem logItem, AtomicLong completed) {
            this.logItem = logItem;
            this.completed = completed;
        }

        @Override
        public void onSuccess(@Nullable Result result) {
            LOGGER.info("Send log successfully, project={}, logStore={}, logItem={}", result.getProject(), result.getLogStore(), logItem.ToJsonString());
            completed.getAndIncrement();
        }

        @Override
        public void onFailure(Throwable t) {
            if (t instanceof ResultFailedException) {
                Result result = ((ResultFailedException) t).getResult();
                LOGGER.error("Failed to send log, result=" + result);
            }
            completed.getAndIncrement();
        }
    }

}
