package cgx.com.usecase.ManageUser.SearchUsers;

public class SearchUsersRequestData {
	public final String authToken; // Token của Admin
    
    // Tiêu chí tìm kiếm & Phân trang
    public final String searchTerm; // Từ khóa (email hoặc name)
    public final int pageNumber; // Số trang (bắt đầu từ 1)
    public final int pageSize; // Kích thước trang

    public SearchUsersRequestData(String authToken, String searchTerm, int pageNumber, int pageSize) {
        this.authToken = authToken;
        this.searchTerm = searchTerm;
        this.pageNumber = (pageNumber <= 0) ? 1 : pageNumber; // Đảm bảo trang > 0
        this.pageSize = (pageSize <= 0) ? 10 : pageSize; // Kích thước mặc định
    }
}
