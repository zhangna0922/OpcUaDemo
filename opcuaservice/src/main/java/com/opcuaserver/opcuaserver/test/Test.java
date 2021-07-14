//package com.opcuaserver.opcuaserver.test;
//
//import com.opcuaserver.opcuaserver.ExampleRunner.ReadExample;
//import com.opcuaserver.opcuaserver.ExampleRunner.Runner;
//import com.opcuaserver.opcuaserver.certificate.CerLoad;
//import com.opcuaserver.opcuaserver.certificate.DerPemLoadImp;
//import com.opcuaserver.opcuaserver.certificate.PKCS12LoadImp;
//import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
//import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
//import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
//import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
//import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
//import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
//
//import java.io.File;
//import java.security.KeyPair;
//import java.security.cert.X509Certificate;
//import java.util.concurrent.ExecutionException;
//
//import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
//
//public class Test {
//    //定义OPC UA服务器的IP地址和端口
//    private static String EndPointUrl = "opc.tcp://zhangna:53530/OPCUA/SimulationServer";
//
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        test1();
//    }
//
//
//    public static void test1(){
//        //定义证书文件路径
//        File cerfile = new File("example-client.pfx");
//
//        CerLoad cerLoad = new PKCS12LoadImp();    //加载pfx证书文件的实例
//        Runner runner = new ReadExample();   //Runner对象
//        //CerLoad cerLoad = new DerPemLoadImp();    加载der和pem证书文件的实例
//        X509Certificate certificate  =cerLoad.getCer(cerfile);  //获取证书对象
//        KeyPair keyPair = cerLoad.getKeyPair(cerfile);          //获取密钥对 对象
//
//        //搜索OPC节点
//        EndpointDescription[] endpointDescriptions = null;
//        System.out.println("开始OPC连接...");
//        try{
//            endpointDescriptions = UaTcpStackClient.getEndpoints(EndPointUrl).get();
//            System.out.println(endpointDescriptions[1]);
//        }catch (Throwable e){
//            System.out.println("获取端点失败");
//            return;
//        }
//
//        EndpointDescription endpointDescription = endpointDescriptions[1];
//
//        //创建OpcUaClientConfig 配置对象
//        OpcUaClientConfig config = OpcUaClientConfig.builder()
//                .setApplicationName(LocalizedText.english("demo"))
//                .setApplicationUri("urn:eclipse:milo:examples:client")
//                .setCertificate(certificate)
//                .setKeyPair(keyPair)
//                .setEndpoint(endpointDescription)
////                .setIdentityProvider(new UsernameProvider("username","1234567890"))
//                .setRequestTimeout(uint(5000))
//                .build();
//
//        //利用OpcUaClientConfig创建OPC UA客户端
//        OpcUaClient opcClient = new OpcUaClient(config);
//
//        try {
//            runner.Run(opcClient);
//        }catch (Exception e){
//            System.out.println("发生异常: "+e);
//        }
//    }
//
//    public static void test2(){
//        OpcUaSimple opcUaSimple = new OpcUaSimple();
//        String endPointUrl = "opc.tcp://zhangna:53530/OPCUA/SimulationServer";
////        EndpointDescription[] endpointDescription = UaTcpStackClient.getEndpoints(endPointUrl).get();
//        //过滤掉不需要的安全策略，选择一个自己需要的安全策略
//       /* EndpointDescription endpoint = Arrays.stream(endpointDescription)
//                .filter(e -> e.getSecurityPolicyUri().equals(securityPolicy.getSecurityPolicyUri()))
//                .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));
//
//
//        OpcUaClientConfig config = OpcUaClientConfig.builder()
//                .setApplicationName(LocalizedText.english("OPCAPP"))
//                .setApplicationUri("urn:LAPTOP-AQ90KJVR:OPCAPP")
////                .setCertificate(certificate)
////                .setKeyPair(keyPair)
//                .setEndpoint(endpoint)
//                .setIdentityProvider(new UsernameProvider("username","password"))
//                .setRequestTimeout(uint(5000))
//                .build();*/
//
//        OpcUaClientConfig clientConfig = OpcUaClientConfig.builder().setEndpointUrl(endPointUrl)
//
//                .setRequestTimeout(uint(5000))
//                .build();
//        OpcUaClient opcClient = new OpcUaClient(clientConfig);
////        opcUaSimple.browseNode(opcClient);
//    }
//}
