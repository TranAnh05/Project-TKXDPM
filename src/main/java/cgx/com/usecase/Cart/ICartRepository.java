package cgx.com.usecase.Cart;

public interface ICartRepository {
	CartData findByUserId(String userId);
    void save(CartData cartData);
}
