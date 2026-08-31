package _Project.Mita.controller.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import _Project.Mita.entity.Category;
import _Project.Mita.form.CategoryRequest;
import _Project.Mita.service.CategoryService;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryApiControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CategoryService categoryService;

	private static final String INVALID_CATEGORY_JSON = "{}";
	private static final String BLANK_CATEGORY_JSON = "{\"categoryName\": \"\"}";
	private static final String VALID_CATEGORY_JSON = "{\"categoryName\": \"新規カテゴリ\"}";

	@Test
	void 未ログインでカテゴリ登録にアクセスすると401() throws Exception {
		mockMvc.perform(post("/api/categories").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_CATEGORY_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void 未ログインでカテゴリ更新にアクセスすると401() throws Exception {
		mockMvc.perform(put("/api/categories/1").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_CATEGORY_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void 未ログインでカテゴリ削除にアクセスすると401() throws Exception {
		mockMvc.perform(delete("/api/categories/1").with(csrf()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーがカテゴリ登録にアクセスすると403() throws Exception {
		mockMvc.perform(post("/api/categories").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_CATEGORY_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーがカテゴリ更新にアクセスすると403() throws Exception {
		mockMvc.perform(put("/api/categories/999999").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_CATEGORY_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーがカテゴリ削除にアクセスすると403() throws Exception {
		mockMvc.perform(delete("/api/categories/999999").with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者は正しいJSONを送ればカテゴリ登録APIを正常に実行できる() throws Exception {
		Category dummyCategory = new Category();
		dummyCategory.setCategoryId(1L);
		dummyCategory.setCategoryName("新規カテゴリ");
		when(categoryService.create(any(CategoryRequest.class))).thenReturn(dummyCategory);

		mockMvc.perform(post("/api/categories").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(VALID_CATEGORY_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者がcategoryNameがnullのJSONを送ると400エラーになりfieldErrorsを返す() throws Exception {
		mockMvc.perform(post("/api/categories").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_CATEGORY_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.categoryName").value("カテゴリ名は必須です"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者がcategoryNameが空文字のJSONを送ると400エラーになりfieldErrorsを返す() throws Exception {
		mockMvc.perform(post("/api/categories").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(BLANK_CATEGORY_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.categoryName").value("カテゴリ名は必須です"));
	}
}
