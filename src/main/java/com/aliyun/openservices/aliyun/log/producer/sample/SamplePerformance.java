package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfigs;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePerformance {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithCallback.class);

  private static final Random random = new Random();

  public static void main(String[] args) throws InterruptedException {
    final String project = System.getenv("PROJECT");
    final String logStore = System.getenv("LOG_STORE");
    final String endpoint = System.getenv("ENDPOINT");
    final String accessKeyId = System.getenv("ACCESS_KEY_ID");
    final String accessKeySecret = System.getenv("ACCESS_KEY_SECRET");
    int sendThreadCount = Integer.valueOf(System.getenv("SEND_THREAD_COUNT"));
    final int times = Integer.valueOf(System.getenv("TIMES"));
    int ioThreadCount = Integer.valueOf(System.getenv("IO_THREAD_COUNT"));
    int totalSizeInBytes = Integer.valueOf(System.getenv("TOTAL_SIZE_IN_BYTES"));
    LOGGER.info(
        "project={}, logStore={}, endpoint={}, sendThreadCount={}, times={}, ioThreadCount={}, totalSizeInBytes={}",
        project,
        logStore,
        endpoint,
        sendThreadCount,
        times,
        ioThreadCount,
        totalSizeInBytes);
    ExecutorService executorService = Executors.newFixedThreadPool(sendThreadCount);
    ProjectConfigs projectConfigs = new ProjectConfigs();
    projectConfigs.put(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));
    ProducerConfig producerConfig = new ProducerConfig(projectConfigs);
    producerConfig.setBatchSizeThresholdInBytes(3 * 1024 * 1024);
    producerConfig.setBatchCountThreshold(40960);
    producerConfig.setIoThreadCount(ioThreadCount);
    producerConfig.setTotalSizeInBytes(totalSizeInBytes);

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
                  int r = random.nextInt(times);
                  producer.send(
                      project,
                      logStore,
                      generateTopic(r),
                      generateSource(r),
                      generateLogItem(r),
                      new Callback() {
                        @Override
                        public void onCompletion(Result result) {
                          if (result.isSuccessful()) {
                            successCount.incrementAndGet();
                          }
                        }
                      });
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
      Thread.sleep(100);
    }
    long t2 = System.currentTimeMillis();
    LOGGER.info("Test end.");
    LOGGER.info("======Summary======");
    LOGGER.info("Total count " + sendThreadCount * times + ".");
    long timeCost = t2 - t1;
    LOGGER.info("Time cost " + timeCost + " millis");
    try {
      producer.close();
    } catch (ProducerException e) {
      LOGGER.error("Failed to close producer, e=", e);
    }
    executorService.shutdown();
  }

  private static LogItem generateLogItem(int r) {
    LogItem logItem = new LogItem();
    logItem.PushBack("content_key_1", "1abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_2", "2abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_3", "3abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_4", "4abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_5", "5abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_6", "6abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_7", "7abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    logItem.PushBack("content_key_8", "8abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_" + r);
    return logItem;
  }

  private static String generateTopic(int r) {
    return "topic-" + r % 5;
  }

  private static String generateSource(int r) {
    return "source-" + r % 10;
  }
}
