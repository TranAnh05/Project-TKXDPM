package application.usecases.ManageUser.ViewAllUsers;

import java.util.ArrayList;
import java.util.List;

import application.dtos.ManageUser.UserData;
import application.dtos.ManageUser.UserOutputData;
import application.dtos.ManageUser.ViewAllUsers.ViewAllUsersOutputData;
import application.ports.in.ManageUser.ViewAllUsers.ViewAllUsersInputBoundary;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.ManageUser.ViewAllUsers.ViewAllUsersOutputBoundary;
import domain.entities.User;

public class ViewAllUsersUsecase implements ViewAllUsersInputBoundary{
	private UserRepository userRepository;
    private ViewAllUsersOutputBoundary userPresenter;
    private ViewAllUsersOutputData outputData; // Field cho TDD
    
    public ViewAllUsersUsecase() {}
	
	public ViewAllUsersUsecase(UserRepository userRepository, ViewAllUsersOutputBoundary userPresenter) {
		this.userRepository = userRepository;
		this.userPresenter = userPresenter;
	}
	
	public ViewAllUsersOutputData getOuputData() {
		return outputData;
	}

	@Override
	public void execute() {
		outputData = new ViewAllUsersOutputData();
		try {
			// 1. Lấy UserData (T3 DTO)
            List<UserData> userDataList = userRepository.findAll();
            
            if (userDataList.isEmpty()) {
                // 2. Xử lý kịch bản rỗng
                this.outputData.success = true;
                this.outputData.message = "Không tìm thấy người dùng nào.";
                this.outputData.users = new ArrayList<>();
                userPresenter.present(this.outputData);
                return;
            }
            
            // 3. Chuyển DTO (T3) -> Entity (T4)
            List<User> userEntities = mapDataToEntities(userDataList);
            
            // 4. Chuyển Entity (T4) -> Output DTO an toàn (T3)
            // (Bước này loại bỏ passwordHash)
            List<UserOutputData> safeOutputList = mapEntitiesToOutputData(userEntities);
            
            // 5. Báo cáo thành công
            outputData.success = true;
            outputData.users = safeOutputList;
		} catch (Exception e) {
			// 6. Bắt lỗi hệ thống
            outputData.success = false;
            outputData.message = "Đã xảy ra lỗi hệ thống khi tải người dùng.";
            outputData.users = new ArrayList<>();
		}
		
		userPresenter.present(outputData);
		
	}

	private List<UserOutputData> mapEntitiesToOutputData(List<User> entities) {
		List<UserOutputData> dtoList = new ArrayList<>();
		
        for (User entity : entities) {
            UserOutputData dto = new UserOutputData();
            dto.id = entity.getId();
            dto.email = entity.getEmail();
            dto.fullName = entity.getFullName();
            dto.address = entity.getAddress();
            dto.role = entity.getRole();
            dto.isBlocked = entity.isBlocked();
            
            dtoList.add(dto);
        }
        
        return dtoList;
	}

	private List<User> mapDataToEntities(List<UserData> dataList) {
		List<User> entities = new ArrayList<>();
		
        for (UserData data : dataList) {
            entities.add(new User(
                data.id, data.email, data.passwordHash,
                data.fullName, data.address, data.role, data.isBlocked
            ));
        }
        
        return entities;
	}

}
