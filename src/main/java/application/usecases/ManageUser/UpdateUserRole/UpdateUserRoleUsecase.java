package application.usecases.ManageUser.UpdateUserRole;

import Entities.User;
import Entities.UserRole;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleInputData;
import application.dtos.ManageUser.UpdateUserRole.UpdateUserRoleOutputData;
import application.ports.in.ManageUser.UpdateUserRole.UpdateUserRoleInputBoundary;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.ManageUser.UpdateUserRole.UpdateUserRoleOutputBoundary;
import usecase.ManageUser.UserData;
import usecase.ManageUser.UserOutputData;

public class UpdateUserRoleUsecase implements UpdateUserRoleInputBoundary{
	private UserRepository userRepository;
    private UpdateUserRoleOutputBoundary userPresenter;
    private UpdateUserRoleOutputData outputData; // Field cho TDD
    
    public UpdateUserRoleUsecase() {
    	
    }
    
	public UpdateUserRoleUsecase(UserRepository userRepository, UpdateUserRoleOutputBoundary userPresenter) {
		this.userRepository = userRepository;
		this.userPresenter = userPresenter;
	}

	public UpdateUserRoleOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(UpdateUserRoleInputData input) {
		outputData = new UpdateUserRoleOutputData();
		try {
			UserRole newRole;
			User.validateRole(input.newRole);
			newRole = UserRole.valueOf(input.newRole.toUpperCase());
			
			// 2. Kiểm tra nghiệp vụ (Admin tự sửa)
            if (input.userIdToUpdate == input.currentAdminId) {
                throw new IllegalArgumentException("Không thể tự thay đổi vai trò của chính mình.");
            }
            
            // 3. Lấy UserData (T3 DTO)
            UserData userData = userRepository.findById(input.userIdToUpdate);
            if (userData == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng để cập nhật.");
            }
            
            // 4. Chuyển T3 DTO -> T4 Entity
            User userEntity = mapDataToEntity(userData);
            
            // 5. GỌI TẦNG 4 (Entity) ĐỂ CẬP NHẬT
            userEntity.setRole(newRole);
            
            // 6. Chuyển T4 (Entity) -> T3 DTO
            UserData dataToUpdate = mapEntityToData(userEntity);
            
            // 7. Lưu vào CSDL
            UserData updatedUserData = userRepository.update(dataToUpdate);
            
            // 8. Báo cáo thành công (Chuyển đổi sang DTO an toàn)
            outputData.success = true;
            outputData.message = "Cập nhật vai trò thành công!";
            outputData.updatedUser = mapUserDataToOutputData(updatedUserData);
            
		} catch (IllegalArgumentException e) {
            // 9. BẮT LỖI VALIDATION (T4) HOẶC LỖI NGHIỆP VỤ (T3)
            outputData.success = false;
            outputData.message = e.getMessage();
        } catch (Exception e) {
        	// 10. Bắt lỗi hệ thống
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
