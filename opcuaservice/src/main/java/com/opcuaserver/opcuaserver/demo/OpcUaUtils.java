package com.opcuaserver.opcuaserver.demo;

import com.opcuaserver.opcuaserver.Simple2.ClientGen;
import com.opcuaserver.opcuaserver.Simple2.OpcModel;
import com.opcuaserver.opcuaserver.Simple2.ResultBean;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class OpcUaUtils {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaUtils.class);

    public static ResultBean writePLC(OpcModel server, String editValue) {
        ResultBean resultBean = new ResultBean(false, "");
        String accessPath = server.getAccessPath();
        String itemName = server.getItemName();
        String s = StringUtils.hasText(itemName) ? "." : "";
        String item = accessPath + s + itemName;
        try {
//          创建连接
            OpcUaConfig opcUaConfig = new OpcUaConfig();
            OpcUaClient client = opcUaConfig.createClient();
            // 同步建立连接
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

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultBean;
    }

    public static ResultBean readPLC(OpcModel server) {
        ResultBean resultBean = new ResultBean(false, "");
        String accessPath = server.getAccessPath();
        String itemName = server.getItemName();
        String s = StringUtils.hasText(itemName) ? "." : "";
        String item = accessPath + s + itemName;
        try {
            OpcUaConfig opcUaConfig = new OpcUaConfig();
            OpcUaClient client = opcUaConfig.createClient();
            // 同步建立连接
            client.connect().get();
            NodeId nodeId = new NodeId(server.getNamespaceIndex(), item);
            CompletableFuture<DataValue> readValue = client.readValue(0.0, TimestampsToReturn.Both, nodeId);
            DataValue value = readValue.get();
            logger.warn("DataValue---{}",value.toString());
            String plcValue = value.getValue().toString();
            assert value.getStatusCode() != null;
            String statusCode = value.getStatusCode().toString();
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

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultBean;
    }

    public static OpcModel genOpcModel(String nodeId, String status) {
        String[] split = nodeId.split(";");
        String index1 = split[0];
        Integer nameIndex = Integer.parseInt(index1.replace("ns=", ""));
        String accessPath = split[1].replace("i=", "");
        int index = Integer.parseInt(accessPath);
        if (index == 2001) {
            accessPath = "Floodlight";
        } else if (index == 2002) {
            accessPath = "Strobelight";
        } else if (index == 2003) {
            accessPath = "Siren";
        } else if (index > 2100) {
            int i = index - 2100;
            String s = String.valueOf(i);
            if (s.length() < 3) {
                accessPath = "Zone" + (s.length() == 1 ? "00" : "0") + s;
            } else if (s.length() == 3) {
                accessPath = "Zone" + s;
            } else {
                logger.error("未知情况，暂不处理nodeId={}", nodeId);
                return null;
            }
        } else {
            logger.error("未知情况，暂不处理nodeId={}", nodeId);
            return null;
        }

        return new OpcModel(nameIndex, status, accessPath);
    }
}
