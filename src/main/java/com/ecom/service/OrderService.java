package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.OrderRequest;
import com.ecom.model.RoomOrder;

public interface OrderService {

	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception;

	public List<RoomOrder> getOrdersByUser(Integer userId);

	public RoomOrder updateOrderStatus(Integer id, String status);

	public List<RoomOrder> getAllOrders();

	public RoomOrder getOrdersByOrderId(String orderId);

	public Page<RoomOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);

	public List<RoomOrder> getOrdersByOwnerId(Integer ownerId);
}
