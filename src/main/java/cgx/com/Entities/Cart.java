package cgx.com.Entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private String userId;
    private List<CartItem> items;
    private BigDecimal totalEstimatedPrice; 
    private Instant updatedAt;

 // Constructor 1: Tạo mới (Giỏ rỗng) - Dùng khi mapDataToEntity gặp data=null
    public Cart(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.totalEstimatedPrice = BigDecimal.ZERO;
        this.updatedAt = Instant.now();
    }

    // Constructor 2: Tái tạo (Rehydration) - Dùng trong mapDataToEntity
    public Cart(String userId, List<CartItem> items, BigDecimal totalEstimatedPrice, Instant updatedAt) {
        this.userId = userId;
        this.items = (items != null) ? items : new ArrayList<>();
        this.totalEstimatedPrice = (totalEstimatedPrice != null) ? totalEstimatedPrice : BigDecimal.ZERO;
        this.updatedAt = updatedAt;
    }

    /**
     * LOGIC NGHIỆP VỤ CỐT LÕI: THÊM VÀO GIỎ
     * Flow: Check tồn kho -> Check đã có chưa -> Cộng dồn hoặc Thêm mới -> Tính giá
     * 
     */
    public void addItem(String deviceId, int requestedQuantity, int currentStock, BigDecimal unitPrice) {
        // Tính toán tổng số lượng dự kiến (Scenario Merge)
        int currentQtyInCart = 0;
        Optional<CartItem> existingItemOpt = items.stream()
                .filter(item -> item.getDeviceId().equals(deviceId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            currentQtyInCart = existingItemOpt.get().getQuantity();
        }

        int finalQuantity = currentQtyInCart + requestedQuantity;

        // CHECK TỒN KHO
        if (finalQuantity > currentStock) {
            throw new IllegalArgumentException(
                String.format("Số lượng vượt quá tồn kho. Kho còn: %d, Bạn đã có: %d, Muốn thêm: %d.", 
                currentStock, currentQtyInCart, requestedQuantity)
            );
        }

        // Thực hiện thay đổi dữ liệu
        if (existingItemOpt.isPresent()) {
            // Case: Đã có -> Update số lượng
            existingItemOpt.get().addQuantity(requestedQuantity);
        } else {
            // Case: Chưa có -> Thêm item mới
            items.add(new CartItem(deviceId, requestedQuantity));
        }

        // Cập nhật tổng tiền
        BigDecimal amountToAdd = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
        this.totalEstimatedPrice = this.totalEstimatedPrice.add(amountToAdd);
        
        // Update thời gian
        this.updatedAt = Instant.now();
    }
    
    /**
    * NGHIỆP VỤ: Cập nhật số lượng (Edit Cart)
    */
   public void updateItemQuantity(String deviceId, int newQuantity, int currentStock, BigDecimal currentUnitPrice) {
       // Tìm item trong giỏ
       Optional<CartItem> itemOpt = items.stream()
               .filter(i -> i.getDeviceId().equals(deviceId))
               .findFirst();
       
       CartItem item = itemOpt.get();
       int oldQuantity = item.getQuantity();

       // check tồn kho với sl user chỉ định
       if (newQuantity > currentStock) {
           throw new IllegalArgumentException("Kho chỉ còn " + currentStock + " sản phẩm. Không đủ số lượng yêu cầu.");
       }

       item.setQuantity(newQuantity);

       BigDecimal oldAmount = currentUnitPrice.multiply(BigDecimal.valueOf(oldQuantity));
       BigDecimal newAmount = currentUnitPrice.multiply(BigDecimal.valueOf(newQuantity));
       
       // Tổng mới = Tổng cũ - Tiền cũ + Tiền mới
       this.totalEstimatedPrice = this.totalEstimatedPrice.subtract(oldAmount).add(newAmount);
       
       this.updatedAt = Instant.now();
   }
   
   
   public void removeItem(String deviceId, BigDecimal currentUnitPrice) {
       // Tìm item cần xóa
       Optional<CartItem> itemOpt = items.stream()
               .filter(i -> i.getDeviceId().equals(deviceId))
               .findFirst();

       CartItem itemToRemove = itemOpt.get();
       
       BigDecimal amountToSubtract = currentUnitPrice.multiply(BigDecimal.valueOf(itemToRemove.getQuantity()));
       
       this.totalEstimatedPrice = this.totalEstimatedPrice.subtract(amountToSubtract);

       items.remove(itemToRemove);
       
       this.updatedAt = Instant.now();
   }

    // Getters
    public String getUserId() { return userId; }
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public BigDecimal getTotalEstimatedPrice() { return totalEstimatedPrice; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    // Helper cho UI hiển thị badge
    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}