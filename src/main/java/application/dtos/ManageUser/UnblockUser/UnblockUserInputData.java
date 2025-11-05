package application.dtos.ManageUser.UnblockUser;

public class UnblockUserInputData {
	public int userIdToUnblock;
	
	public UnblockUserInputData() {}

    public UnblockUserInputData(int userIdToUnblock) {
        this.userIdToUnblock = userIdToUnblock;
    }
}
