package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoanApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
