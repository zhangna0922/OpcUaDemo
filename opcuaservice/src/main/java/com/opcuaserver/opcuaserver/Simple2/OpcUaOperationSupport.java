package com.opcuaserver.opcuaserver.Simple2;

import com.google.common.collect.ImmutableList;
import com.opcuaserver.opcuaserver.Simple2.ClientGen;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.methods.UaMethod;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

public class OpcUaOperationSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws UaException {
        ClientGen clientGen = new ClientGen();
        clientGen.createClient();

        OpcUaOperationSupport opcUaOperationSupport = new OpcUaOperationSupport();
        /*读节点ns=3;s=String*/
        OpcModel readOpcModel = new OpcModel();
        readOpcModel.setAccessPath("String");
        readOpcModel.setItemName("");
        readOpcModel.setNamespaceIndex(3);
//        opcUaOperationSupport.readPLC(readOpcModel);

        /*写节点ns=3;s=String 值改为hello world*/
        OpcModel writeOpcModel = new OpcModel();
        writeOpcModel.setAccessPath("Armed.Floodlight");
        writeOpcModel.setItemName("");
        writeOpcModel.setNamespaceIndex(2);
        opcUaOperationSupport.writePLC(writeOpcModel,"123456");

        /*读节点ns=3;s=String*/
        OpcModel readObjOpcModel = new OpcModel();
        readObjOpcModel.setAccessPath("MyLevel.Alarm");
        readObjOpcModel.setItemName("");
        readObjOpcModel.setNamespaceIndex(2);
        NodeId nodeId = NodeId.parse("i=85");
//        opcUaOperationSupport.browseNode("DeepFolder", ClientGen.opcUaClient, Identifiers.ObjectTypesFolder);

//        opcUaOperationSupport.browseNodeExample("", ClientGen.opcUaClient, NodeId.parse("ns=2;s=MyDevice"));
//        opcUaOperationSupport.browseNodeExample("", ClientGen.opcUaClient, NodeId.parse("ns=2;s=MyLevel.Alarm"));
//        opcUaOperationSupport.browseNodeExample("", ClientGen.opcUaClient, NodeId.parse("ns=2;s=MyLevel.Alarm/0:ConfirmedState"));


//    public AddNodesItem(ExpandedNodeId parentNodeId, NodeId referenceTypeId, ExpandedNodeId requestedNewNodeId, QualifiedName browseName,
//    NodeClass nodeClass, ExtensionObject nodeAttributes, ExpandedNodeId typeDefinition) {


//        opcUaOperationSupport.uaMethod( ClientGen.opcUaClient);
//            opcUaOperationSupport.test2( ClientGen.opcUaClient);
            opcUaOperationSupport.uaMethod(ClientGen.opcUaClient);

