package cgx.com.usecase.ManageProduct.AddNewProduct;

/**
 * Interface đánh dấu chung (Marker Interface) hoặc dùng Generic.
 * Để đơn giản, chúng ta dùng Generic T extends AddDeviceRequestData.
 */
public interface AddDeviceInputBoundary<T extends AddDeviceRequestData> {
    void execute(T inputData);
}