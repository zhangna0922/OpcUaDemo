/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.opcuaserver.opcuaserver.example;

import org.eclipse.milo.opcua.sdk.client.DataTypeTreeBuilder;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.methods.UaMethod;
import org.eclipse.milo.opcua.sdk.client.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.core.DataTypeTree;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MethodExample2 implements ClientExample {

    public static void main(String[] args) throws Exception {
        MethodExample2 example = new MethodExample2();

        new ClientExampleRunner(example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        UaObjectNode objectNode = client.getAddressSpace()
            .getObjectNode(NodeId.parse("ns=2;s=HelloWorld"));

        UaMethod sqrtMethod = objectNode.getMethod("sqrt(x)");

        logArguments(client, sqrtMethod);

        Variant[] inputs = {new Variant(16.0)};
        Variant[] outputs = sqrtMethod.call(inputs);

        logger.info("Input values: " + Arrays.toString(inputs));
        logger.info("Output values: " + Arrays.toString(outputs));

        future.complete(client);
    }

    private void logArguments(OpcUaClient client, UaMethod method) throws UaException {
        DataTypeTree dataTypeTree = DataTypeTreeBuilder.build(client);
        Argument[] inputArguments = method.getInputArguments();
        Argument[] outputArguments = method.getOutputArguments();

        logArguments(inputArguments, dataTypeTree, true);
        logArguments(outputArguments, dataTypeTree, false);
    }

    private void logArguments(Argument[] arguments, DataTypeTree dataTypeTree, boolean input) {
        if (arguments.length == 0) {
            logger.info("{} arguments: none", input ? "Input" : "Output");
        } else {
            logger.info("{} arguments:", input ? "Input" : "Output");
            for (Argument argument : arguments) {
                NodeId dataTypeId = argument.getDataType();
                DataTypeTree.DataType dataType = dataTypeTree.getDataType(dataTypeId);
                assert dataType != null;
                String dataTypeName = dataType.getBrowseName().getName();

                logger.info("  {}: {} ({})",
                    argument.getName(),
                    dataTypeName,
                    dataTypeId.toParseableString()
                );
            }
        }
    }

}
