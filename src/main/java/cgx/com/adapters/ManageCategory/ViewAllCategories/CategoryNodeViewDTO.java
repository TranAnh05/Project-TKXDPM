package cgx.com.adapters.ManageCategory.ViewAllCategories;

import java.util.ArrayList;
import java.util.List;

public class CategoryNodeViewDTO {
	public String id;
    public String name;
    public String description;
    public List<CategoryNodeViewDTO> children = new ArrayList<>();
}
