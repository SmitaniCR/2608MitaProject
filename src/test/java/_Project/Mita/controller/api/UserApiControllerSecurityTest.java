package _Project.Mita.controller.api;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import _Project.Mita.entity.User;
import _Project.Mita.service.SessionUserService;
import _Project.Mita.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiControllerSecurityTest {

	//@MockitoBeanでServiceを丸ごと偽物にするやり方
	//UserPrincipalに仮のuserを通すやり方ではなく、そのロジックを丸ごと飛ばす
	//Service層は既に検証済みかつ@Validメソッドが存在しないためController自身の配線のテストのみで十分
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessionUserService sessionUserService;

	@MockitoBean
	private UserService userService;

	private static final String ROLE_UPDATE_JSON = "{\"isAdmin\":true}";
	private User mockAdminUser;

	@BeforeEach
	void setUp() {
		mockAdminUser = new User();
		mockAdminUser.setUserId(1L);
		mockAdminUser.setAdmin(true);

		when(sessionUserService.requireCurrentUser(any())).thenReturn(mockAdminUser);
	}

	@Nested
	class ListTest {
		@Test
		void 未ログインで一覧にアクセスすると401() throws Exception {
			mockMvc.perform(get("/api/users"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@WithMockUser(roles = "USER")
		void 一般ユーザーが一覧にアクセスすると403() throws Exception {
			mockMvc.perform(get("/api/users"))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void 管理者が一覧にアクセスすると200() throws Exception {
			mockMvc.perform(get("/api/users"))
					.andExpect(status().isOk());
		}
	}

	@Nested
	class UpdateRoleTest {
		@Test
		void 未ログインで権限変更にアクセスすると401() throws Exception {
			mockMvc.perform(patch("/api/users/999999/role").with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ROLE_UPDATE_JSON))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@WithMockUser(roles = "USER")
		void 一般ユーザーが権限変更にアクセスすると403() throws Exception {
			mockMvc.perform(patch("/api/users/999999/role").with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ROLE_UPDATE_JSON))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void 管理者でもCSRFトークンが無いと403() throws Exception {
			mockMvc.perform(patch("/api/users/999999/role")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ROLE_UPDATE_JSON))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void 管理者かつ存在しないユーザーIDの場合は404() throws Exception {

			when(userService.updateRole(any(), any(), any())).thenThrow(new NoSuchElementException());

			mockMvc.perform(patch("/api/users/999999/role").with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ROLE_UPDATE_JSON))
					.andExpect(status().isNotFound()); // 404の確認
		}
	}

	@Nested
	class DeleteTest {
		@Test
		void 未ログインで削除にアクセスすると401() throws Exception {
			mockMvc.perform(delete("/api/users/999999").with(csrf()))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@WithMockUser(roles = "USER")
		void 一般ユーザーが削除にアクセスすると403() throws Exception {
			mockMvc.perform(delete("/api/users/999999").with(csrf()))
					.andExpect(status().isForbidden());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void 管理者でもCSRFトークンが無いと403() throws Exception {
			mockMvc.perform(delete("/api/users/999999"))
					.andExpect(status().isForbidden());
		}

		@Test
        @WithMockUser(roles = "ADMIN")
        void 管理者かつ存在しないユーザーIDの削除は404() throws Exception {

            doThrow(new NoSuchElementException()).when(userService).delete(999999L);

            mockMvc.perform(delete("/api/users/999999").with(csrf()))
                    .andExpect(status().isNotFound());
        }
	}
}