package Product.UpdateDevice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cgx.com.adapters.ManageProduct.UpdateProduct.UpdateDevicePresenter;
import cgx.com.adapters.ManageProduct.UpdateProduct.UpdateDeviceViewModel;
import cgx.com.usecase.ManageProduct.UpdateProduct.UpdateDeviceResponseData;

class UpdateDevicePresenterTest {

    private UpdateDeviceViewModel viewModel;
    private UpdateDevicePresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new UpdateDeviceViewModel();
        presenter = new UpdateDevicePresenter(viewModel);
    }

    // Case: thành công
    @Test
    void testPresent_WhenSuccess_ShouldUpdateViewModelCorrectly() {
        // Arrange
        UpdateDeviceResponseData responseData = new UpdateDeviceResponseData();
        responseData.success = true;
        responseData.message = "Cập nhật thành công";
        responseData.deviceId = "DEVICE-001";

        // Act
        presenter.present(responseData);

        // Assert
        // Kiểm tra success được convert từ boolean true -> String "true"
        assertEquals("true", viewModel.success);
        assertEquals("Cập nhật thành công", viewModel.message);
        assertEquals("DEVICE-001", viewModel.deviceId);
    }

    // Case thất bại
    @Test
    void testPresent_WhenFailure_ShouldUpdateViewModelCorrectly() {
        // Arrange
        UpdateDeviceResponseData responseData = new UpdateDeviceResponseData();
        responseData.success = false;
        responseData.message = "Cập nhật thất bại";

        // Act
        presenter.present(responseData);

        assertEquals("false", viewModel.success);
        assertEquals("Cập nhật thất bại", viewModel.message);
        assertNull(viewModel.deviceId);
    }
}
