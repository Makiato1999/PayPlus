package com.payplus.api;

import com.payplus.api.dto.CreatePayRequestDTO;
import com.payplus.api.response.Response;

public interface IPayService {
    Response<String> createPayOrder(CreatePayRequestDTO createPayRequestDTO);
}
