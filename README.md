# Aliyun LOG Java Producer Sample Application

[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

[中文版 README](/README_CN.md)

This repo provides the sample application for [Aliyun LOG Java Producer](https://github.com/aliyun/aliyun-log-producer), it will help you to master the producer library more quickly.

## Prerequisite
You will need the following:
* Opening [Aliyun Log Service](https://www.alibabacloud.com/product/log-service) and create target project, logstore in advancce.
* Prepare the AK used by producer.
* Install JDK 1.6+.
* Install Maven (`brew install maven`，`sudo apt-get install maven`).

If you use the sub-account AK, you need to configure the following permissions for the account to ensure it can put logs to the target logstore.

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

## Set environment variables
The sample will read the value of project, endpoint, logstore and AK from environment variables. You can set the value of them in `.producerrc` and then run the command `source .producerrc`.

## Run the Sample
After the above tasks have been finished, do a clean build to compile the source code and generate jar:
```
mvn clean package
```

Then run the producer with callback to send some logs to your logstore:
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithCallback"
```

Run the producer with future to send some logs to your logstore:
```
mvn exec:java -Dexec.mainClass="com.aliyun.openservices.aliyun.log.producer.sample.SampleProducerWithFuture"
```




