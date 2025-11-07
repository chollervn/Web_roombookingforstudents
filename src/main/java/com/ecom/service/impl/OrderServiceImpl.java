package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.RoomBooking;
import com.ecom.model.RoomOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.RoomBookingRepository;
import com.ecom.repository.RoomOrderRepository;
import com.ecom.repository.RoomRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private RoomOrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private RoomBookingRepository roomBookingRepository;

	@Override
	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {

		List<Cart> carts = cartRepository.findByUserId(userid);

		for (Cart cart : carts) {

			RoomOrder order = new RoomOrder();

			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setRoom(cart.getRoom());
			order.setPrice(cart.getRoom().getMonthlyRent());

			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());

			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());


			order.setOrderAddress(address);

			RoomOrder saveOrder = orderRepository.save(order);

			// Clear cart after saving order
			resetCart(cart.getUser());



		}
	}

	private void resetCart(UserDtls user) {
		cartRepository.deleteByUser(user);
	}

	@Override
	public List<RoomOrder> getOrdersByUser(Integer userId) {
		List<RoomOrder> orders = orderRepository.findByUserId(userId);
		return orders;
	}

	@Override
	public RoomOrder updateOrderStatus(Integer id, String status) {
		Optional<RoomOrder> orderOpt = orderRepository.findById(id);
		if (orderOpt.isPresent()) {
			RoomOrder order = orderOpt.get();
			order.setStatus(status);

			if ("CANCELLED".equals(status)) {
				// Khi user hủy thuê, phòng chuyển ACTIVE
				if (order.getRoom() != null) {
					order.getRoom().setStatus("ACTIVE");
					order.getRoom().setIsAvailable(true);
					roomRepository.save(order.getRoom());
				}

				// Đồng bộ với RoomBooking
				RoomBooking booking = roomBookingRepository.findByUserIdAndRoomId(
					order.getUser().getId(), order.getRoom().getId());
				if (booking != null) {
					booking.setStatus("CANCELLED");
					roomBookingRepository.save(booking);
				}
			}
			return orderRepository.save(order);
		}
		return null;
	}

	@Override
	public List<RoomOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<RoomOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);

	}

	@Override
	public RoomOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

	@Override
	public List<RoomOrder> getOrdersByOwnerId(Integer ownerId) {
		return orderRepository.findByRoomOwnerId(ownerId);
	}

	@Override
	public RoomOrder saveRoomOrder(RoomOrder roomOrder) {
		return orderRepository.save(roomOrder);
	}

}
