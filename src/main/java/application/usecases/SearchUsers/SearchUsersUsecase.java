package application.usecases.SearchUsers;

import java.util.ArrayList;
import java.util.List;

import Entities.User;
import application.dtos.SearchUsers.SearchUsersInputData;
import application.dtos.SearchUsers.SearchUsersOutputData;
import application.ports.in.SearchUsers.SearchUsersInputBoundary;
import application.ports.out.ManageUser.UserRepository;
import application.ports.out.SearchUsers.SearchUsersOutputBoundary;
import usecase.ManageUser.UserData;
import usecase.ManageUser.UserOutputData;

public class SearchUsersUsecase implements SearchUsersInputBoundary{
	private UserRepository userRepository;
    private SearchUsersOutputBoundary userPresenter;
    private SearchUsersOutputData outputData; // Field cho TDD
    
    public SearchUsersUsecase() {}
    
	public SearchUsersUsecase(UserRepository userRepository, SearchUsersOutputBoundary userPresenter) {
		this.userRepository = userRepository;
		this.userPresenter = userPresenter;
	}

	public SearchUsersOutputData getOutputData() {
		return outputData;
	}

	@Override
	public void execute(SearchUsersInputData input) {
		outputData = new SearchUsersOutputData();
		
		try {
			// 1. Lấy UserData (T3 DTO) TỪ HÀM TÌM KIẾM
            List<UserData> userDataList = userRepository.searchByEmail(input.emailKeyword);
            
         // 2. Xử lý kịch bản rỗng
            if (userDataList.isEmpty()) {
                outputData.success = true;
                outputData.message = "Không tìm thấy người dùng nào khớp với '" + input.emailKeyword + "'.";
                outputData.users = new ArrayList<>();
                userPresenter.present(outputData);
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
            outputData.message = "Đã xảy ra lỗi hệ thống khi tìm kiếm người dùng.";
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
