package com.payplus.infrastructure.adapter.port;

import com.payplus.domain.order.adapter.port.IProductPort;
import com.payplus.domain.order.model.entity.ProductEntity;
import com.payplus.infrastructure.gateway.ProductRPC;
import com.payplus.infrastructure.gateway.dto.ProductDTO;

public class ProductPort implements IProductPort {
    private final ProductRPC productRPC;

    public ProductPort(ProductRPC productRPC) {
        this.productRPC = productRPC;
    }

    @Override
    public ProductEntity queryProductByProductId(String productId) {
        ProductDTO productDTO = productRPC.queryProductByProductId(productId);
        return ProductEntity.builder()
                .productId(productDTO.getProductId())
                .productName(productDTO.getProductName())
                .productDesc(productDTO.getProductDesc())
                .price(productDTO.getPrice())
                .build();
    }
}
