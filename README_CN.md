# Aliyun LOG Java Producer 应用示例

[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[README in English](/README.md)

本 repo 提供了使用 [Aliyun LOG Java Producer](https://github.com/aliyun/aliyun-log-producer) 的样例程序，有助于快速上手 producer library。

## 前提条件
您需要准备好以下内容：
* 开通[日志服务](https://cn.aliyun.com/product/sls)，创建好用于存放数据的 project、logStore。
* 准备好 producer 需要的 AK。
* 安装 JDK 1.6+。
* 安装 Maven (`brew install maven`，`sudo apt-get install maven`)。

如果您使用的是子账号 AK，需要为该账号配置以下权限来保证它能够将日志写入目标 logStore。
<table>
<thead>
<tr>
<th>Action</th>
<th>Resource</th>
</tr>
</thead>
<tbody>
<tr>
<td>log:PostLogStoreLogs</td>
<td>acs:log:${regionName}:${projectOwnerAliUid}:project/${projectName}/logstore/${logstoreName}</td>
</tr>
</tbody>
</table>

关于如何配置权限还可以参考文章 [RAM 子用户访问](https://help.aliyun.com/document_detail/29049.html)。


## 设置环境变量
示例程序会从环境变量中读取 project、endpoint、logStore、AK 等信息，您可以将这些值填入文件 .producerrc 中，然后执行命令`source .producerrc`。

## 运行样例
准备工作完成后，执行以下命令编译源码、生成 jar 包。
```
mvn clean package
```

译完成后，输入以下命令运行使用 callback 的样例。
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithCallback"
```

译完成后，输入以下命令运行使用 future 的样例。
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithFuture"
```

关于 SamplePerformance 的运行方式请参考文档 [SamplePerformance 运行步骤](https://github.com/aliyun/aliyun-log-producer-sample/blob/master/PERF_README_CN.md)。

## 相关资料
* [日志上云利器 - Aliyun LOG Java Producer](https://yq.aliyun.com/articles/682762)
* [Aliyun LOG Java Producer 快速入门](https://yq.aliyun.com/articles/682761)


