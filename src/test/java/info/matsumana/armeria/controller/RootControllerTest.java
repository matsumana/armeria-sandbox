package info.matsumana.armeria.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import info.matsumana.armeria.TextContext;

@SpringJUnitConfig(TextContext.class)
@WebMvcTest(RootController.class)
public class RootControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void Root() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
           .andExpect(status().isOk())
           .andExpect(content().string("index"));
    }
}
