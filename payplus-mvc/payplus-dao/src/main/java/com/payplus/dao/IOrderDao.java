package com.payplus.dao;

import com.payplus.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IOrderDao {
    void insert(PayOrder payOrder);

    PayOrder queryUnpaidOrder(PayOrder payOrder);
}
