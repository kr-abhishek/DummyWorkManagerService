package org.kaabhis.bulk.datacreator;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaabhis.bulk.datacreator.common.WorkStatus;
import org.kaabhis.bulk.datacreator.common.WorkUpdate;
import org.kaabhis.bulk.datacreator.data.WorkItem;

import com.amazonaws.endpointdiscovery.DaemonThreadFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataService {

    private static final int STREAMS = 10;
    private static final int KEY_RANGE = 1000;

    private final DynamoDBMapper ddbMapper;
    private final AmazonDynamoDB ddbClient;

    private final ExecutorService executor;

    public DataService(boolean isProd) {
        if (isProd) {
            this.ddbClient = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_WEST_2)
                    .build();
        } else {
            this.ddbClient = DynamoDBEmbedded.create().amazonDynamoDB();
        }
        this.ddbMapper = new DynamoDBMapper(ddbClient);
        this.executor = Executors.newFixedThreadPool(STREAMS, DaemonThreadFactory.INSTANCE);
        initTable();
        initTableInspector();
    }

    public void initStreams() {

        for (int i = 0; i < STREAMS; i++) {
            final Integer innerI = i;
            String streamName = "Stream_" + innerI;
            int offset = innerI * KEY_RANGE / STREAMS;
            executor.submit(new StreamTask(streamName, offset, (int) KEY_RANGE / STREAMS));
        }
    }

    protected void initTableInspector() {
        executor.submit(new TableInspectorTask());
    }

    protected class TableInspectorTask implements Runnable {
        @Override
        public void run() {
            Scanner myObj = new Scanner(System.in);
            while (true) {
                try {
                    System.out.println("Enter work index:: ");
                    String indexS = myObj.nextLine();
                    log.info("1. Querying index:: " + indexS);

                    Long indexL = Long.parseLong(indexS);
                    log.info("2. Querying index:: " + indexL);
                    String formattedKey = "index_" + indexL;

                    log.info("3. Querying index:: " + formattedKey);
                    WorkItem currItem = ddbMapper.load(WorkItem.class, formattedKey);
                    log.info("Work Details:: " + currItem);
                    System.out.println("Work Details:: " + currItem);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.warn("Data Inspector exiting....\n Bye");
                    System.out.println("Data Inspector exiting....\n Bye");
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Unknown Exception Trace: " + e);
                    System.out.println("Unknown Exception Trace: " + e);
                    // myObj.close();
                }
            }
        }
    }

    @AllArgsConstructor
    protected class StreamTask implements Runnable {

        private final String streamName;
        private final int keyOffset;
        private final int keySpace;

        @Override
        public void run() {
            while (true) {
                int innerKey = (int) (Math.random() * keySpace);
                String key = "index_" + (innerKey + keyOffset);
                try {
                    WorkUpdate workUpdate = new WorkUpdate(key, WorkStatus.nextState(), streamName);

                    updateWorkStatus(workUpdate);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.warn("Data Stream interrupted: " + streamName);
                } catch (Exception e) {
                    log.error("Unknown Exception Trace: " + e);
                    throw e;
                }
            }
        }

        protected void updateWorkStatus(WorkUpdate update) {
            WorkItem currItem = ddbMapper.load(WorkItem.class, update.getKey());
            if (null == currItem) {
                currItem = new WorkItem(update.getKey(), update.getNewStatus().name(), update.getNewStatus(),
                        streamName, null);
                ddbMapper.save(currItem);
            } else {
                currItem.setValue(currItem.getValue() + "->" + update.getNewStatus());
                currItem.setState(update.getNewStatus());
                ddbMapper.save(currItem);
            }
            log.info("Thread = " + streamName + ", Update: [key:" + currItem.getKey() + "] " + currItem);
            // System.out.println("Thread = " + streamName + ", Update: [key:" +
            // currItem.getKey() + "] " + currItem);
        }
    }

    private void initTable() {
        try {
            describeTable();
        } catch (ResourceNotFoundException e) {
            log.info("Table Not Found. Creating...");
            createTable();
        }
    }

    private void createTable() {
        ProvisionedThroughput throughput = new ProvisionedThroughput(10L, 10L);

        try {
            CreateTableRequest request = ddbMapper.generateCreateTableRequest(WorkItem.class);
            request.withProvisionedThroughput(throughput);
            CreateTableResult result = ddbClient.createTable(request);

            TableDescription tableInfo = result.getTableDescription();
            System.out.println("Created DDB Table: " + tableInfo.getTableArn());

        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private TableDescription describeTable() {
        try {
            DescribeTableRequest request = new DescribeTableRequest(WorkItem.TABLE_NAME);
            DescribeTableResult tableInfo = ddbClient.describeTable(request);
            System.out.println("Found DDB Table: " + tableInfo.getTable().getTableArn());

            return tableInfo.getTable();
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (AmazonDynamoDBException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
