package com.hil.gatewayms.infra.async;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAsyncApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestAsyncApi.class);
    public static final int MAX_VAL = 25;


    public static void main(String[] args) {
        runCollectorConsumerAsync();

    }


    public static void runCollectorConsumerAsync() {

        try {
            LOG.debug("-----------------------------Start TEST----------------------------------");

            long startTime = new Date().getTime();

            List<String> inputData = List.of("unu", "doi", "trei");

            CompletableFuture<Void> isDone = new CompletableFuture<Void>();

            //temperature
            List<CompletionStage<Void>> listSubscribeCs = new ArrayList<CompletionStage<Void>>(inputData.size());
            for (String xPath : inputData) {

                CompletionStage<Void> subscribeCs = api.subscribeAsync(xPath, () -> {

                    List<CompletionStage<String>> listReadCs = new ArrayList<CompletionStage<String>>(inputData.size());
                    for (String tempXpath : inputData) {
                        CompletionStage<String> readCs = api.readAsync(tempXpath);
                        listReadCs.add(readCs);
                    }
                    CompletableFuture<Void> listReadCsDone = CompletableFuture.allOf(listReadCs.toArray(new CompletableFuture[listReadCs.size()]));
                    listReadCsDone.thenAccept(res -> {

                        int total = 0;
                        for (CompletionStage<String> readCs : listReadCs) {
                            String value = readCs.toCompletableFuture().join();
                            int intValue = Integer.valueOf(value);
                            total += intValue;
                        }
                        int avgTemp = total / inputData.size();
                        LOG.debug("Average Temperature = " + avgTemp);
                        if (avgTemp > MAX_VAL) {
                            LOG.debug("Average Temperature THRESHOLD");
                        }
                    });
                });

                listSubscribeCs.add(subscribeCs);
            }

            CompletableFuture<Void> listSubscribeCsDone = CompletableFuture
                .allOf(listSubscribeCs.toArray(new CompletableFuture[listSubscribeCs.size()]));
            listSubscribeCsDone.get();

            isDone.get();

            LOG.debug("-----------------------------END TEST----------------------------------");
            LOG.debug("Duration " + ((new Date()).getTime() - startTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
