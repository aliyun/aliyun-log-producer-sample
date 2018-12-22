package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.log.common.LogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SampleProducerWithCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithCallback.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
        final Producer producer = Utils.createProducer();

        int nTask = 100;

        // The monotonically increasing sequence number we will put in the data of each log
        final AtomicLong sequenceNumber = new AtomicLong(0);

        // The number of logs that have finished (either successfully send, or failed)
        final AtomicLong completed = new AtomicLong(0);

        final CountDownLatch latch = new CountDownLatch(nTask);

        for (int i = 0; i < nTask; ++i) {
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogItem logItem = Utils.generateLogItem(sequenceNumber.getAndIncrement());
                        producer.send(
                                System.getenv("PROJECT"),
                                System.getenv("LOG_STORE"),
                                Utils.getTopic(),
                                Utils.getSource(),
                                logItem,
                                new SampleCallback(logItem, completed));
                    } catch (Exception e) {
                        LOGGER.error("Failed to put logs, e=", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        EXECUTOR_SERVICE.shutdown();

        Utils.doSomething();

        producer.close();

        LOGGER.info("All log complete, completed={}", completed.get());
    }

    final private static class SampleCallback implements Callback {

        private static final Logger LOGGER = LoggerFactory.getLogger(SampleCallback.class);

        private final LogItem logItem;

        private final AtomicLong completed;

        SampleCallback(LogItem logItem, AtomicLong completed) {
            this.logItem = logItem;
            this.completed = completed;
        }

        @Override
        public void onCompletion(Result result) {
            try {
                if (result.isSuccessful()) {
                    LOGGER.info("Send log successfully, logItem={}", logItem.ToJsonString());
                } else {
                    LOGGER.error(
                            "Failed to send log, logItem={}, errorCode={}, errorMessage={}, attemptCount={}",
                            logItem.ToJsonString(),
                            result.getErrorCode(),
                            result.getErrorMessage(),
                            result.getAttempts().size());
                }
            } finally {
                completed.getAndIncrement();
            }
        }
    }


}
