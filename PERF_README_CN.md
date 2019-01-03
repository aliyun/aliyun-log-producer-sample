# SamplePerformance 运行步骤

执行以下命令编译源码、生成 jar 包。
```
mvn clean package
```

输入以下命令运行性能测试的样例，请将 \<project\>、\<logstore\>、\<endpoint\>、\<access_key_id\>、\<access_key_secret\> 替换为实际值。
```
export PROJECT=<project> \
export LOG_STORE=<logstore> \
export ENDPOINT=<endpoint> \
export ACCESS_KEY_ID=<access_key_id> \
export ACCESS_KEY_SECRET=<access_key_secret> \
export SEND_THREAD_COUNT=10 \
export TIMES=20000000 \
export IO_THREAD_COUNT=16 \
export TOTAL_SIZE_IN_BYTES=104857600 \
export LEVEL=INFO && \
java -jar target/aliyun-log-producer-sample-0.0.1-SNAPSHOT.jar -Xms2g -Xmx2g
```

**注意**：运行以上命令向您的目标 logstore 写入约 115 GB 的数据，在非性能测试场景下谨慎运行。
