package com.list.todo.util;

import com.list.todo.entity.BaseEntity;
import com.list.todo.payload.UserSummary;

import java.util.Comparator;

public class UserSummaryComparator implements Comparator<UserSummary> {

    @Override
    public int compare(UserSummary o1, UserSummary o2) {
        return o1.getUsername().compareTo(o2.getUsername());
    }
}
