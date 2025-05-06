package com.payplus.domain.payment.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrepayCommand {
    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal amount;
    private String payMethod;
}
