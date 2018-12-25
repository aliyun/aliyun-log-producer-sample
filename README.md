# Aliyun LOG Java Producer 应用示例

## 前提条件
您需要准备好以下内容：
* 开通[日志服务](https://cn.aliyun.com/product/sls)，创建好用于存放数据的 project、logStore。
* 准备好 producer 需要的 AK。
* 安装 JDK 1.6+。
* 安装 Maven (`brew install maven`，`sudo apt-get install maven`)。

如果您使用的是子账号 AK，需要保证该子账号拥有目标 project、logStore 的写权限。如何授权请参考 [RAM 子用户访问](https://help.aliyun.com/document_detail/29049.html)。 

## 设置环境变量
示例程序会从环境变量中读取 project、endpoint、logStore、AK 等信息，您可以将这些值填入文件 .producerrc 中，然后执行命令`source .producerrc`。

## 运行样例
准备工作完成后，执行以下命令编译源码、生成 jar 包。
```
mvn clean package
```

编译完成后，执行以下命令执行使用 callback 的样例。
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithCallback"
```

编译完成后，执行以下命令执行使用 future 的样例。
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithFuture"
```




