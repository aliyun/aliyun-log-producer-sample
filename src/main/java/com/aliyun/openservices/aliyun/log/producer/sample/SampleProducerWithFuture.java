package com.aliyun.openservices.aliyun.log.producer.sample;

import com.aliyun.openservices.aliyun.log.producer.*;
import com.aliyun.openservices.log.common.LogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleProducerWithFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducerWithFuture.class);

    public static void main(String[] args) {
        Producer producer = createProducer();
        try {
            producer.send(System.getenv("PROJECT"), System.getenv("LOG_STORE"), buildLogItem("1"));
        } catch (InterruptedException e) {

        }
    }

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

    public static LogItem buildLogItem(String id) {
        LogItem logItem = new LogItem();
        logItem.PushBack("id", id);
        logItem.PushBack("key1", "val1");
        logItem.PushBack("key2", "val2");
        return logItem;
    }

}
