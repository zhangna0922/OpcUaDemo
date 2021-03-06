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
     * @return opu ua ????????????
     * @throws Exception
     */
    @Bean
    public OpcUaClient createClient() throws Exception {

        // ?????????????????????
        String EndPointUrl = endPointUrl;
        //???????????? None???Basic256???Basic128Rsa15???Basic256Sha256
        SecurityPolicy securityPolicy = SecurityPolicy.None;

        //??????????????????
        List<EndpointDescription> endpointDescription = DiscoveryClient.getEndpoints(EndPointUrl).get();
        //???????????????????????????????????????????????????????????????????????????
        EndpointDescription endpoint = endpointDescription.stream()
                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getUri()))
//                    .filter(clientExample.endpointFilter())
                .findFirst()
                .orElseThrow(() -> new Exception("?????????????????????"));

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("test")) // opc ua ????????????
                .setApplicationUri(EndPointUrl)// ??????
                .setEndpoint(endpoint)// ?????????????????????
                .setRequestTimeout(uint(10000)) //????????????
                .build();

        return OpcUaClient.create(config);
    }

    private final AtomicLong clientHandles = new AtomicLong(1L);

    /**
     * opc ua  ??????????????????
     *
     * @throws Exception
     */
    @Bean
    public void createSubscription() throws Exception {
        // ??????OPC UA??????????????????
        OpcUaClient client = this.createClient();
        // ??????????????????
        client.connect().get();
        //?????????????????????
        //??????????????????1000ms???????????????
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
        // ?????????????????????key
        for (int i = 0; i < opcModels.size(); i++) {
            OpcModel opcModel = opcModels.get(i);
            String node = opcModel.getAccessPath();
            //?????????????????????
            NodeId nodeId = new NodeId(opcModel.getNamespaceIndex(), node);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
            //?????????????????????
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1 + i),  // ??????????????????????????????key?????????
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
                            logger.info("===== >  here is callbacks ... ???????????????????????? ");
                            logger.info("subscription value received: item={}, value={}", item1.getReadValueId().getNodeId(), value.getValue());
                        }
                );

        //?????????????????????????????????????????????????????????????????????
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
