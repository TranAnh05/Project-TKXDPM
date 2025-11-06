package infrastructure.web.controllers;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import adapters.ManageCategory.AddNewCategory.AddNewCategoryPresenter;
import adapters.ManageCategory.AddNewCategory.AddNewCategoryViewModel;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryPresenter;
import adapters.ManageCategory.DeleteCategory.DeleteCategoryViewModel;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryPresenter;
import adapters.ManageCategory.UpdateCategory.UpdateCategoryViewModel;
import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesPresenter;
import adapters.ManageCategory.ViewAllCategories.ViewAllCategoriesViewModel;
import application.dtos.ManageCategory.AddNewCategory.AddNewCategoryInputData;
import application.dtos.ManageCategory.DeleteCategory.DeleteCategoryInputData;
import application.dtos.ManageCategory.UpdateCategory.UpdateCategoryInputData;
import application.ports.out.ManageCategory.CategoryRepository;
import application.usecases.ManageCategory.AddNewCategory.AddNewCategoryUsecase;
import application.usecases.ManageCategory.DeleteCategory.DeleteCategoryUsecase;
import application.usecases.ManageCategory.UpdateCategory.UpdateCategoryUsecase;
import application.usecases.ManageCategory.ViewAllCategories.ViewAllCategoryUsecase;
import infrastructure.database.CategoryRepositoryImpl;
import infrastructure.web.requests.AddCategory.AddCategoryRequest;
import infrastructure.web.requests.UpdateCategory.UpdateCategoryRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TRIỂN KHAI API (Tầng 1) - DÙNG SERVLET
 * URL sẽ là: http://localhost:8080/[ten-du-an]/api/admin/categories
 */

@WebServlet("/api/admin/categories/*")
public class CategoryRestfulWS extends HttpServlet{
	// Khởi tạo thư viện JSON (Gson)
    private final Gson gson = new Gson();
    
    /**
     * Xử lý HTTP POST (Thêm mới)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // Cấu hình Request/Response là JSON
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        AddNewCategoryViewModel responseModel; // (ViewModel T2 "toàn string")

        try {
            // 1. Đọc JSON từ Request (T1) và map vào DTO (T1)
            AddCategoryRequest requestBody = gson.fromJson(req.getReader(), AddCategoryRequest.class);

            // 2. Validation sơ bộ (Tầng 1)
            if (requestBody == null) {
                responseModel = new AddNewCategoryViewModel();
                responseModel.success = "false";
                responseModel.message = "Request body (JSON) bị rỗng.";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 3. *** NƠI LẮP RÁP (COMPOSITION ROOT) ***
            // Lắp ráp "Thật" Tầng 1 (Database)
            CategoryRepository categoryRepo = new CategoryRepositoryImpl();
            
            // Lắp ráp Tầng 2 (ViewModel "Toàn String" + Presenter)
            AddNewCategoryViewModel viewModel = new AddNewCategoryViewModel();
            AddNewCategoryPresenter presenter = new AddNewCategoryPresenter(viewModel);
            
            // Lắp ráp Tầng 3 (Interactor)
            AddNewCategoryUsecase useCase = new AddNewCategoryUsecase(categoryRepo, presenter);

            // 4. Chuẩn bị DTO (T3)
            AddNewCategoryInputData input = new AddNewCategoryInputData(
                requestBody.name,
                requestBody.attributeTemplate
            );
            
            // 5. GỌI LÕI NGHIỆP VỤ (TẦNG 3)
            useCase.execute(input);

            // 6. Lấy ViewModel (T2) đã được Presenter cập nhật
            responseModel = presenter.getModel();
            
            // 7. Trả về Response
            if ("false".equals(responseModel.success)) {
                // Nếu UseCase báo lỗi (VD: Tên trùng, Tên rỗng)
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            } else {
                // Thành công
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (JsonSyntaxException e) {
            // Xử lý nếu JSON gửi lên bị sai cú pháp
            responseModel = new AddNewCategoryViewModel();
            responseModel.success = "false";
            responseModel.message = "JSON request không hợp lệ hoặc sai cú pháp.";
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write(gson.toJson(responseModel));
        } catch (Exception e) {
            // Xử lý các lỗi hệ thống khác
            responseModel = new AddNewCategoryViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP GET (Xem tất cả)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // Cấu hình Response là JSON
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        ViewAllCategoriesViewModel responseModel; // (ViewModel T2 "toàn string")

        try {
            // 1. *** NƠI LẮP RÁP (COMPOSITION ROOT) ***
            
            // Lắp ráp "Thật" Tầng 1 (Database)
            CategoryRepository categoryRepo = new CategoryRepositoryImpl(); // (Tên T1 Impl của bạn)
            
            // Lắp ráp Tầng 2 (ViewModel "Toàn String" + Presenter)
            ViewAllCategoriesViewModel viewModel = new ViewAllCategoriesViewModel();
            ViewAllCategoriesPresenter presenter = new ViewAllCategoriesPresenter(viewModel);
            
            // Lắp ráp Tầng 3 (Interactor)
            ViewAllCategoryUsecase useCase = new ViewAllCategoryUsecase(categoryRepo, presenter); // (Tên T3 Usecase của bạn)

            // 2. GỌI LÕI NGHIỆP VỤ (TẦNG 3)
            useCase.execute();

            // 3. Lấy ViewModel (T2) đã được Presenter cập nhật
            responseModel = presenter.getModel();
            
            // 4. Trả về Response
            if ("false".equals(responseModel.success)) {
                // Nếu UseCase báo lỗi (VD: Lỗi CSDL)
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            } else {
                // Thành công
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            // Xử lý các lỗi hệ thống khác (ví dụ: lỗi lắp ráp)
            e.printStackTrace(); // In lỗi ra console Tomcat
            responseModel = new ViewAllCategoriesViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP PUT (Sửa)
     * URL: /api/admin/categories/{id} (ví dụ: /api/admin/categories/1)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        UpdateCategoryViewModel responseModel; 

        try {
            // 1. Lấy ID từ URL
            int categoryId = getResourceId(req);
            if (categoryId == -1) {
                // VIẾT LỖI 400 (HOÀN CHỈNH)
                responseModel = new UpdateCategoryViewModel();
                responseModel.success = "false";
                responseModel.message = "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/categories/1)";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 2. Đọc JSON từ Request (T1) -> DTO (T1)
            UpdateCategoryRequest requestBody = gson.fromJson(req.getReader(), UpdateCategoryRequest.class);

            // 3. Validation sơ bộ (Tầng 1)
            if (requestBody == null) {
                // VIẾT LỖI 400 (HOÀN CHỈNH)
                responseModel = new UpdateCategoryViewModel();
                responseModel.success = "false";
                responseModel.message = "Request body (JSON) bị rỗng.";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 4. *** NƠI LẮP RÁP (COMPOSITION ROOT) ***
            CategoryRepository categoryRepo = new CategoryRepositoryImpl();
            UpdateCategoryViewModel viewModel = new UpdateCategoryViewModel();
            UpdateCategoryPresenter presenter = new UpdateCategoryPresenter(viewModel);
            UpdateCategoryUsecase useCase = new UpdateCategoryUsecase(categoryRepo, presenter);

            // 5. Chuẩn bị DTO (T3)
            UpdateCategoryInputData input = new UpdateCategoryInputData(
                categoryId, // <-- ID từ URL
                requestBody.name,
                requestBody.attributeTemplate
            );
            
            // 6. GỌI LÕI NGHIỆP VỤ (TẦNG 3)
            useCase.execute(input);

            // 7. Lấy ViewModel (T2) đã được Presenter cập nhật
            responseModel = presenter.getModel();
            
            // 8. Trả về Response
            if ("false".equals(responseModel.success)) {
                // Nếu UseCase báo lỗi (VD: Tên trùng, Không tìm thấy ID)
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            } else {
                // Thành công
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (JsonSyntaxException e) {
            // (Xử lý lỗi JSON sai cú pháp)
            responseModel = new UpdateCategoryViewModel();
            responseModel.success = "false";
            responseModel.message = "JSON request không hợp lệ hoặc sai cú pháp.";
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            resp.getWriter().write(gson.toJson(responseModel));
        } catch (Exception e) {
            // (Xử lý lỗi hệ thống)
            e.printStackTrace(); // In lỗi
            responseModel = new UpdateCategoryViewModel();
            responseModel.success = "false";
            responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
    
    /**
     * Hàm helper (private) để lấy ID từ URL
     * (ví dụ: /api/admin/categories/1 -> trả về 1)
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
        } catch (NumberFormatException e) {
            // (URL không phải là số)
        }
        return -1; // Không tìm thấy ID
    }
    
    /**
     * HÀM MỚI: Xử lý HTTP DELETE (Xóa)
     * URL: /api/admin/categories/{id} (ví dụ: /api/admin/categories/1)
     */
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        DeleteCategoryViewModel responseModel; // (ViewModel T2 "toàn string")

