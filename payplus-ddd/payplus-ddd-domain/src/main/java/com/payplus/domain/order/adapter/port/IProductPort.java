package com.payplus.domain.order.adapter.port;

import com.payplus.domain.order.model.entity.ProductEntity;

public interface IProductPort {
    ProductEntity queryProductByProductId(String productId);
}
