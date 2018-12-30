package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.aliyun.log.producer.errors.LogsTooLargeException;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.aliyun.log.producer.errors.TimeoutException;
import com.aliyun.openservices.log.common.LogItem;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      EXECUTOR_SERVICE.submit(
          new Runnable() {
            @Override
            public void run() {
              LogItem logItem = Utils.generateLogItem(sequenceNumber.getAndIncrement());
              try {
                String project = System.getenv("PROJECT");
                String logStore = System.getenv("LOG_STORE");
                producer.send(
                    project,
                    logStore,
                    Utils.getTopic(),
                    Utils.getSource(),
                    logItem,
                    new SampleCallback(project, logStore, logItem, completed));
              } catch (InterruptedException e) {
                LOGGER.warn("The current thread has been interrupted during send logs.");
              } catch (Exception e) {
                if (e instanceof LogsTooLargeException) {
                  LOGGER.error(
                      "The size of log is larger than the maximum allowable size, e={}", e);
                } else if (e instanceof TimeoutException) {
                  LOGGER.error(
                      "The time taken for allocating memory for the logs has surpassed., e={}", e);
                } else {
                  LOGGER.error("Failed to send log, logItem={}, e=", logItem, e);
                }
              } finally {
                latch.countDown();
              }
            }
          });
    }
    latch.await();
    EXECUTOR_SERVICE.shutdown();

    Utils.doSomething();

    try {
      producer.close();
    } catch (InterruptedException e) {
      LOGGER.warn("The current thread has been interrupted from close.");
    } catch (ProducerException e) {
      LOGGER.info("Failed to close producer, e=", e);
    }

    LOGGER.info("All log complete, completed={}", completed.get());
  }

  private static final class SampleCallback implements Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleCallback.class);

    private final String project;

    private final String logStore;

    private final LogItem logItem;

    private final AtomicLong completed;

    SampleCallback(String project, String logStore, LogItem logItem, AtomicLong completed) {
      this.project = project;
      this.logStore = logStore;
      this.logItem = logItem;
      this.completed = completed;
    }

    @Override
    public void onCompletion(Result result) {
      try {
        if (result.isSuccessful()) {
          LOGGER.info("Send log successfully.");
        } else {
          LOGGER.error(
              "Failed to send log, project={}, logStore={}, logItem={}, result={}",
              project,
              logStore,
              logItem.ToJsonString(),
              result);
        }
      } finally {
        completed.getAndIncrement();
      }
    }
  }
}
