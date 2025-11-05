package application.dtos.SearchOrders;

public class SearchOrdersInputData {
	public String emailKeyword; // Từ khóa tìm kiếm (email)

    public SearchOrdersInputData(String emailKeyword) {
        this.emailKeyword = emailKeyword;
    }
}
