package com.list.todo.util;

import com.list.todo.entity.BaseEntity;

import java.util.Comparator;

public class IdComparator implements Comparator<BaseEntity> {

    @Override
    public int compare(BaseEntity o1, BaseEntity o2) {
        if (o1.getId().equals(o2.getId())) {
            return 0;
        } else if (o1.getId() < o2.getId()) {
            return -1;
        } else {
            return 1;
        }
    }
}
