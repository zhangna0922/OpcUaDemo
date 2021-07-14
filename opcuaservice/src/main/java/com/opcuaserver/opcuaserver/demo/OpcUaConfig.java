package com.opcuaserver.opcuaserver.demo;


import com.opcuaserver.opcuaserver.Simple2.OpcModel;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.client.UaStackClientConfig;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Configuration
public class OpcUaConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${agrilfence.endPointUrl}")
    private String endPointUrl = "opc.tcp://127.0.0.1:12686/milo";

    /**
     * @return opu ua 准备连接
     * @throws Exception
     */
    @Bean
    public OpcUaClient createClient() throws Exception {

        // 连接地址端口号
        String EndPointUrl = endPointUrl;
        //安全策略 None、Basic256、Basic128Rsa15、Basic256Sha256
        SecurityPolicy securityPolicy = SecurityPolicy.None;

        //安全策略选择
        List<EndpointDescription> endpointDescription = DiscoveryClient.getEndpoints(EndPointUrl).get();
        //过滤掉不需要的安全策略，选择一个自己需要的安全策略
        EndpointDescription endpoint = endpointDescription.stream()
                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getUri()))
//                    .filter(clientExample.endpointFilter())
                .findFirst()
                .orElseThrow(() -> new Exception("没有连接上端点"));

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("test")) // opc ua 定义的名
                .setApplicationUri(EndPointUrl)// 地址
                .setEndpoint(endpoint)// 安全策略等配置
                .setRequestTimeout(uint(10000)) //等待时间
                .build();

        return OpcUaClient.create(config);
    }

    private final AtomicLong clientHandles = new AtomicLong(1L);

    /**
     * opc ua  打开连接订阅
     *
     * @throws Exception
     */
    @Bean
    public void createSubscription() throws Exception {
        // 获取OPC UA服务器的数据
        OpcUaClient client = this.createClient();
        // 同步建立连接
        client.connect().get();
        //创建监控项请求
        //创建发布间隔1000ms的订阅对象
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(500.0).get();
//        List<String> lights = Arrays.asList("Status", "Armed", "Enable", "Disable", "On", "Off");
//        List<String> zones = Arrays.asList("Status", "Armed", "Enable", "Disable", "LastModified", "CameraId", "CameraPreset");
        List<String> lights = Arrays.asList("Status", "Armed");
        List<String> zones = Arrays.asList("Status", "Armed", "LastModified", "CameraId", "CameraPreset");

        List<OpcModel> opcModels = new ArrayList<>();
        List<String> strings = Arrays.asList("Floodlight", "Siren", "Strobelight", "Zone001", "Zone002");
        for (String string : strings) {
            if (string.contains("Zone")) {
                for (String zone : zones) {
                    opcModels.add(new OpcModel(2, zone + "." + string));
                }
            } else {
                for (String zone : lights) {
                    opcModels.add(new OpcModel(2, zone + "." + string));
                }
            }
        }

        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
        // 你所需要订阅的key
        for (int i = 0; i < opcModels.size(); i++) {
            OpcModel opcModel = opcModels.get(i);
            String node = opcModel.getAccessPath();
            //创建订阅的变量
            NodeId nodeId = new NodeId(opcModel.getNamespaceIndex(), node);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
            //创建监控的参数
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1 + i),  // 为了保证唯一性，否则key值一致
                    0.0,     // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
            );

            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
            requests.add(request);
        }

        UaSubscription.ItemCreationCallback onItemCreated =
                (subscriptionItem, id) -> subscriptionItem.setValueConsumer((UaMonitoredItem item1, DataValue value) -> {
                            logger.info("===== >  here is callbacks ... 订阅的回调函数。 ");
                            logger.info("subscription value received: item={}, value={}", item1.getReadValueId().getNodeId(), value.getValue());
                        }
                );

        //创建监控项，并且注册变量值改变时候的回调函数。
        List<UaMonitoredItem> monitoredItems = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                requests,
                onItemCreated
        ).get();

        for (UaMonitoredItem monitoredItem : monitoredItems) {
            if (monitoredItem.getStatusCode().isGood()) {
                logger.info("item created for nodeId={}", monitoredItem.getReadValueId().getNodeId());
            } else {
                logger.warn(
                        "failed to create item for nodeId={} (status={})",
                        monitoredItem.getReadValueId().getNodeId(), monitoredItem.getStatusCode());
            }
        }
    }

}
