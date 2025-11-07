package infrastructure.web.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import adapters.ManageUser.BlockUser.BlockUserPresenter;
import adapters.ManageUser.BlockUser.BlockUserViewModel;
import adapters.ManageUser.UnblockUser.UnblockUserPresenter;
import adapters.ManageUser.UnblockUser.UnblockUserViewModel;
import adapters.ManageUser.UpdateUserRole.UpdateUserRolePresenter;
import adapters.ManageUser.UpdateUserRole.UpdateUserRoleViewModel;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersPresenter;
import adapters.ManageUser.ViewAllUsers.ViewAllUsersViewModel;
import adapters.SearchUsers.SearchUsersPresenter;
import adapters.SearchUsers.SearchUsersViewModel;
import application.dtos.ManageUser.BlockUser.BlockUserInputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserInputData;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleInputData;
import application.dtos.SearchUsers.SearchUsersInputData;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageUser.BlockUser.BlockUserUsecase;
import application.usecases.ManageUser.UnblockUser.UnblockUserUsecase;
import application.usecases.ManageUser.UpdateUserRole.UpdateUserRoleUsecase;
import application.usecases.ManageUser.ViewAllUsers.ViewAllUsersUsecase;
import application.usecases.SearchUsers.SearchUsersUsecase;
import infrastructure.database.UserRepositoryImpl;
import infrastructure.web.requests.UpdateUserRole.UpdateUserRoleRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * API (Tầng 1) cho "Quản lý Người dùng"
 * URL: /api/admin/users/*
 */
@WebServlet("/api/admin/users/*")
public class UserRestfulWS extends HttpServlet{
	private final Gson gson = new Gson();
    private UserRepository userRepo;

    @Override
    public void init() throws ServletException {
        super.init();
        // Lắp ráp Tầng 1 (Database)
        this.userRepo = new UserRepositoryImpl();
    }
    
    /**
     * Xử lý HTTP GET (Xem tất cả HOẶC Tìm kiếm)
     * 1. GET /api/admin/users (Xem tất cả)
     * 2. GET /api/admin/users?search=test.com (Tìm kiếm)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Lấy query parameter "?search=..."
        String keyword = req.getParameter("search"); 

        if (keyword == null || keyword.isEmpty()) {
            // Không có param 'search' -> Admin muốn "Xem tất cả" (Lát cắt 10)
            handleViewAllUsers(resp);
        } else {
            // Có param 'search' -> Admin muốn "Tìm kiếm" (Lát cắt 17)
            handleSearchUsers(resp, keyword);
        }
    }
    
    /**
     * HÀM HELPER 1 (cho doGet)
     * Xử lý logic cho "Xem tất cả" (Lát cắt 10)
     */
    private void handleViewAllUsers(HttpServletResponse resp) throws IOException {
        ViewAllUsersViewModel responseModel;
        try {
            // --- Lắp ráp T2, T3 ---
            ViewAllUsersViewModel viewModel = new ViewAllUsersViewModel();
            ViewAllUsersPresenter presenter = new ViewAllUsersPresenter(viewModel);
            ViewAllUsersUsecase useCase = new ViewAllUsersUsecase(this.userRepo, presenter);
            
            useCase.execute(); // --- Gọi Tầng 3 ---
            
            responseModel = presenter.getViewModel(); // --- Lấy Response ---
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new ViewAllUsersViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }

    /**
     * HÀM HELPER 2 (cho doGet)
     * Xử lý logic cho "Tìm kiếm" (Lát cắt 17)
     */
    private void handleSearchUsers(HttpServletResponse resp, String keyword) throws IOException {
        SearchUsersViewModel responseModel;
        try {
            // --- Lắp ráp T2, T3 ---
            SearchUsersViewModel viewModel = new SearchUsersViewModel();
            SearchUsersPresenter presenter = new SearchUsersPresenter(viewModel);
            SearchUsersUsecase useCase = new SearchUsersUsecase(this.userRepo, presenter);
            
            // --- Chuẩn bị & Gọi Tầng 3 ---
            SearchUsersInputData input = new SearchUsersInputData(keyword);
            useCase.execute(input);
            
            responseModel = presenter.getViewModel(); // --- Lấy Response ---
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new SearchUsersViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP PUT (Sửa Role) - /api/admin/users/{id}/role
     * (Lát cắt 11)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Phân tích URL mới
        ParsedURL parsedUrl = new ParsedURL(req);

        if (parsedUrl.id == -1) {
            // Lỗi: URL không có ID
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/users/1/role)");
            return;
        }
        if (parsedUrl.action == null) {
            // Lỗi: URL thiếu hành động
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "URL không hợp lệ. Thiếu hành động (ví dụ: /role hoặc /block)");
            return;
        }

        // 2. Phân luồng (dispatch) dựa trên hành động (action)
        switch (parsedUrl.action) {
            case "role":
                handleUpdateUserRole(req, resp, parsedUrl.id);
                break;
            case "block":
                handleBlockUser(req, resp, parsedUrl.id);
                break;
            case "unblock":
                handleUnblockUser(req, resp, parsedUrl.id);
                break;
            default:
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Hành động '" + parsedUrl.action + "' không được hỗ trợ.");
                break;
        }
    }
    
