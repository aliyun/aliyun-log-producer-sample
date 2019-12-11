package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.*;
import com.aliyun.openservices.aliyun.log.producer.errors.LogSizeTooLargeException;
import com.aliyun.openservices.aliyun.log.producer.errors.MaxBatchCountExceedException;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.aliyun.log.producer.errors.ResultFailedException;
import com.aliyun.openservices.aliyun.log.producer.errors.TimeoutException;
import com.aliyun.openservices.log.common.LogItem;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleProducerWithFuture {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithFuture.class);

  private static final ExecutorService EXECUTOR_SERVICE = Executors
      .newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 1));

  public static void main(String[] args) throws InterruptedException {
    Producer producer = Utils.createProducer();
    int n = 100;
    int size = 20;

    // The number of logs that have finished (either successfully send, or failed)
    final AtomicLong completed = new AtomicLong(0);

    for (int i = 0; i < n; ++i) {
      List<LogItem> logItems = Utils.generateLogItems(size);
      try {
        String project = System.getenv("PROJECT");
        String logStore = System.getenv("LOG_STORE");
        ListenableFuture<Result> f = producer.send(project, logStore, logItems);
        Futures.addCallback(
            f, new SampleFutureCallback(project, logStore, logItems, completed), EXECUTOR_SERVICE);
      } catch (InterruptedException e) {
        LOGGER.warn("The current thread has been interrupted during send logs.");
      } catch (Exception e) {
        if (e instanceof MaxBatchCountExceedException) {
          LOGGER.error("The logs exceeds the maximum batch count, e={}", e);
        } else if (e instanceof LogSizeTooLargeException) {
          LOGGER.error("The size of log is larger than the maximum allowable size, e={}", e);
        } else if (e instanceof TimeoutException) {
          LOGGER.error("The time taken for allocating memory for the logs has surpassed., e={}", e);
        } else {
          LOGGER.error("Failed to send logs, e=", e);
        }
      }
    }

    Utils.doSomething();

    try {
      producer.close();
    } catch (InterruptedException e) {
      LOGGER.warn("The current thread has been interrupted from close.");
    } catch (ProducerException e) {
      LOGGER.info("Failed to close producer, e=", e);
    }

    EXECUTOR_SERVICE.shutdown();
    while (!EXECUTOR_SERVICE.isTerminated()) {
      EXECUTOR_SERVICE.awaitTermination(100, TimeUnit.MILLISECONDS);
    }
    LOGGER.info("All log complete, completed={}", completed.get());
  }

  private static final class SampleFutureCallback implements FutureCallback<Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleFutureCallback.class);

    private final String project;

    private final String logStore;

    private final List<LogItem> logItems;

    private final AtomicLong completed;

    SampleFutureCallback(
        String project, String logStore, List<LogItem> logItems, AtomicLong completed) {
      this.project = project;
      this.logStore = logStore;
      this.logItems = logItems;
      this.completed = completed;
    }

    @Override
    public void onSuccess(@Nullable Result result) {
      LOGGER.info("Send logs successfully.");
      completed.getAndIncrement();
    }

    @Override
    public void onFailure(Throwable t) {
      if (t instanceof ResultFailedException) {
        Result result = ((ResultFailedException) t).getResult();
        LOGGER.error(
            "Failed to send logs, project={}, logStore={}, result={}", project, logStore, result);
      } else {
        LOGGER.error("Failed to send log, e=", t);
      }
      completed.getAndIncrement();
    }
  }
}
