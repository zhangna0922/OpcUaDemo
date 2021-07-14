package com.opcuaserver.opcuaserver.demo;

import com.opcuaserver.opcuaserver.Simple2.OpcModel;
import com.opcuaserver.opcuaserver.Simple2.ResultBean;
import io.netty.handler.codec.json.JsonObjectDecoder;
import jdk.nashorn.internal.parser.JSONParser;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;


@RestController
@RequestMapping("opcua/")
public class OpcUaController {
    private static final Logger logger = LoggerFactory.getLogger(OpcUaController.class);


    /**
     * 修改节点数据
     * @param nodeId 节点
     * @param opt 操作
     * @param val 修改值
     * @return
     */
    @RequestMapping(value = "writePLC", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultBean writePLC(String nodeId, String opt, String val) {
        OpcModel opcModel = OpcUaUtils.genOpcModel(nodeId, opt);
        return OpcUaUtils.writePLC(opcModel, val);
    }

    /**
     * 读取所有节点数据
     * @return
     */
    @RequestMapping(value = "readPLC", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultBean readPLC() {
        List<String> nodeIds = Arrays.asList("ns=2;i=2001", "ns=2;i=2002", "ns=2;i=2101");
        List<String> statues = Arrays.asList("Status", "Armed");
        List<EquipmentModel> equipmentModels = new ArrayList<>();
        for (String nodeId : nodeIds) {
            EquipmentModel equipmentModel = new EquipmentModel();
            equipmentModel.setNodeId(nodeId);
            Map<String, Object> statusMap = equipmentModel.getStatusMap();
            if (CollectionUtils.isEmpty(statusMap)) {
                statusMap = new HashMap<>(statues.size());
            }

            for (String statue : statues) {
                OpcModel opcModel = OpcUaUtils.genOpcModel(nodeId, statue);
                if (opcModel == null) {
                    continue;
                }

                ResultBean resultBean = OpcUaUtils.readPLC(opcModel);
                if (resultBean.isSuccess()) {
                    equipmentModel.setName(opcModel.getItemName());
                    statusMap.put(statue, resultBean.getResult());
                }
            }
            if (statusMap.isEmpty()) {
                continue;
            }

            equipmentModel.setStatusMap(statusMap);
            equipmentModels.add(equipmentModel);
        }

        return new ResultBean(true, equipmentModels);
    }


    /**
     * 读取所有节点数据
     * @return
     */
    @RequestMapping(value = "readPLC2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultBean readPLC(String nodeId,String attrId){
        OpcUaConfig opcUaConfig = new OpcUaConfig();
        OpcUaClient client = null;
        ResultBean resultBean = new ResultBean();
        try {
            client = opcUaConfig.createClient();
            // 同步建立连接
            client.connect().get();
            UaVariableNode node = client.getAddressSpace().getVariableNode(
                    new NodeId(2, nodeId)
            );

            logger.info("DataType={}", node.getDataType());

            // Read the current value
            DataValue value = node.readValue();
            logger.info("Value={}", value);
            resultBean.setResult(value);
            resultBean.setSuccess(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return resultBean;
    }



    /**
     * 执行方法
     * @return
     */
    @RequestMapping(value = "toExecute", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultBean executeFunction(String nodeId, String opt){

        OpcUaConfig opcUaConfig = new OpcUaConfig();
        OpcUaClient client = null;
        ResultBean resultBean = new ResultBean();
        try {
            client = opcUaConfig.createClient();
            // 同步建立连接
            client.connect().get();
            // call the sqrt(x) function
            String val = "On";
            on(client).exceptionally(ex -> {
                logger.error("error invoking On()", ex);
                resultBean.setSuccess(false);
                resultBean.setResult("error");
                return "error";
//            }).thenAccept(v -> {
            }).thenApplyAsync(v -> {
                logger.info("返回值{}={}", val, v);
                resultBean.setSuccess(true);
                resultBean.setResult(v);
                logger.warn("======resultBean:success:{}; result:{}", resultBean.isSuccess(), resultBean.getResult());
//                future.complete(client);
                return resultBean;
            });
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        logger.warn("resultBean:success:{}; result:{}", resultBean.isSuccess(), resultBean.getResult());
        return resultBean;
    }


    private CompletableFuture<String> on(OpcUaClient client) {
        NodeId objectId = NodeId.parse("ns=2;s=2001");
        NodeId methodId = NodeId.parse("ns=2;s=On.Floodlight");

        CallMethodRequest request = new CallMethodRequest(
                objectId, methodId, new Variant[]{});

        return client.call(request).thenCompose(result -> {
            StatusCode statusCode = result.getStatusCode();

            if (statusCode.isGood()) {
                String value = (String)l(result.getOutputArguments()).get(0).getValue();
                return CompletableFuture.completedFuture(value);
            } else {
                CompletableFuture<String> f = new CompletableFuture<>();
                f.completeExceptionally(new UaException(statusCode));
                return f;
            }
        });
    }

}
