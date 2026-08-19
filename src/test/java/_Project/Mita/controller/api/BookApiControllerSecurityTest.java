package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String INVALID_BOOK_JSON = "{}";
    private static final String INVALID_CATEGORY_JSON = "{}";

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
    void 未ログインでカテゴリ登録にアクセスすると401() throws Exception {
        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_CATEGORY_JSON))
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
    void 管理者はカテゴリ登録APIに到達できる() throws Exception {
        mockMvc.perform(post("/api/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_CATEGORY_JSON))
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
