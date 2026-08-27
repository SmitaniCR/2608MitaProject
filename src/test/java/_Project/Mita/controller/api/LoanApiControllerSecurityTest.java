package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.entity.User;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.CategoryRepository;
import _Project.Mita.repository.UserRepository;
import _Project.Mita.security.UserPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
class LoanApiControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	//正常系テストの追加（WithMockUserはSessionUserService(Principal)をデコイできない）
	@Test
	@Transactional // 実DBを汚さないようロールバックさせる
	void 一般ユーザーが在庫のある書籍を貸出できる() throws Exception {
		
		// 1. 準備：実際にUserとBook(在庫あり)をDBに保存する
		
		Category category = new Category();
		category.setCategoryName("教科書");
		categoryRepository.save(category);

		Book book = new Book();
		book.setTitle("Java入門");
		book.setCategory(category);
		bookRepository.save(book);

		User userEntity = new User();
		userEntity.setName("貸子");
		userEntity.setEmail("a@com");
		userEntity.setPassword(passwordEncoder.encode("pasuwado"));
		userRepository.save(userEntity);

		// 2. UserPrincipalでラップ

		UserPrincipal principal = new UserPrincipal(userEntity);

		// 3. 実行
		
		String requestJson = String.format("{\"bookId\": %d}", book.getBookId());
		mockMvc.perform(post("/api/loans")
				.with(user(principal)) // WithMockUserの代わりに本物のPrincipalをデコイ（注入）する
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())// status().isCreated() (201) を確認
				.andExpect(jsonPath("$.bookId").value(book.getBookId()));// レスポンスに貸出された本の情報が含まれているか等を検証
	}

	@Test
	@WithMockUser(roles = "USER")
	void 貸出登録時にbookIdがnullだと400が返る() throws Exception {//@Validのエラー検証
		mockMvc.perform(post("/api/loans")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bookId\":null}")) // bookIdがnullのJSON
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
				.andExpect(jsonPath("$.fieldErrors.bookId").value("書籍IDは必須です"));
	}

	@Test
	void 未ログインで管理者向け貸出一覧にアクセスすると401() throws Exception {
		mockMvc.perform(get("/api/loans"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーが管理者向け貸出一覧にアクセスすると403() throws Exception {
		mockMvc.perform(get("/api/loans"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者は貸出一覧を取得できる() throws Exception {
		mockMvc.perform(get("/api/loans"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者はCSRFトークン付きで強制返却APIにアクセスできる() throws Exception {
		mockMvc.perform(post("/api/loans/999999/admin-return").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void CSRFトークンが無い場合は強制返却APIが拒否される() throws Exception {
		mockMvc.perform(post("/api/loans/999999/admin-return"))
				.andExpect(status().isForbidden());
	}

	@Test
	void 未ログインで貸出CSVエクスポートにアクセスすると401() throws Exception {
		mockMvc.perform(get("/api/loans/export"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーが貸出CSVエクスポートにアクセスすると403() throws Exception {
		mockMvc.perform(get("/api/loans/export"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者は貸出CSVエクスポートを取得できる() throws Exception {
		mockMvc.perform(get("/api/loans/export"))
				.andExpect(status().isOk());
	}
}
