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
        // 1. Validate Input cơ bản
        if (requestedQuantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        
        // 2. Tính toán tổng số lượng dự kiến (Scenario Merge)
        int currentQtyInCart = 0;
        Optional<CartItem> existingItemOpt = items.stream()
                .filter(item -> item.getDeviceId().equals(deviceId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            currentQtyInCart = existingItemOpt.get().getQuantity();
        }

        int finalQuantity = currentQtyInCart + requestedQuantity;

        // 3. CHECK TỒN KHO THỰC TẾ (Business Rule Quan Trọng Nhất)
        // Kịch bản: Kho có 5, Giỏ có 3, Muốn thêm 3 -> Tổng 6 > 5 -> Lỗi
        if (finalQuantity > currentStock) {
            throw new IllegalArgumentException(
                String.format("Số lượng vượt quá tồn kho. Kho còn: %d, Bạn đã có: %d, Muốn thêm: %d.", 
                currentStock, currentQtyInCart, requestedQuantity)
            );
        }

        // 4. Thực hiện thay đổi dữ liệu (State Change)
        if (existingItemOpt.isPresent()) {
            // Case: Đã có -> Update số lượng
            existingItemOpt.get().addQuantity(requestedQuantity);
        } else {
            // Case: Mới -> Thêm item mới
            items.add(new CartItem(deviceId, requestedQuantity));
        }

        // 5. Cập nhật tổng tiền tạm tính (Snapshot)
        // Logic: Cộng thêm giá trị của lượng vừa thêm vào tổng cũ
        BigDecimal amountToAdd = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
        this.totalEstimatedPrice = this.totalEstimatedPrice.add(amountToAdd);
        
        // 6. Update thời gian
        this.updatedAt = Instant.now();
    }
    
    /**
    * NGHIỆP VỤ: Cập nhật số lượng (Edit Cart)
    */
   public void updateItemQuantity(String deviceId, int newQuantity, int currentStock, BigDecimal currentUnitPrice) {
       if (newQuantity <= 0) {
           throw new IllegalArgumentException("Số lượng phải lớn hơn 0. Hãy dùng chức năng Xóa nếu muốn bỏ sản phẩm.");
       }

       // 1. Tìm item trong giỏ
       Optional<CartItem> itemOpt = items.stream()
               .filter(i -> i.getDeviceId().equals(deviceId))
               .findFirst();

       if (itemOpt.isPresent()) {
           CartItem item = itemOpt.get();
           int oldQuantity = item.getQuantity();

           // 2. CHECK TỒN KHO: Số lượng MỚI có vượt quá kho không?
           // (Khách muốn set thành 10, kho có 8 -> Lỗi)
           if (newQuantity > currentStock) {
               throw new IllegalArgumentException("Kho chỉ còn " + currentStock + " sản phẩm. Không đủ số lượng yêu cầu.");
           }

           // 3. Cập nhật số lượng
           item.setQuantity(newQuantity);

           // 4. Tính toán lại Tổng tiền (Estimate)
           // Logic: Trừ đi tiền của số lượng cũ, cộng tiền của số lượng mới
           // (Dùng giá hiện tại để đảm bảo tính thực tế)
           BigDecimal oldAmount = currentUnitPrice.multiply(BigDecimal.valueOf(oldQuantity));
           BigDecimal newAmount = currentUnitPrice.multiply(BigDecimal.valueOf(newQuantity));
           
           // Tổng mới = Tổng cũ - Tiền cũ + Tiền mới
           this.totalEstimatedPrice = this.totalEstimatedPrice.subtract(oldAmount).add(newAmount);
           
           // Đảm bảo không bị âm (do sai số float/double nếu có, dù dùng BigDecimal thì hiếm)
           if (this.totalEstimatedPrice.compareTo(BigDecimal.ZERO) < 0) {
               this.totalEstimatedPrice = BigDecimal.ZERO;
           }

           this.updatedAt = Instant.now();
       } else {
           throw new IllegalArgumentException("Sản phẩm không tìm thấy trong giỏ hàng.");
       }
   }
   
   
   public void removeItem(String deviceId, BigDecimal currentUnitPrice) {
       // 1. Tìm item cần xóa
       Optional<CartItem> itemOpt = items.stream()
               .filter(i -> i.getDeviceId().equals(deviceId))
               .findFirst();

       if (itemOpt.isPresent()) {
           CartItem itemToRemove = itemOpt.get();
           
           // 2. Tính toán số tiền cần trừ
           // Logic: Tổng tiền mới = Tổng cũ - (Số lượng đang có * Giá hiện tại)
           BigDecimal amountToSubtract = currentUnitPrice.multiply(BigDecimal.valueOf(itemToRemove.getQuantity()));
           
           this.totalEstimatedPrice = this.totalEstimatedPrice.subtract(amountToSubtract);
           
           // Đảm bảo không âm (Safety check)
           if (this.totalEstimatedPrice.compareTo(BigDecimal.ZERO) < 0) {
               this.totalEstimatedPrice = BigDecimal.ZERO;
           }

           // 3. Xóa khỏi danh sách
           items.remove(itemToRemove);
           
           // 4. Update thời gian
           this.updatedAt = Instant.now();
       } else {
           // Tùy nghiệp vụ: Có thể ném lỗi hoặc lờ đi nếu không tìm thấy.
           // Ở đây ta chọn lờ đi (Idempotent) - nếu đã xóa rồi thì coi như thành công.
       }
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