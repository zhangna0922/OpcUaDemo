//package com.opcuaserver.opcuaserver.test;
//
//import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
//import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
//import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
//import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
//import org.eclipse.milo.opcua.stack.core.AttributeId;
//import org.eclipse.milo.opcua.stack.core.Identifiers;
//import org.eclipse.milo.opcua.stack.core.UaException;
//import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
//import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
//import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
//import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
//import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
//import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
//import org.eclipse.milo.opcua.stack.core.types.structured.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//
//import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
//
//public class OpcUaSimple {
//
//    /**
//     *浏览节点
//     * @param client
//     * @throws ExecutionException
//     * @throws InterruptedException
//     */
//    public void browseNode(OpcUaClient client) throws ExecutionException, InterruptedException {
//        //开启连接
//        client.connect().get();
//
////        List<Node> nodes = client.getAddressSpace().browse(Identifiers.RootFolder).get();
//        ReferenceDescription nodes = null;
//        try {
//            nodes = client.getAddressSpace().browse(Identifiers.RootFolder).get(1);
//        } catch (UaException e) {
//            e.printStackTrace();
//        }
//
////        for(Node node:nodes){
////            System.out.println("Node= " + node.getBrowseName().get().getName());
////        }
//    }
//
//
//    /**
//     * 获取节点值
//     * @param client
//     * @throws ExecutionException
//     * @throws InterruptedException
//     */
//    public void readValue(OpcUaClient client) throws ExecutionException, InterruptedException {
//        //创建连接
//        client.connect().get();
//
//        NodeId nodeId = new NodeId(3,"\"test_value\"");
//
//        DataValue value = client.readValue(0.0, TimestampsToReturn.Both, nodeId).get();
//
//        System.out.println((Integer)value.getValue().getValue());
//    }
//
//    /**
//     * 写变量
//     * @param client
//     * @param value
//     * @throws ExecutionException
//     * @throws InterruptedException
//     */
//    public void writeValue(OpcUaClient client, int value) throws ExecutionException, InterruptedException {
//        //创建连接
//        client.connect().get();
//
//        //创建变量节点
//        NodeId nodeId = new NodeId(3, "\"test_value\"");
//
//        //创建Variant对象和DataValue对象
//        Variant v = new Variant(value);
//        DataValue dataValue = new DataValue(v, null, null);
//
//        StatusCode statusCode = client.writeValue(nodeId, dataValue).get();
//
//        System.out.println(statusCode.isGood());
//    }
//
//
//    public void createSubscription(OpcUaClient client) throws ExecutionException, InterruptedException {
//
//        //创建连接
//        client.connect().get();
//
//        //创建发布间隔1000ms的订阅对象
//        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();
//
//        //创建订阅的变量
//        NodeId nodeId = new NodeId(3,"\"test_value\"");
//        ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(),null,null);
//
//        //创建监控的参数
//        MonitoringParameters parameters = new MonitoringParameters(
//                uint(1),
//                1000.0,     // sampling interval
//                null,       // filter, null means use default
//                uint(10),   // queue size
//                true        // discard oldest
//        );
//
//        //创建监控项请求
//        //该请求最后用于创建订阅。
//        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
//
//        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
//        requests.add(request);
//
//        //创建监控项，并且注册变量值改变时候的回调函数。
//        List<UaMonitoredItem> items = subscription.createMonitoredItems(
//                TimestampsToReturn.Both,
//                requests,
//                (item,id)->{
//                    item.setValueConsumer((item2, value2)->{
//                        System.out.println("nodeid :"+item2.getReadValueId().getNodeId());
//                        System.out.println("value :"+value2.getValue().getValue());
//                    });
//                }
//        ).get();
//    }
//}
