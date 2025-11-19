package usecase.ManageCategory;

import java.time.Instant;

public class CategoryData {
	public String categoryId;
    public String name;
    public String description;
    public String parentCategoryId;
    public Instant createdAt;
    public Instant updatedAt;

    public CategoryData() {}

    public CategoryData(String categoryId, String name, String description, String parentCategoryId, 
                        Instant createdAt, Instant updatedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
