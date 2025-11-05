package application.dtos.ManageOrder;

import java.time.LocalDateTime;

import domain.entities.OrderStatus;

public class OrderOutputData {
	public int id;
    public int userId;
    public String userEmail; // <-- Đã được làm giàu
    public LocalDateTime orderDate; // <-- Giữ kiểu gốc, Presenter sẽ format
    public double totalAmount;
    public OrderStatus status; // <-- Giữ kiểu Enum
}
