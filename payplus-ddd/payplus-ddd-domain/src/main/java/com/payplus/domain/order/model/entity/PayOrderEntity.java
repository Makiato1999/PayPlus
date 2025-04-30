package com.payplus.domain.order.model.entity;

import com.payplus.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderEntity {
    private String userId;
    private String orderId;
    private String payUrl;
    private OrderStatusVO orderStatusVO;
}
