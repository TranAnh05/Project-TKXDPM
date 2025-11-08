package infrastructure.web.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusPresenter;
import adapters.ManageOrder.UpdateOrderStatus.UpdateOrderStatusViewModel;
import adapters.ManageOrder.ViewAllOrders.ViewAllOrdersPresenter;
import adapters.ManageOrder.ViewAllOrders.ViewAllOrdersViewModel;
import adapters.SearchOrders.SearchOrdersPresenter;
import adapters.SearchOrders.SearchOrdersViewModel;
import application.dtos.ManageOrder.UpdateOrderStatus.UpdateOrderStatusInputData;
import application.dtos.SearchOrders.SearchOrdersInputData;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageUser.UserRepository;
import application.usecases.ManageOrder.UpdateOrderStatus.UpdateOrderStatusUsecase;
import application.usecases.ManageOrder.ViewAllOrders.ViewAllOrdersUsecase;
import application.usecases.SearchOrders.SearchOrdersUsecase;
import infrastructure.database.OrderRepositoryImpl;
import infrastructure.database.UserRepositoryImpl;
import infrastructure.web.requests.UpdateOrderStatus.UpdateOrderStatusRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LỚP 2 (MỚI): API (Tầng 1) cho "Quản lý Đơn hàng"
 * URL: /api/admin/orders/*
 */
@WebServlet("/api/admin/orders/*")
public class OrderRestfulWS extends HttpServlet{
	private final Gson gson = new Gson();
    private OrderRepository orderRepo;
    private UserRepository userRepo; // Cần cho logic "làm giàu"

    @Override
    public void init() throws ServletException {
        super.init();
        // Lắp ráp các Repository Tầng 1
        this.orderRepo = new OrderRepositoryImpl();
        this.userRepo = new UserRepositoryImpl();
    }
    
    /**
     * Xử lý HTTP GET (Xem tất cả)
     * 1. GET /api/admin/orders (Xem tất cả)
     * (Sau này sẽ mở rộng cho Search)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Lấy query parameter "?search=..."
        String keyword = req.getParameter("search"); 

        if (keyword == null || keyword.isEmpty()) {
            // Không có param 'search' -> Admin muốn "Xem tất cả" (Lát cắt 14)
            handleViewAllOrders(resp);
        } else {
            // Có param 'search' -> Admin muốn "Tìm kiếm" (Lát cắt 18)
            handleSearchOrders(resp, keyword);
        }
    }
    
    /**
     * HÀM HELPER 2 (MỚI) (cho doGet)
     * Xử lý logic cho "Tìm kiếm" (Lát cắt 18)
     * @throws IOException 
     */
    private void handleSearchOrders(HttpServletResponse resp, String keyword) throws IOException {
    	SearchOrdersViewModel responseModel; // (ViewModel T2 của Search)
        try {
            // --- Lắp ráp T2, T3 ---
            SearchOrdersViewModel viewModel = new SearchOrdersViewModel();
            SearchOrdersPresenter presenter = new SearchOrdersPresenter(viewModel); // (Tên Presenter T2 của bạn)
            SearchOrdersUsecase useCase = new SearchOrdersUsecase( // (Tên Usecase T3 của bạn)
                this.orderRepo, this.userRepo, presenter
            );
            
            // --- Chuẩn bị & Gọi Tầng 3 ---
            SearchOrdersInputData input = new SearchOrdersInputData(keyword);
            useCase.execute(input);
            
            responseModel = presenter.getViewModel(); // --- Lấy Response ---
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định: " + e.getMessage());
        }
	}

	/**
     * HÀM HELPER 1 (cho doGet)
     * Xử lý logic cho "Xem tất cả" (Lát cắt 14)
     */
    private void handleViewAllOrders(HttpServletResponse resp) throws IOException {
        ViewAllOrdersViewModel responseModel;
        try {
            // --- Lắp ráp T2, T3 ---
            ViewAllOrdersViewModel viewModel = new ViewAllOrdersViewModel();
            ViewAllOrdersPresenter presenter = new ViewAllOrdersPresenter(viewModel);
            ViewAllOrdersUsecase useCase = new ViewAllOrdersUsecase( // (Tên Usecase T3 của bạn)
                this.orderRepo, this.userRepo, presenter
            );
            
            useCase.execute(); // --- Gọi Tầng 3 ---
            
            responseModel = presenter.getViewModel(); // --- Lấy Response ---
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new ViewAllOrdersViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP PUT (Sửa Status) - /api/admin/orders/{id}
     * (Lát cắt 15)
     * (Lưu ý: Chúng ta dùng PUT /api/admin/orders/{id} và truyền status trong body)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        UpdateOrderStatusViewModel responseModel; // (ViewModel T2 "toàn string")
        try {
            // 1. Lấy ID từ URL
            int orderId = getResourceId(req);
            if (orderId == -1) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/orders/1)");
                return;
            }

            // 2. Đọc JSON Request
            UpdateOrderStatusRequest requestBody = gson.fromJson(req.getReader(), UpdateOrderStatusRequest.class);
            if (requestBody == null || requestBody.newStatus == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body (JSON) bị rỗng hoặc thiếu 'newStatus'.");
                return;
            }

            // 3. Lắp ráp T2, T3
            UpdateOrderStatusViewModel viewModel = new UpdateOrderStatusViewModel();
            UpdateOrderStatusPresenter presenter = new UpdateOrderStatusPresenter(viewModel);
            UpdateOrderStatusUsecase useCase = new UpdateOrderStatusUsecase( // (Tên Usecase T3 của bạn)
                this.orderRepo, this.userRepo, presenter
            );

            // 4. Chuẩn bị & Gọi Tầng 3
            UpdateOrderStatusInputData input = new UpdateOrderStatusInputData(
                orderId,           // ID từ URL
                requestBody.newStatus  // Status mới từ Body
            );
            useCase.execute(input);

            // 5. Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 (Lỗi validation/nghiệp vụ)
            } else {
                resp.setStatus(HttpServletResponse.SC_OK); // 200
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (JsonSyntaxException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON request không hợp lệ hoặc sai cú pháp.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống không xác định: " + e.getMessage());
        }
    }

    /**
     * Hàm helper (private) để Gửi Lỗi
     */
	private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
		Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("success", "false");
        errorResponse.put("message", message);
        
        resp.setStatus(statusCode);
        resp.getWriter().write(gson.toJson(errorResponse));
	}

	/**
     * Hàm helper (private) để lấy ID từ URL
     * (ví dụ: /api/admin/orders/1 -> trả về 1)
     */
	private int getResourceId(HttpServletRequest req) {
		try {
            String pathInfo = req.getPathInfo(); // Lấy phần "/1"
            if (pathInfo != null && !pathInfo.equals("/")) {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length > 1) {
                    return Integer.parseInt(pathParts[1]);
                }
            }
        } catch (NumberFormatException e) { /* (URL không phải là số) */ }
        return -1; // Không tìm thấy ID
	}
}
