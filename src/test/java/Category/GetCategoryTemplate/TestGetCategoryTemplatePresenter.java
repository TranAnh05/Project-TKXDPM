package Category.GetCategoryTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import adapters.ManageCategory.GetCategoryTemplate.GetCategoryTemplatePresenter;
import adapters.ManageCategory.GetCategoryTemplate.GetCategoryTemplateViewModel;
import application.dtos.ManageCategory.GetCategoryTemplate.GetCategoryTemplateOutputData;

public class TestGetCategoryTemplatePresenter {
	@Test
    public void testPresent_SuccessCase() {
        // 1. Arrange
        GetCategoryTemplateViewModel viewModel = new GetCategoryTemplateViewModel();
        GetCategoryTemplatePresenter presenter = new GetCategoryTemplatePresenter(viewModel);
        
        GetCategoryTemplateOutputData output = new GetCategoryTemplateOutputData();
        output.success = true;
        output.attributeTemplate = "{\"cpu\":\"text\"}";

        // 2. Act
        presenter.present(output);

        // 3. Assert (Kiểm tra ViewModel "toàn string")
        assertEquals("true", presenter.getViewModel().success);
        assertEquals("{\"cpu\":\"text\"}", presenter.getViewModel().attributeTemplate);
    }
}