        try {
            // 1. Lấy ID từ URL (dùng lại hàm helper)
            int categoryId = getResourceId(req);
            if (categoryId == -1) {
                responseModel = new DeleteCategoryViewModel();
                responseModel.success = "false";
                responseModel.message = "URL không hợp lệ. Phải có ID (ví dụ: /api/admin/categories/1)";
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                resp.getWriter().write(gson.toJson(responseModel));
                return;
            }

            // 2. *** NƠI LẮP RÁP (COMPOSITION ROOT) ***
            CategoryRepository categoryRepo = new CategoryRepositoryImpl();
            DeleteCategoryViewModel viewModel = new DeleteCategoryViewModel();
            DeleteCategoryPresenter presenter = new DeleteCategoryPresenter(viewModel);
            DeleteCategoryUsecase useCase = new DeleteCategoryUsecase(categoryRepo, presenter); // (Tên T3 Usecase của bạn)

            // 3. Chuẩn bị DTO (T3)
            DeleteCategoryInputData input = new DeleteCategoryInputData(categoryId);
            
            // 4. GỌI LÕI NGHIỆP VỤ (TẦNG 3)
            useCase.execute(input);

            // 5. Lấy ViewModel (T2) đã được Presenter cập nhật
            responseModel = presenter.getViewModel();
            
            // 6. Trả về Response
            if ("false".equals(responseModel.success)) {
                // Nếu UseCase báo lỗi (VD: Không tìm thấy, Còn sản phẩm)
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            } else {
                // Thành công
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            }
            resp.getWriter().write(gson.toJson(responseModel));

        } catch (Exception e) {
            // (Xử lý lỗi hệ thống - ví dụ: Lỗi khóa ngoại từ CSDL)
            e.printStackTrace(); // In lỗi
            responseModel = new DeleteCategoryViewModel();
            responseModel.success = "false";
            // Check lỗi khóa ngoại
            if (e.getCause() != null && e.getCause().getMessage().contains("foreign key constraint fails")) {
                responseModel.message = "Lỗi CSDL: Không thể xóa (vi phạm khóa ngoại).";
            } else {
                responseModel.message = "Lỗi hệ thống không xác định: " + e.getMessage();
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            resp.getWriter().write(gson.toJson(responseModel));
        }
    }
}
