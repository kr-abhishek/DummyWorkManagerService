package org.kaabhis.bulk.datacreator.data;

import org.kaabhis.bulk.datacreator.common.WorkStatus;
import org.kaabhis.bulk.datacreator.common.WorkStatusConvertor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamoDBTable(tableName = "WorkStatusV2")
public class WorkItem {

    @DynamoDBIgnore
    public static final String TABLE_NAME = "WorkStatusV2";

    @DynamoDBHashKey
    private String key;

    @DynamoDBAttribute
    private String value;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = WorkStatusConvertor.class)
    private WorkStatus state;

    @DynamoDBAttribute
    private String streamInfo;

    @DynamoDBVersionAttribute
    private Long rvn;
}
