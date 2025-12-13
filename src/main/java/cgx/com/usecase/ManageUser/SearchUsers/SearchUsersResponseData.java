package cgx.com.usecase.ManageUser.SearchUsers;

import java.util.List;

import cgx.com.usecase.Interface_Common.PaginationData;
import cgx.com.usecase.ManageUser.UserData;

public class SearchUsersResponseData {
	public boolean success;
    public String message;
    
    // Dữ liệu khi thành công
    public List<UserData> users; // Dùng lại UserData DTO
    public PaginationData pagination;
}
