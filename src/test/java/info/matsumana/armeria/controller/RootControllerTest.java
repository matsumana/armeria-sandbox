package info.matsumana.armeria.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import info.matsumana.armeria.TextContext;

@SpringJUnitConfig(TextContext.class)
@WebMvcTest(RootController.class)
public class RootControllerTest {

    @Inject
    private MockMvc mvc;

    @Test
    public void Root() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string("index"));
    }
}
