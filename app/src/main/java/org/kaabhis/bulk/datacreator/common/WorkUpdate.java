package org.kaabhis.bulk.datacreator.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WorkUpdate {
    private String key;
    private WorkStatus newStatus;
    private String streamInfo;
}