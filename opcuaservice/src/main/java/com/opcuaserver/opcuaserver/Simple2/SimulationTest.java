package com.opcuaserver.opcuaserver.Simple2;

public class SimulationTest {

    public static void main(String[] args) {
        createSubscription();
    }
    public static void  createSubscription(){
        ClientGen clientGen = new ClientGen();
        clientGen.createClient();

        OpcUaOperationSupport opcUaOperationSupport = new OpcUaOperationSupport();

        /*ns=2;s=2001*/
        OpcModel subOpcModel1 = new OpcModel(2,"","2001");
        opcUaOperationSupport.createSubscription(subOpcModel1);
    }
}
