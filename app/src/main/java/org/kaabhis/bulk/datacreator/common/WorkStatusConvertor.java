package org.kaabhis.bulk.datacreator.common;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class WorkStatusConvertor implements DynamoDBTypeConverter<String, WorkStatus> {

    @Override
    public String convert(WorkStatus state) {
        return state.name();
    }

    @Override
    public WorkStatus unconvert(String state) {
        return WorkStatus.valueOf(state);
    }
}
