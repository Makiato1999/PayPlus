package com.payplus.dao;

import com.payplus.domain.po.PayOrder;

public interface IOrderDao {
    void insert(PayOrder payOrder);
}
