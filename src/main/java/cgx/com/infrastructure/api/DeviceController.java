package cgx.com.infrastructure.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cgx.com.adapters.ManageProduct.AddNewProduct.AddDevicePresenter;
import cgx.com.adapters.ManageProduct.AddNewProduct.AddDeviceViewModel;
import cgx.com.adapters.ManageProduct.AdjustStock.AdjustStockPresenter;
import cgx.com.adapters.ManageProduct.AdjustStock.AdjustStockViewModel;
import cgx.com.adapters.ManageProduct.DeleteDevice.DeleteDevicePresenter;
import cgx.com.adapters.ManageProduct.DeleteDevice.DeleteDeviceViewModel;
import cgx.com.adapters.ManageProduct.SearchDevices.SearchDevicesPresenter;
import cgx.com.adapters.ManageProduct.SearchDevices.SearchDevicesViewModel;
import cgx.com.adapters.ManageProduct.UpdateProduct.UpdateDevicePresenter;
import cgx.com.adapters.ManageProduct.UpdateProduct.UpdateDeviceViewModel;
import cgx.com.adapters.ManageProduct.ViewDeviceDetail.ViewDeviceDetailPresenter;
import cgx.com.adapters.ManageProduct.ViewDeviceDetail.ViewDeviceDetailViewModel;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddDeviceInputBoundary;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddLaptopRequestData;
import cgx.com.usecase.ManageProduct.AddNewProduct.AddMouseRequestData;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockInputBoundary;
import cgx.com.usecase.ManageProduct.AdjustStock.AdjustStockRequestData;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceInputBoundary;
import cgx.com.usecase.ManageProduct.DeleteDevice.DeleteDeviceRequestData;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesInputBoundary;
import cgx.com.usecase.ManageProduct.SearchDevices.SearchDevicesRequestData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceInputBoundary;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateLaptopRequestData;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateMouseRequestData;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailInputBoundary;
import cgx.com.usecase.ManageProduct.ViewDeviceDetail.ViewDeviceDetailRequestData;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    // --- Add Use Cases ---
    private final AddDeviceInputBoundary<AddLaptopRequestData> addLaptopUseCase;
    private final AddDeviceInputBoundary<AddMouseRequestData> addMouseUseCase;
    private final AddDevicePresenter addDevicePresenter;

    // --- Update Use Cases ---
    private final UpdateDeviceInputBoundary<UpdateLaptopRequestData> updateLaptopUseCase;
    private final UpdateDeviceInputBoundary<UpdateMouseRequestData> updateMouseUseCase;
    private final UpdateDevicePresenter updateDevicePresenter;

    // --- Adjust Stock Use Cases ---
    private final AdjustStockInputBoundary adjustLaptopStockUseCase;
    private final AdjustStockInputBoundary adjustMouseStockUseCase;
    private final AdjustStockPresenter adjustStockPresenter;
    
    // --- Delete Use Cases (FIX: Tách biệt Laptop và Mouse) ---
    private final DeleteDeviceInputBoundary deleteLaptopUseCase;
    private final DeleteDeviceInputBoundary deleteMouseUseCase;
    private final DeleteDevicePresenter deleteDevicePresenter;
    
    // --- Other Use Cases ---
    private final ViewDeviceDetailInputBoundary viewDetailUseCase;
    private final ViewDeviceDetailPresenter viewDetailPresenter;
    
    private final SearchDevicesInputBoundary searchUseCase;
    private final SearchDevicesPresenter searchPresenter;

    // --- Constructor Injection ---
    public DeviceController(
            AddDeviceInputBoundary<AddLaptopRequestData> addLaptopUseCase,
            AddDeviceInputBoundary<AddMouseRequestData> addMouseUseCase,
            AddDevicePresenter addDevicePresenter,
            UpdateDeviceInputBoundary<UpdateLaptopRequestData> updateLaptopUseCase,
            UpdateDeviceInputBoundary<UpdateMouseRequestData> updateMouseUseCase,
            UpdateDevicePresenter updateDevicePresenter,
            @Qualifier("adjustLaptopStockUseCase") AdjustStockInputBoundary adjustLaptopStockUseCase,
            @Qualifier("adjustMouseStockUseCase") AdjustStockInputBoundary adjustMouseStockUseCase,
            AdjustStockPresenter adjustStockPresenter,
            
            // FIX: Inject cả 2 Bean xóa riêng biệt
            @Qualifier("deleteLaptopUseCase") DeleteDeviceInputBoundary deleteLaptopUseCase,
            @Qualifier("deleteMouseUseCase") DeleteDeviceInputBoundary deleteMouseUseCase,
            DeleteDevicePresenter deleteDevicePresenter,
            
            ViewDeviceDetailInputBoundary viewDetailUseCase,
            ViewDeviceDetailPresenter viewDetailPresenter,
            SearchDevicesInputBoundary searchUseCase,
            SearchDevicesPresenter searchPresenter) {
        this.addLaptopUseCase = addLaptopUseCase;
        this.addMouseUseCase = addMouseUseCase;
        this.addDevicePresenter = addDevicePresenter;
        this.updateLaptopUseCase = updateLaptopUseCase;
        this.updateMouseUseCase = updateMouseUseCase;
        this.updateDevicePresenter = updateDevicePresenter;
        this.adjustLaptopStockUseCase = adjustLaptopStockUseCase;
        this.adjustMouseStockUseCase = adjustMouseStockUseCase;
        this.adjustStockPresenter = adjustStockPresenter;
        this.deleteLaptopUseCase = deleteLaptopUseCase;
        this.deleteMouseUseCase = deleteMouseUseCase;
        this.deleteDevicePresenter = deleteDevicePresenter;
        this.viewDetailUseCase = viewDetailUseCase;
        this.viewDetailPresenter = viewDetailPresenter;
        this.searchUseCase = searchUseCase;
        this.searchPresenter = searchPresenter;
    }

    // =========================================================================
    // 1. CREATE (ADD)
    // =========================================================================

    @PostMapping("/laptops")
    public ResponseEntity<AddDeviceViewModel> addLaptop(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddLaptopRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        AddLaptopRequestData input = new AddLaptopRequestData(
            token, req.name, req.description, req.price, req.stockQuantity, req.categoryId, req.thumbnail,
            req.cpu, req.ram, req.storage, req.screenSize
        );
        addLaptopUseCase.execute(input);
        return ResponseEntity.ok(addDevicePresenter.getModel());
    }

    @PostMapping("/mice")
    public ResponseEntity<AddDeviceViewModel> addMouse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddMouseRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        AddMouseRequestData input = new AddMouseRequestData(
            token, req.name, req.description, req.price, req.stockQuantity, req.categoryId, req.thumbnail,
            req.dpi, req.isWireless, req.buttonCount
        );
        addMouseUseCase.execute(input);
        return ResponseEntity.ok(addDevicePresenter.getModel());
    }

    // =========================================================================
    // 2. UPDATE INFO
    // =========================================================================

    @PutMapping("/laptops/{id}")
    public ResponseEntity<UpdateDeviceViewModel> updateLaptop(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id, @RequestBody UpdateLaptopRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        UpdateLaptopRequestData input = new UpdateLaptopRequestData(
            token, id, req.name, req.description, req.price, req.stockQuantity, req.categoryId, req.thumbnail, req.status,
            req.cpu, req.ram, req.storage, req.screenSize
        );
        updateLaptopUseCase.execute(input);
        return ResponseEntity.ok(updateDevicePresenter.getModel());
    }

    @PutMapping("/mice/{id}")
    public ResponseEntity<UpdateDeviceViewModel> updateMouse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id, @RequestBody UpdateMouseRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        UpdateMouseRequestData input = new UpdateMouseRequestData(
            token, id, req.name, req.description, req.price, req.stockQuantity, req.categoryId, req.thumbnail, req.status,
            req.dpi, req.isWireless, req.buttonCount
        );
        updateMouseUseCase.execute(input);
        return ResponseEntity.ok(updateDevicePresenter.getModel());
    }

    // =========================================================================
    // 3. ADJUST STOCK
    // =========================================================================

    @PutMapping("/laptops/{id}/stock")
    public ResponseEntity<AdjustStockViewModel> adjustLaptopStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id, 
            @RequestBody AdjustStockRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        adjustLaptopStockUseCase.execute(new AdjustStockRequestData(token, id, req.newQuantity));
        return ResponseEntity.ok(adjustStockPresenter.getModel());
    }

    @PutMapping("/mice/{id}/stock")
    public ResponseEntity<AdjustStockViewModel> adjustMouseStock(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id, 
            @RequestBody AdjustStockRequestJson req) {
        String token = authHeader != null ? authHeader : "";
        adjustMouseStockUseCase.execute(new AdjustStockRequestData(token, id, req.newQuantity));
        return ResponseEntity.ok(adjustStockPresenter.getModel());
    }

    // =========================================================================
    // 4. READ (SEARCH & DETAIL)
    // =========================================================================

    @GetMapping
    public ResponseEntity<SearchDevicesViewModel> search(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = authHeader != null ? authHeader : "";
        SearchDevicesRequestData input = new SearchDevicesRequestData(
            token, keyword, categoryId, minPrice, maxPrice, status, page, size
        );
        searchUseCase.execute(input);
        return ResponseEntity.ok(searchPresenter.getModel());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViewDeviceDetailViewModel> getDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        viewDetailUseCase.execute(new ViewDeviceDetailRequestData(id, token));
        return ResponseEntity.ok(viewDetailPresenter.getModel());
    }

    // =========================================================================
    // 5. DELETE (FIX: Tách biệt endpoint)
    // =========================================================================

    @DeleteMapping("/laptops/{id}")
    public ResponseEntity<DeleteDeviceViewModel> deleteLaptop(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        // Gọi đúng Use Case xóa Laptop
        deleteLaptopUseCase.execute(new DeleteDeviceRequestData(token, id));
        return ResponseEntity.ok(deleteDevicePresenter.getModel());
    }

    @DeleteMapping("/mice/{id}")
    public ResponseEntity<DeleteDeviceViewModel> deleteMouse(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        String token = authHeader != null ? authHeader : "";
        // Gọi đúng Use Case xóa Mouse
        deleteMouseUseCase.execute(new DeleteDeviceRequestData(token, id));
        return ResponseEntity.ok(deleteDevicePresenter.getModel());
    }

    // =========================================================================
    // DTOs (Inner Classes for JSON Body)
    // =========================================================================

    public static class AddLaptopRequestJson {
        public String name; public String description; public BigDecimal price; public int stockQuantity;
        public String categoryId; public String thumbnail;
        public String cpu; public String ram; public String storage; public double screenSize;
    }
    
    public static class AddMouseRequestJson {
        public String name; public String description; public BigDecimal price; public int stockQuantity;
        public String categoryId; public String thumbnail;
        public int dpi; public boolean isWireless; public int buttonCount;
    }

    public static class UpdateLaptopRequestJson extends AddLaptopRequestJson { 
        public String status; 
    }
    
    public static class UpdateMouseRequestJson extends AddMouseRequestJson { 
        public String status; 
    }

    public static class AdjustStockRequestJson { 
        public int newQuantity; 
    }
}