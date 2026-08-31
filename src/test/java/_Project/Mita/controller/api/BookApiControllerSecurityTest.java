package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String INVALID_BOOK_JSON = "{}";
    
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void 書籍登録時に複数項目が違反しているとすべてfieldErrorsに入る() throws Exception {
        // titleがnull、かつ totalCopiesがマイナス(-1)の不正なJSON
        String multipleErrorsJson = "{\"title\":null, \"totalCopies\":-1}";

        mockMvc.perform(post("/api/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(multipleErrorsJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
                .andExpect(jsonPath("$.fieldErrors.title").value("タイトルは必須です"))// 2つのエラーが両方ともMapに入っていることを検証
                .andExpect(jsonPath("$.fieldErrors.totalCopies").value("総冊数は0以上で入力してください"));
    }

    
    @Test
	@WithMockUser(roles = "USER")
	void 書籍登録時にtitleがnullだと400が返る() throws Exception {//@Validのエラー検証
		mockMvc.perform(post("/api/books")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":null}")) // titleがnullのJSON
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("入力内容に誤りがあります"))
				.andExpect(jsonPath("$.fieldErrors.title").value("タイトルは必須です"));
	}

    @Test
    void 未ログインで書籍登録にアクセスすると401() throws Exception {
        mockMvc.perform(post("/api/books").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BOOK_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 一般ユーザーが書籍登録にアクセスすると403() throws Exception {
        mockMvc.perform(post("/api/books").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BOOK_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 一般ユーザーが書籍更新にアクセスすると403() throws Exception {
        mockMvc.perform(put("/api/books/999999").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BOOK_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 一般ユーザーが書籍削除にアクセスすると403() throws Exception {
        mockMvc.perform(delete("/api/books/999999").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 管理者は書籍登録APIに到達できる() throws Exception {
        mockMvc.perform(post("/api/books").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BOOK_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 未ログインで書籍CSVエクスポートにアクセスすると401() throws Exception {
        mockMvc.perform(get("/api/books/export"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 一般ユーザーが書籍CSVエクスポートにアクセスすると403() throws Exception {
        mockMvc.perform(get("/api/books/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 管理者は書籍CSVエクスポートを取得できる() throws Exception {
        mockMvc.perform(get("/api/books/export"))
                .andExpect(status().isOk());
    }

    @Test
    void 未ログインでも書籍サジェストは利用できる() throws Exception {
        mockMvc.perform(get("/api/books/suggest").param("keyword", "テスト"))
                .andExpect(status().isOk());
    }
}
