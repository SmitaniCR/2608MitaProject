package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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
import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.User;
import _Project.Mita.entity.enums.ReservationStatus;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.CategoryRepository;
import _Project.Mita.repository.ReservationRepository;
import _Project.Mita.repository.UserRepository;
import _Project.Mita.security.UserPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationApiControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// 1. 一般ユーザー（USER）向けの正常系・異常系テスト

	@Test
	@Transactional
	void 一般ユーザーが書籍を予約できる() throws Exception {
		// 1. 準備
		Category category = new Category();
		category.setCategoryName("本");
		categoryRepository.save(category);

		Book book = new Book();
		book.setTitle("テスト本");
		book.setCategory(category);
		book.setAvailableCopies(0);
		bookRepository.save(book);

		User userEntity = new User();
		userEntity.setName("予子");
		userEntity.setEmail("yoko@com");
		userEntity.setPassword(passwordEncoder.encode("pasuwado"));
		userRepository.save(userEntity);

		UserPrincipal principal = new UserPrincipal(userEntity);

		// 2. 実行
		String requestJson = String.format("{\"bookId\": %d}", book.getBookId());
		mockMvc.perform(post("/api/reservations")
				.with(user(principal)) // 本物のPrincipalを注入
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookId").value(book.getBookId()));
	}

	@Test
	@WithMockUser(roles = "USER")
	void 予約登録時にbookIdがnullだと400が返る() throws Exception {
		mockMvc.perform(post("/api/reservations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bookId\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
				.andExpect(jsonPath("$.fieldErrors.bookId").value("書籍IDは必須です"));
	}

	@Test
	@Transactional
	void 一般ユーザーが自身の予約をキャンセルできる() throws Exception {
		// 1. 準備：ユーザー、書籍、そして予約データを事前に作成・保存する
		User userEntity = new User();
		userEntity.setName("一郎");
		userEntity.setEmail("1ro@com");
		userEntity.setPassword(passwordEncoder.encode("pasuwado"));
		userRepository.save(userEntity);

		Category category = new Category();
		category.setCategoryName("本");
		categoryRepository.save(category);

		Book book = new Book();
		book.setTitle("テスト本");
		book.setCategory(category);
		book.setAvailableCopies(0);
		bookRepository.save(book);

		LocalDateTime baseTime = LocalDateTime.of(2026, 8, 27, 10, 0);

		Reservation reservation = new Reservation();
		reservation.setUser(userEntity);
		reservation.setBook(book);
		reservation.setStatus(ReservationStatus.WAITING);
		reservation.setReservedAt(baseTime);

		reservationRepository.save(reservation);

		UserPrincipal principal = new UserPrincipal(userEntity);

		// 2. 実行・検証
		mockMvc.perform(post("/api/reservations/" + reservation.getReservationId() + "/cancel")
				.with(user(principal))
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
    @Transactional
    void 自身の予約一覧にアクセスできる() throws Exception {
    	User userEntity = new User();
        userEntity.setName("予子");
        userEntity.setEmail("yoko@com");
        userEntity.setPassword(passwordEncoder.encode("pasuwado"));
        userRepository.save(userEntity);
    	
        UserPrincipal principal = new UserPrincipal(userEntity);
        
        mockMvc.perform(get("/api/reservations/me")
                .with(user(principal)))
                .andExpect(status().isOk());
    }

	// 2. 管理者向けAPIの認可・セキュリティテスト

	@Test
	void 未ログインで管理者向け予約一覧にアクセスすると401() throws Exception {
		mockMvc.perform(get("/api/reservations"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 一般ユーザーが管理者向け予約一覧にアクセスすると403() throws Exception {
		mockMvc.perform(get("/api/reservations"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者は予約一覧を取得できる() throws Exception {
		mockMvc.perform(get("/api/reservations"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void 管理者はCSRFトークン付きで強制キャンセルAPIにアクセスできる() throws Exception {
		// 存在しないID(999999)を叩いてNoSuchElementException（404等にハンドリングされている想定）を確認
		mockMvc.perform(post("/api/reservations/999999/admin-cancel").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void CSRFトークンが無い場合は強制キャンセルAPIが拒否される() throws Exception {
		mockMvc.perform(post("/api/reservations/999999/admin-cancel"))
				.andExpect(status().isForbidden());
	}
}
