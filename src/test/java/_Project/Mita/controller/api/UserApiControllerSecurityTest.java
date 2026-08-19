package _Project.Mita.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class UserApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ROLE_UPDATE_JSON = "{\"isAdmin\":true}";

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
}
