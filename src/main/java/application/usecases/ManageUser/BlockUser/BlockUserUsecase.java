package application.usecases.ManageUser.BlockUser;

import Entities.User;
import application.dtos.ManageUser.BlockUser.BlockUserInputData;
import application.dtos.ManageUser.BlockUser.BlockUserOutputData;
import application.ports.in.ManageUser.BlockUser.BlockUserInputBoundary;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.ManageUser.BlockUser.BlockUserOutputBoundary;
import usecase.ManageUser.UserData;
import usecase.ManageUser.UserOutputData;

public class BlockUserUsecase implements BlockUserInputBoundary{
	private UserRepository userRepository;
    private BlockUserOutputBoundary userPresenter;
    private BlockUserOutputData outputData; // Field cho TDD
    
    public BlockUserUsecase() {}
    
	public BlockUserUsecase(UserRepository userRepository, BlockUserOutputBoundary userPresenter) {
		this.userRepository = userRepository;
		this.userPresenter = userPresenter;
	}
	
	public BlockUserOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(BlockUserInputData input) {
		outputData = new BlockUserOutputData();
		
		try {
			// 1. Kiểm tra nghiệp vụ (Admin tự khóa)
            if (input.userIdToBlock == input.currentAdminId) {
                throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình.");
            }
            
            // 2. Lấy UserData (T3 DTO)
            UserData userData = userRepository.findById(input.userIdToBlock);
            if (userData == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng để khóa.");
            }
            
            // 3. Chuyển T3 DTO -> T4 Entity
            User userEntity = mapDataToEntity(userData);

            // 4. GỌI TẦNG 4 (Entity) ĐỂ CẬP NHẬT
            userEntity.setBlocked(true); // <-- Logic chính

            // 5. Chuyển T4 (Entity) -> T3 DTO
            UserData dataToUpdate = mapEntityToData(userEntity);

            // 6. Lưu vào CSDL
            UserData updatedUserData = userRepository.update(dataToUpdate);

            // 7. Báo cáo thành công (Chuyển đổi sang DTO an toàn)
            outputData.success = true;
            outputData.message = "Khóa tài khoản thành công!";
            outputData.updatedUser = mapUserDataToOutputData(updatedUserData);
		} catch (IllegalArgumentException e) {
            // 8. BẮT LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 9. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.";
		}
		
		userPresenter.present(outputData);
	}

	private UserOutputData mapUserDataToOutputData(UserData data) {
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
