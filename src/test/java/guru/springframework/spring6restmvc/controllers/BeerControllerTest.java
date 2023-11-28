package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.services.BeerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
@WebMvcTest(BeerController.class) // Test splice which is limited to the BeerController class
class BeerControllerTest {

    // No longer need to reference the BeerController class because we've used @WebMvcTest(BeerController.class)
    // which will autowire the BeerController into the Spring Context
    // @Autowired
    // BeerController beerController;

    // This setups Spring MockMVC component via DI via the Spring Framework
    @Autowired
    MockMvc mockMvc;

    // The BeerController has a dependency BeerService
    // we want to test this dependency - so lets set that up via DI
    // MockBean sets up a Mock of the BeerService into the Spring Context
    @MockBean
    BeerService beerService;

    @Test
    void getBeerById() throws Exception {
        // the get method matches the annotation on the BeerController classes getBeerById() method
        // this is how MockMVC knows to test getBeerById()
        // this will return 200 - so it will pass but no data is actually returned - we will fix that in the next branch
        ResultActions resultActions = mockMvc.perform(get("/api/v1/beer/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        printResultActions(resultActions);
    }

    @Test
    void listBeers() throws Exception {
        // this will return 200 - so it will pass but no data is actually returned - we will fix that in the next branch
        ResultActions resultActions = mockMvc.perform(get("/api/v1/beer")
                                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        printResultActions(resultActions);
    }

    private static void printResultActions(ResultActions resultActions) throws UnsupportedEncodingException {
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }

}