package usecase.ManageUser.SearchUsers;

/**
 * DTO (Data Transfer Object)
 * Dùng để đóng gói các tiêu chí tìm kiếm User.
 * Use Case sẽ tạo DTO này và truyền nó cho Repository.
 */
public class UserSearchCriteria {
    // Tìm kiếm theo "từ khóa" chung (có thể là email hoặc name)
    private final String searchTerm;

    public UserSearchCriteria(String searchTerm) {
        this.searchTerm = (searchTerm == null) ? "" : searchTerm.trim();
    }
    
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.isEmpty();
    }
}