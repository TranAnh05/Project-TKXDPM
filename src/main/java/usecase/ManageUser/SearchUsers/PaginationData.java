package usecase.ManageUser.SearchUsers;

public class PaginationData {
	public final long totalCount; // Tổng số mục (ví dụ: 100 users)
    public final int currentPage; // Trang hiện tại (ví dụ: 1)
    public final int pageSize; // Kích thước trang (ví dụ: 10)
    public final int totalPages; // Tổng số trang (ví dụ: 10)

    public PaginationData(long totalCount, int currentPage, int pageSize) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        // Tính toán tổng số trang
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }
}
