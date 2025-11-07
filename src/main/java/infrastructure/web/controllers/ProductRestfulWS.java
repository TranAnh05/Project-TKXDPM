package infrastructure.web.controllers;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import adapters.ManageProduct.AddNewProduct.AddNewProductPresenter;
import adapters.ManageProduct.AddNewProduct.AddNewProductViewModel;
import adapters.ManageProduct.DeleteProduct.DeleteProductPresenter;
import adapters.ManageProduct.DeleteProduct.DeleteProductViewModel;
import adapters.ManageProduct.UpdateProduct.UpdateProductPresenter;
import adapters.ManageProduct.UpdateProduct.UpdateProductViewModel;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsPresenter;
import adapters.ManageProduct.ViewAllProducts.ViewAllProductsViewModel;
import adapters.SearchProducts.SearchProductsPresenter;
import adapters.SearchProducts.SearchProductsViewModel;
import application.dtos.ManageProduct.AddNewProduct.AddNewProductInputData;
import application.dtos.ManageProduct.DeleteProduct.DeleteProductInputData;
import application.dtos.ManageProduct.UpdateProduct.UpdateProductInputData;
import application.dtos.SearchProducts.SearchProductsInputData;
import application.factories.ManageProduct.ProductFactory;
import application.ports.out.ManageCategory.CategoryRepository;
import application.ports.out.ManageOrder.OrderRepository;
import application.ports.out.ManageProduct.ProductRepository;
import application.usecases.ManageProduct.AddNewProduct.AddNewProductUsecase;
import application.usecases.ManageProduct.DeleteProduct.DeleteProductUsecase;
import application.usecases.ManageProduct.UpdateProduct.UpdateProductUsecase;
import application.usecases.ManageProduct.ViewAllProducts.ViewAllProductsUsecase;
import application.usecases.SearchProducts.SearchProductsUsecase;
import infrastructure.database.CategoryRepositoryImpl;
import infrastructure.database.OrderRepositoryImpl;
import infrastructure.database.ProductRepositoryImpl;
import infrastructure.web.requests.AddProductRequest.AddProductRequest;
import infrastructure.web.requests.UpdateProduct.UpdateProductRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/admin/products/*")
public class ProductRestfulWS extends HttpServlet{
	private final Gson gson = new Gson();
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private OrderRepository orderRepo;
    private ProductFactory productFactory;
    
    @Override
    public void init() throws ServletException {
        super.init();
        // Lắp ráp các thành phần Tầng 1 và Tầng 3 (Factory)
        this.productRepo = new ProductRepositoryImpl();
        this.categoryRepo = new CategoryRepositoryImpl();
        this.orderRepo = new OrderRepositoryImpl(); // (Cần cho Delete)
        this.productFactory = new ProductFactory();
    }
    
    /**
     * Xử lý HTTP POST (Thêm mới) - /api/admin/products
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        AddNewProductViewModel responseModel; // (ViewModel T2 "toàn string")
        
        try {
            // 1. Đọc JSON Request
            AddProductRequest requestBody = gson.fromJson(req.getReader(), AddProductRequest.class);

            // 2. Validation Tầng 1
            if (requestBody == null) {
                responseModel = new AddNewProductViewModel();
                responseModel.success = "false";
                responseModel.message = "Request body (JSON) bị rỗng.";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 3. Lắp ráp T2, T3
            AddNewProductViewModel viewModel = new AddNewProductViewModel();
            AddNewProductPresenter presenter = new AddNewProductPresenter(viewModel);
            AddNewProductUsecase useCase = new AddNewProductUsecase(
                this.productRepo, this.categoryRepo, presenter, this.productFactory
            );

            // 4. Chuẩn bị & Gọi Tầng 3
            AddNewProductInputData input = new AddNewProductInputData(
                requestBody.name, requestBody.description, requestBody.price, 
                requestBody.stockQuantity, requestBody.imageUrl, requestBody.categoryId,
                requestBody.attributes
            );
            useCase.execute(input);

            // 5. Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 (Lỗi nghiệp vụ)
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (JsonSyntaxException e) {
            // (Xử lý lỗi JSON sai cú pháp)
            responseModel = new AddNewProductViewModel();
            responseModel.success = "false";
            responseModel.message = "JSON request không hợp lệ hoặc sai cú pháp.";
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write(gson.toJson(responseModel));
        } catch (Exception e) {
            // (Xử lý lỗi Hệ thống)
            e.printStackTrace(); // In lỗi ra Console Tomcat
            responseModel = new AddNewProductViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM DO_GET
     * Xử lý logic cho "Xem tất cả"
     * URL: /api/admin/products (Không có ?search=)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
    	req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Lấy query parameter "?search=..."
        String keyword = req.getParameter("search"); 

        if (keyword == null || keyword.isEmpty()) {
            // Không có param 'search' -> Admin muốn "Xem tất cả" (Lát cắt 7)
            handleViewAllProducts(resp);
        } else {
            // Có param 'search' -> Admin muốn "Tìm kiếm" (Lát cắt 16)
            handleSearchProducts(resp, keyword);
        }
    }
    
    private void handleSearchProducts(HttpServletResponse resp, String keyword) throws IOException {
    	SearchProductsViewModel responseModel; // (ViewModel T2 của Search)
        try {
            // --- Lắp ráp T2, T3 ---
            SearchProductsViewModel viewModel = new SearchProductsViewModel();
            SearchProductsPresenter presenter = new SearchProductsPresenter(viewModel);
            SearchProductsUsecase useCase = new SearchProductsUsecase( // (Tên Usecase T3 của bạn)
                this.productRepo, this.categoryRepo, presenter, this.productFactory
            );
            
            // --- Chuẩn bị & Gọi Tầng 3 ---
            SearchProductsInputData input = new SearchProductsInputData(keyword);
            useCase.execute(input);
            
            responseModel = presenter.getViewModel(); // --- Lấy Response ---
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new SearchProductsViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
	}

	private void handleViewAllProducts(HttpServletResponse resp) throws IOException {
		ViewAllProductsViewModel responseModel;
        try {
            // Lắp ráp T2, T3
            ViewAllProductsViewModel viewModel = new ViewAllProductsViewModel();
            ViewAllProductsPresenter presenter = new ViewAllProductsPresenter(viewModel);
            ViewAllProductsUsecase useCase = new ViewAllProductsUsecase( // (Tên Usecase T3 của bạn)
                this.productRepo, this.categoryRepo, presenter, this.productFactory
            );
            
            useCase.execute(); // Gọi Tầng 3
            
            responseModel = presenter.getViewModel(); 
            resp.setStatus(HttpServletResponse.SC_OK); // 200
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new ViewAllProductsViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
	}

	/**
     * HÀM MỚI: Xử lý HTTP PUT (Sửa) - /api/admin/products/{id}
     * (Lát cắt 8)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        UpdateProductViewModel responseModel; // (ViewModel T2 "toàn string")
        try {
            // 1. Lấy ID từ URL
            int productId = getResourceId(req);
            if (productId == -1) {
                responseModel = new UpdateProductViewModel();
                responseModel.success = "false";
                responseModel.message = "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/products/1)";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 2. Đọc JSON Request
            UpdateProductRequest requestBody = gson.fromJson(req.getReader(), UpdateProductRequest.class);
            if (requestBody == null) {
                responseModel = new UpdateProductViewModel();
                responseModel.success = "false";
                responseModel.message = "Request body (JSON) bị rỗng.";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 3. Lắp ráp T2, T3
            UpdateProductViewModel viewModel = new UpdateProductViewModel();
            UpdateProductPresenter presenter = new UpdateProductPresenter(viewModel);
            UpdateProductUsecase useCase = new UpdateProductUsecase( // (Tên Usecase T3 của bạn)
                this.productRepo, this.categoryRepo, presenter, this.productFactory
            );

            // 4. Chuẩn bị & Gọi Tầng 3
            UpdateProductInputData input = new UpdateProductInputData(
                productId, // <-- ID từ URL
                requestBody.name, requestBody.description, requestBody.price, 
                requestBody.stockQuantity, requestBody.imageUrl, requestBody.categoryId,
                requestBody.attributes
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
            responseModel = new UpdateProductViewModel();
            responseModel.success = "false";
            responseModel.message = "JSON request không hợp lệ hoặc sai cú pháp.";
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
            resp.getWriter().write(gson.toJson(responseModel));
        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new UpdateProductViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP DELETE (Xóa) - /api/admin/products/{id}
     * (Lát cắt 9)
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        DeleteProductViewModel responseModel; // (ViewModel T2 "toàn string")
        try {
            // 1. Lấy ID từ URL
            int productId = getResourceId(req);
            if (productId == -1) {
                responseModel = new DeleteProductViewModel();
                responseModel.success = "false";
                responseModel.message = "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/products/1)";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 2. Lắp ráp T2, T3
            DeleteProductViewModel viewModel = new DeleteProductViewModel();
            DeleteProductPresenter presenter = new DeleteProductPresenter(viewModel);
            DeleteProductUsecase useCase = new DeleteProductUsecase( // (Tên Usecase T3 của bạn)
                this.productRepo, this.orderRepo, presenter
            );

            // 3. Chuẩn bị & Gọi Tầng 3
            DeleteProductInputData input = new DeleteProductInputData(productId);
            useCase.execute(input);

            // 4. Trả về Response
            responseModel = presenter.getViewModel();
            if ("false".equals(responseModel.success)) {
                // (Lỗi nghiệp vụ: Không tìm thấy, Còn đơn hàng)
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            } else {
                resp.setStatus(HttpServletResponse.SC_OK); // 200
            }
            resp.getWriter().write(gson.toJson(responseModel));
            
        } catch (Exception e) {
            e.printStackTrace();
            responseModel = new DeleteProductViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
   
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
