package com.list.todo.it;

import com.list.todo.TodoListApplication;
import com.list.todo.configurations.H2TestProfileJPAConfig;
import com.list.todo.controllers.TokenController;
import com.list.todo.payload.JwtAuthenticationResponse;
import com.list.todo.security.UserPrincipal;
import com.list.todo.services.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TodoListApplication.class, H2TestProfileJPAConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TokenTest {

    private final static Long CURRENT_USER_ID = 1L;
    private final static String CURRENT_USERNAME = "username";

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationServiceMock;

    @InjectMocks
    private TokenController tokenControllerMock;

    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            UserPrincipal userPrincipal = new UserPrincipal();
            userPrincipal.setId(CURRENT_USER_ID);
            userPrincipal.setUsername(CURRENT_USERNAME);
            userPrincipal.setPassword(new BCryptPasswordEncoder().encode("password"));

            return userPrincipal;
        }
    };

    private HandlerMethodArgumentResolver putSupportParameters = new HandlerMethodArgumentResolver() {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(Pageable.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return PageRequest.of(0, 50);
        }
    };

    @Before
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tokenControllerMock)
                .setCustomArgumentResolvers(putAuthenticationPrincipal, putSupportParameters)
                .build();
    }

    @Test
    public void updateAccessToken_ReturnsAUpdatedToken() throws Exception {
        //arrange
        String jwt = "nkgnjksgnkg";
        when(authenticationServiceMock.updateAccessToken()).thenReturn(new JwtAuthenticationResponse(jwt));

        //act, assert
        this.mockMvc.perform(post("/api/token/update"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        verify(authenticationServiceMock, times(1)).updateAccessToken();

    }
}
