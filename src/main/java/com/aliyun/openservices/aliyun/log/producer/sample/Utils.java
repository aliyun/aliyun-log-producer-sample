package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.*;
import com.aliyun.openservices.log.common.LogItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  private static final Random RANDOM = new Random();

  private static final String CHARS = "abcdefghijklmnopqrstuvwxyz";

  public static Producer createProducer() {
    ProducerConfig producerConfig = new ProducerConfig(buildProjectConfigs());
    return new LogProducer(producerConfig);
  }

  private static ProjectConfigs buildProjectConfigs() {
    ProjectConfigs projectConfigs = new ProjectConfigs();
    projectConfigs.put(buildProjectConfig());
    return projectConfigs;
  }

  private static ProjectConfig buildProjectConfig() {
    String project = System.getenv("PROJECT");
    String endpoint = System.getenv("ENDPOINT");
    String accessKeyId = System.getenv("ACCESS_KEY_ID");
    String accessKeySecret = System.getenv("ACCESS_KEY_SECRET");
    return new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret);
  }

  public static LogItem generateLogItem(long id) {
    LogItem logItem = new LogItem();
    logItem.PushBack("id", String.valueOf(id));
    logItem.PushBack("key1", generateStr(10));
    logItem.PushBack("key2", generateStr(10));
    return logItem;
  }

  public static List<LogItem> generateLogItems(int n) {
    List<LogItem> logItems = new ArrayList<LogItem>();
    for (int i = 0; i < n; ++i) {
      logItems.add(generateLogItem(i));
    }
    return logItems;
  }

  private static String generateStr(int len) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; ++i) {
      sb.append(CHARS.charAt(RANDOM.nextInt(26)));
    }
    return sb.toString();
  }

  public static void doSomething() throws InterruptedException {
    LOGGER.info("Before doSomething");
    Thread.sleep(3000);
    LOGGER.info("After doSomething");
  }

  public static String getTopic() {
    return "topic-" + RANDOM.nextInt(5);
  }

  public static String getSource() {
    return "source-" + RANDOM.nextInt(10);
  }
}
