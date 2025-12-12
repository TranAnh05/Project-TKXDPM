package cgx.com.Entities;

import java.time.Instant;

public class Category {
	private String categoryId;
    private String name;
    private String description;
    private String parentCategoryId;
    private Instant createdAt;
    private Instant updatedAt;
    
    public Category(String categoryId, String name, String description, String parentCategoryId) {
    	 this.categoryId = categoryId;
         this.name = name;
         this.description = description;
         this.parentCategoryId = parentCategoryId;
         this.createdAt = Instant.now();
         this.updatedAt = Instant.now();
    }

    public Category(String categoryId, String name, String description, String parentCategoryId, 
                    Instant createdAt, Instant updatedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- VALIDATION LOGIC ---

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Tên danh mục phải có ít nhất 2 ký tự.");
        }
    }

    // --- GETTERS ---
    public String getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getParentCategoryId() { return parentCategoryId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