    private void handleBlockUser(HttpServletRequest req, HttpServletResponse resp, int userIdToBlock) throws IOException {
    	BlockUserViewModel responseModel;
        try {
            // (Request này không cần body)

            // Lắp ráp T2, T3
            BlockUserViewModel viewModel = new BlockUserViewModel();
            BlockUserPresenter presenter = new BlockUserPresenter(viewModel);
            BlockUserUsecase useCase = new BlockUserUsecase(this.userRepo, presenter);

            // TODO: Lấy ID Admin hiện tại (đang gán cứng là 1)
            int currentAdminId = 1;

            // Chuẩn bị & Gọi Tầng 3
            BlockUserInputData input = new BlockUserInputData(userIdToBlock, currentAdminId);
            useCase.execute(input);

            // Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định: " + e.getMessage());
        }
	}

	private void handleUpdateUserRole(HttpServletRequest req, HttpServletResponse resp, int userIdToUpdate) throws IOException {
		UpdateUserRoleViewModel responseModel;
        try {
            // Đọc JSON Request (Chỉ cho 'role')
            UpdateUserRoleRequest requestBody = gson.fromJson(req.getReader(), UpdateUserRoleRequest.class);
            if (requestBody == null || requestBody.newRole == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body (JSON) bị rỗng hoặc thiếu 'newRole'.");
                return;
            }

            // Lắp ráp T2, T3
            UpdateUserRoleViewModel viewModel = new UpdateUserRoleViewModel();
            UpdateUserRolePresenter presenter = new UpdateUserRolePresenter(viewModel);
            UpdateUserRoleUsecase useCase = new UpdateUserRoleUsecase(this.userRepo, presenter);

            // TODO: Lấy ID Admin hiện tại (đang gán cứng là 1)
            int currentAdminId = 1; 

            // Chuẩn bị & Gọi Tầng 3
            UpdateUserRoleInputData input = new UpdateUserRoleInputData(
                userIdToUpdate, requestBody.newRole, currentAdminId
            );
            useCase.execute(input);

            // Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (JsonSyntaxException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON request không hợp lệ hoặc sai cú pháp.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định: " + e.getMessage());
        }
	}
	
	private void handleUnblockUser(HttpServletRequest req, HttpServletResponse resp, int userIdToUnblock) throws IOException {
        UnblockUserViewModel responseModel; // (ViewModel T2 "toàn string")
        try {
            // (Request này không cần body)

            // 1. Lắp ráp T2, T3
            UnblockUserViewModel viewModel = new UnblockUserViewModel();
            UnblockUserPresenter presenter = new UnblockUserPresenter(viewModel);
            UnblockUserUsecase useCase = new UnblockUserUsecase(this.userRepo, presenter); // (Tên Usecase T3 của bạn)

            // 2. Chuẩn bị & Gọi Tầng 3
            UnblockUserInputData input = new UnblockUserInputData(userIdToUnblock);
            useCase.execute(input);

            // 3. Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 (Lỗi nghiệp vụ)
            } else {
                resp.setStatus(HttpServletResponse.SC_OK); // 200
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            // (Chúng ta dùng sendErrorResponse thay vì lặp code)
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định: " + e.getMessage());
        }
    }

	/**
     * HÀM HELPER (PRIVATE) ĐỂ PHÂN TÍCH URL
     */
    private class ParsedURL {
        public int id = -1;
        public String action = null; // "role", "block", "unblock"

        public ParsedURL(HttpServletRequest req) {
            String pathInfo = req.getPathInfo(); // ví dụ: "/1/role" or "/1"
            if (pathInfo == null || pathInfo.equals("/")) {
                return; // Không có ID
            }
            
            // Tách chuỗi: ["", "1", "role"]
            String[] pathParts = pathInfo.split("/");
            
            try {
                if (pathParts.length > 1) {
                    this.id = Integer.parseInt(pathParts[1]); // Lấy ID ("1")
                }
                if (pathParts.length > 2) {
                    this.action = pathParts[2].toLowerCase(); // Lấy action ("role")
                }
            } catch (NumberFormatException e) {
                this.id = -1; // URL không hợp lệ (ví dụ: /abc/role)
            }
        }
    }
    
    /**
     * HÀM HELPER (PRIVATE) ĐỂ GỬI LỖI (Tránh lặp code)
     */
    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        // (Chúng ta dùng Map để tạo JSON lỗi chung)
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("success", "false");
        errorResponse.put("message", message);
        
        resp.setStatus(statusCode);
        resp.getWriter().write(gson.toJson(errorResponse));
    }

}
