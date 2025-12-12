package cgx.com.usecase.ManageProduct.AddNewProduct;

public interface AddDeviceInputBoundary<T extends AddDeviceRequestData> {
    void execute(T inputData);
}