package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfigs;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.aliyun.log.producer.internals.LogSizeCalculator;
import com.aliyun.openservices.log.common.LogItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePerformance {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithCallback.class);

  public static void main(String[] args) throws InterruptedException {
    final String project = System.getenv("PROJECT");
    final String logStore = System.getenv("LOG_STORE");
    final String endpoint = System.getenv("ENDPOINT");
    final String accessKeyId = System.getenv("ACCESS_KEY_ID");
    final String accessKeySecret = System.getenv("ACCESS_KEY_SECRET");
    int sendThreadCount = Integer.valueOf(System.getenv("SEND_THREAD_COUNT"));
    int ioThreadCount = Integer.valueOf(System.getenv("IO_THREAD_COUNT"));
    final int times = Integer.valueOf(System.getenv("TIMES"));
    final int logsCountInSend = Integer.valueOf(System.getenv("LOGS_COUNT_IN_SEND"));
    LOGGER.info(
        "project={}, logStore={}, endpoint={}, sendThreadCount={}, ioThreadCount={}, times={}",
        project,
        logStore,
        endpoint,
        sendThreadCount,
        ioThreadCount,
        times);
    ExecutorService executorService = Executors.newFixedThreadPool(sendThreadCount);
    int logSizeInBytes = LogSizeCalculator.calculate(getLogItem());
    LOGGER.info("logSizeInBytes={}", logSizeInBytes);
    LOGGER.info("availableProcessors={}", Runtime.getRuntime().availableProcessors());

    ProjectConfigs projectConfigs = new ProjectConfigs();
    projectConfigs.put(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));
    ProducerConfig producerConfig = new ProducerConfig(projectConfigs);
    producerConfig.setIoThreadCount(ioThreadCount);

    final Producer producer = new LogProducer(producerConfig);
    final AtomicInteger successCount = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(sendThreadCount);
    LOGGER.info("Test started.");
    long t1 = System.currentTimeMillis();
    for (int i = 0; i < sendThreadCount; ++i) {
      executorService.submit(
          new Runnable() {
            @Override
            public void run() {
              try {
                for (int i = 0; i < times; ++i) {
                  if (logsCountInSend == 1) {
                    producer.send(
                        project,
                        logStore,
                        getTopic(i),
                        getSource(i),
                        getLogItem(),
                        new Callback() {
                          @Override
                          public void onCompletion(Result result) {
                            if (result.isSuccessful()) {
                              successCount.incrementAndGet();
                            }
                          }
                        });
                  } else {
                    producer.send(
                        project,
                        logStore,
                        getTopic(i),
                        getSource(i),
                        getLogItems(logsCountInSend),
                        new Callback() {
                          @Override
                          public void onCompletion(Result result) {
                            if (result.isSuccessful()) {
                              successCount.incrementAndGet();
                            }
                          }
                        });
                  }
                }
              } catch (Exception e) {
                LOGGER.error("Failed to send log, e=", e);
              } finally {
                latch.countDown();
              }
            }
          });
    }
    latch.await();
    while (true) {
      if (successCount.get() == sendThreadCount * times) {
        break;
      }
      Thread.sleep(1000);
    }
    long t2 = System.currentTimeMillis();
    LOGGER.info("Test end.");
    LOGGER.info("======Summary======");
    long totalSizeInBytes = (long) sendThreadCount * times * logsCountInSend * logSizeInBytes;
    LOGGER.info("Total count " + successCount.get() + ".");
    LOGGER.info("Total size " + totalSizeInBytes + " bytes.");
    long timeCost = t2 - t1;
    LOGGER.info("Time cost " + timeCost + " millis");
    double throughput = (totalSizeInBytes * 1.0 * 1000) / (timeCost * 1024 * 1024);
    LOGGER.info("Throughput " + throughput + " MB/s");
    try {
      producer.close();
    } catch (ProducerException e) {
      LOGGER.error("Failed to close producer, e=", e);
    }
    executorService.shutdown();
  }

  private static LogItem getLogItem() {
    LogItem logItem = new LogItem();
    logItem.PushBack("key-00", "value-00");
    logItem.PushBack("key-01", "value-01");
    logItem.PushBack("key-02", "value-02");
    logItem.PushBack("key-03", "value-03");
    logItem.PushBack("key-04", "value-04");
    logItem.PushBack("key-05", "value-05");
    logItem.PushBack("key-06", "value-06");
    logItem.PushBack("key-07", "value-07");
    logItem.PushBack("key-08", "value-08");
    logItem.PushBack("key-09", "value-09");
    logItem.PushBack("key-10", "value-10");
    logItem.PushBack("key-11", "value-11");
    return logItem;
  }

  private static List<LogItem> getLogItems(int n) {
    List<LogItem> logItems = new ArrayList<LogItem>(n);
    for (int i = 0; i < n; ++i) {
      logItems.add(getLogItem());
    }
    return logItems;
  }

  private static String getTopic(int i) {
    return "t-" + i % 4;
  }

  private static String getSource(int i) {
    return "s-" + i % 5;
  }
}