        OpcModel subOpcModel1 = new OpcModel();
        subOpcModel1.setAccessPath("String");
        subOpcModel1.setItemName("");
        subOpcModel1.setNamespaceIndex(3);
//        opcUaOperationSupport.createSubscription(writeOpcModel);
//        opcUaOperationSupport.readPLC(readOpcModel);
//        opcUaOperationSupport.createSubscription();
    }

    public void test2(OpcUaClient client) throws ExecutionException, InterruptedException {
        client.connect().get();
        System.out.println("AAAAAAAA>>>>>>>>>" + client.getConfig());
        DataValue dataValue = client.readValue(
                0.0,
                TimestampsToReturn.Neither,
                NodeId.parse("ns=2;s=MyDevice")
        ).get();
        System.out.println("bbbbbbbbbb>>>>>>>>>");

        ExtensionObject xo = (ExtensionObject) dataValue.getValue().getValue();

//        Object value = xo.decode(client.getDynamicSerializationContext());
    }

    public ResultBean readPLC(OpcModel server) {
        ResultBean resultBean = new ResultBean(false, "");
        String accessPath = server.getAccessPath();
        String itemName = server.getItemName();
        String s = StringUtils.hasText(itemName) ? "." : "";
        String item = accessPath + s + itemName;
        OpcUaClient client = ClientGen.opcUaClient;
        try {
            client.connect().get();
            NodeId nodeId = new NodeId(server.getNamespaceIndex(), item);
            CompletableFuture<DataValue> readValue = client.readValue(0.0, TimestampsToReturn.Both, nodeId);
            DataValue value = readValue.get();
            String plcValue = value.getValue().getValue().toString();
            String statusCode = value.getStatusCode().toString();
            if (value.getStatusCode() != null) {
                if (value.getStatusCode().isGood()) {
                    logger.warn("获取数据成功,数据：" + plcValue);
                    resultBean.setMsg("获取数据成功,数据：" + plcValue);
                    resultBean.setResult(plcValue);
                    resultBean.setSuccess(true);
                } else {
                    resultBean.setMsg("获取数据失败。");
                    resultBean.setResult(statusCode);
                    resultBean.setSuccess(false);
                }
                logger.info("======== >  it means successfully read StatusCode = {}", statusCode);
            } else {
                resultBean.setMsg("获取数据出现异常。");
                resultBean.setResult("出错了。。。。");
                resultBean.setSuccess(false);
                logger.error("=====》  读数据异常，未获取到数据。。。");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultBean;
    }

    private void browseNodeExample(String indent, OpcUaClient client, NodeId browseRoot) {
        try {
            try {
                client.connect().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            List<? extends UaNode> nodes = client.getAddressSpace().browseNodes(browseRoot);

            System.out.println("uaNode size -----" + nodes.size());
            for (UaNode node : nodes) {
                logger.info("Node----" + node.getNodeId() + ";;;;" + node.getBrowseName().getName(), indent);

                // recursively browse to children
                browseNode(indent + "  ", client, node.getNodeId());
            }
        } catch (UaException e) {
            logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
        }
    }

    /**
     * 浏览节点
     */
    public void browseNode() {
        OpcUaClient client = ClientGen.opcUaClient;
        try {
            client.connect().get();
            browseNode("", client, Identifiers.RootFolder);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * 写入 PLC
     *
     * @param server
     * @param editValue
     * @return
     */
    public ResultBean writePLC(OpcModel server, String editValue) {
        ResultBean resultBean = new ResultBean(false, "");
        String accessPath = server.getAccessPath();
        String itemName = server.getItemName();
        String s = StringUtils.hasText(itemName) ? "" : "";
        String item = accessPath + s + itemName;
        try {
//          创建连接
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();
//          创建节点
            NodeId nodeId = new NodeId(server.getNamespaceIndex(), item);
//          创建Variant对象和DataValue对象
            Variant v;
            boolean b = true;
            switch (editValue) {
                case "1":
                    v = new Variant(b);
                    break;
                case "0":
                    b = false;
                    v = new Variant(b);
                    break;
                default:
                    v = new Variant(editValue);
                    break;
            }
            DataValue dv = new DataValue(v);
            StatusCode statusCode = client.writeValue(nodeId, dv).get();
            if (statusCode.isGood()) {
                logger.warn("写入数据成功。" + dv);
                resultBean.setMsg("写入数据成功。");
                resultBean.setSuccess(true);
                resultBean.setResult(editValue);
                logger.warn("========== >  it means successfully Wrote '{}' to nodeId={}, statusCodes = {}", v, nodeId, statusCode.toString());
            } else {
                logger.warn("写入数据失败。" + dv + "  " + statusCode);
                resultBean.setMsg("写入数据失败。item = " + item);
                resultBean.setSuccess(false);
                resultBean.setResult(statusCode.toString());
                logger.error("statusCodes:" + statusCode.toString());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultBean;
    }


    /**
     * 订阅变量
     *
     * @param server
     * @return
     */
    public ResultBean createSubscription(OpcModel server) {
        ResultBean resultBean = new ResultBean(false, "");
        String accessPath = server.getAccessPath();
        String itemName = server.getItemName();
        String s = StringUtils.hasText(itemName) ? "." : "";
//        String item = accessPath + s + itemName;
        String item = accessPath +  itemName;
        try {
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();

            //创建发布间隔1000ms的订阅对象
            UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

            //创建监控的参数
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1),
                    // 发布间隔
                    1000.0,
                    // filter, 空表示用默认值
                    null,
                    // 队列大小
                    uint(10),
                    //放弃旧配置
                    true
            );
            //创建订阅的变量
            NodeId nodeId = new NodeId(server.getNamespaceIndex(), item);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
            //创建监控项请求
            //该请求最后用于创建订阅。
            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
            List<MonitoredItemCreateRequest> requests = new ArrayList<>();
            requests.add(request);
            //创建监控项，并且注册变量值改变时候的回调函数。
            UaSubscription.ItemCreationCallback onItemCreated =
                    (subscriptionItem, id) -> subscriptionItem.setValueConsumer((UaMonitoredItem item1, DataValue value) -> {
                                logger.info("===== >  here is callbacks ... 订阅的回调函数。 ");
                                resultBean.setMsg("订阅成功, item1 :" + item1.getReadValueId().getNodeId().toString());
                                resultBean.setResult(value.getValue().toString());
                                resultBean.setSuccess(true);
                                logger.info("subscription value received: item={}, value={}", item1.getReadValueId().getNodeId(), value.getValue());
                            }
                    );
            //TODO
            List<UaMonitoredItem> monitoredItems = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    newArrayList(request),
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
            return resultBean;
        } catch (Exception e) {
            logger.error("订阅变量失败",e);
            resultBean.setMsg(e.getMessage());
            resultBean.setResult("订阅变量进入异常。");
            resultBean.setSuccess(false);
        }
        return resultBean;
    }

    /**
     * 查看历史变量记录
     *
     * @param server
     * @param editValue
     * @return
     */
    public ResultBean historyRead(OpcModel server, String editValue) {
        return null;
    }

    /**
     * 浏览节点（抽取方法）
     *
     * @param indent
     * @param client
     * @param browseRoot
     */
    private void browseNode(String indent, OpcUaClient client, NodeId browseRoot) {
        try {
            client.connect().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        BrowseDescription browse = new BrowseDescription(
                browseRoot,
                BrowseDirection.Forward,
                Identifiers.References,
                true,
                uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
                uint(BrowseResultMask.All.getValue())
        );
        try {
            BrowseResult browseResult = client.browse(browse).get();
            List<ReferenceDescription> references = toList(browseResult.getReferences());

            for (ReferenceDescription rd : references) {
                logger.info("{} Node={}", indent, rd.getBrowseName().getName());

                // recursively browse to children
                rd.getNodeId().toNodeId(client.getNamespaceTable())
                        .ifPresent(nodeId -> browseNode(indent + "  ", client, nodeId));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
        }
    }

    /***************************************************/
    public void uaMethod(OpcUaClient client) {
        try {
            client.connect().get();

            NodeId nodeId = NodeId.parse("ns=2;s=MyLevel.Alarm");
            System.out.println("1111111111111>>>>>>>>>>>>>>>>>>>..");
            UaObjectNode objectNode = client.getAddressSpace()
                    .getObjectNode(nodeId);
            System.out.println("2222222222222");
           /* UaVariableNode comment = objectNode.getProperty("enable");
            DataValue dataValue = comment.readValue();
            String s = dataValue.getValue().toString();
            System.out.println("读取值："+s);*/
           /* UaMethod uaMethod = objectNode.getMethod("activeState");
            Variant[] inputs = {new Variant(true)};
            Variant[] call = uaMethod.call(inputs);
            System.out.println("call-----" + call);*/
        } catch (InterruptedException | UaException e) {
           logger.error(e.getMessage(),e);
        } catch (ExecutionException ex) {
            logger.error(ex.getMessage(),ex);
        }
    }


}

