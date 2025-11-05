package application.dtos.ManageUser.BlockUser;

public class BlockUserInputData {
	public int userIdToBlock;
    public int currentAdminId; // ID của Admin đang thực hiện
    
    public BlockUserInputData() {}
    
    public BlockUserInputData(int userIdToBlock, int currentAdminId) {
        this.userIdToBlock = userIdToBlock;
        this.currentAdminId = currentAdminId;
    }
}
