package cgx.com.usecase.ManageProduct.UpdateProduct;

public interface UpdateDeviceInputBoundary<T extends UpdateDeviceRequestData> {
    void execute(T inputData);
}
