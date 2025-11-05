package application.usecases.ManageUser.UnblockUser;

import application.dtos.ManageUser.UserData;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserInputData;
import application.dtos.ManageUser.UnblockUser.UnblockUserOutputData;
import application.ports.in.ManageUser.UnblockUser.UnblockUserInputBoundary;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.ManageUser.UnblockUser.UnblockUserOutputBoundary;
import domain.entities.User;

public class UnblockUserUsecase implements UnblockUserInputBoundary{
	private UserRepository userRepository;
    private UnblockUserOutputBoundary userPresenter;
    private UnblockUserOutputData outputData; // Field cho TDD
    
    public UnblockUserUsecase() {}
    
	public UnblockUserUsecase(UserRepository userRepository, UnblockUserOutputBoundary userPresenter) {
		this.userRepository = userRepository;
		this.userPresenter = userPresenter;
	}

	public UnblockUserOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UnblockUserInputData input) {
		outputData = new UnblockUserOutputData();
		
		try {
			// 1. Lấy UserData (T3 DTO)
            UserData userData = userRepository.findById(input.userIdToUnblock);
            if (userData == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng để mở khóa.");
            }
            
            // 2. Chuyển T3 DTO -> T4 Entity
            User userEntity = mapDataToEntity(userData);

            // 3. GỌI TẦNG 4 (Entity) ĐỂ CẬP NHẬT
            userEntity.setBlocked(false); // <-- Logic chính

            // 4. Chuyển T4 (Entity) -> T3 DTO
            UserData dataToUpdate = mapEntityToData(userEntity);

            // 5. Lưu vào CSDL
            UserData updatedUserData = userRepository.update(dataToUpdate);

            // 6. Báo cáo thành công (Chuyển đổi sang DTO an toàn)
            outputData.success = true;
            outputData.message = "Mở khóa tài khoản thành công!";
            outputData.updatedUser = mapUserDataToOutputData(updatedUserData);
            
		} catch (IllegalArgumentException e) {
            // 7. BẮT LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 8. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		userPresenter.present(outputData);
	}

	private UserOutputData mapUserDataToOutputData(UserData  data) {
		UserOutputData dto = new UserOutputData();
        dto.id = data.id;
        dto.email = data.email;
        dto.fullName = data.fullName;
        dto.address = data.address;
        dto.role = data.role;
        dto.isBlocked = data.isBlocked;
        return dto;
	}

	private UserData mapEntityToData(User entity) {
		return new UserData(entity.getId(), entity.getEmail(), entity.getPasswordHash(),
                entity.getFullName(), entity.getAddress(), entity.getRole(), entity.isBlocked());
	}

	private User mapDataToEntity(UserData data) {
		return new User(data.id, data.email, data.passwordHash,
                data.fullName, data.address, data.role, data.isBlocked);
	}
}
