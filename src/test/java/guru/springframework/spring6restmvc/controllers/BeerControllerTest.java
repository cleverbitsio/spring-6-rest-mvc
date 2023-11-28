package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BeerService beerService;

    // Here we are going to tell Mockito how to return actual data
    // using the BeerServiceImpl class - since it already contains data which we manually mocked earlier in the course
    BeerServiceImpl beerServiceImpl = new BeerServiceImpl();

    @Test
    void getBeerById() throws Exception {

        // the BeerServiceImpl class method listBeers returns a bunch of manually mocked data.
        // here we get the first one from the list
        Beer testBeer = beerServiceImpl.listBeers().get(0);

        // previously Mockito was returning a null body and null content type
        // here we tell Mockito to return testBeer for any UUID object passed to the getBeerById method of the BeerService
        // given any (any is a matcher) UUID object, we will return our testBeer object
        given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer);

        ResultActions resultActions = mockMvc.perform(get("/api/v1/beer/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        printResultActions(resultActions);
    }

    @Test
    void listBeers() throws Exception {

        // the BeerServiceImpl class method listBeers returns a bunch of manually mocked data.
        List<Beer> testBeers = beerServiceImpl.listBeers();

        // previously Mockito was returning a null body and null content type
        // here we tell Mockito to return testBeers when we call the BeerService listBeers method
        // given the beerService listBeers method, we will return our testBeers object
        given(beerService.listBeers()).willReturn(testBeers);

        ResultActions resultActions = mockMvc.perform(get("/api/v1/beer")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        printResultActions(resultActions);
    }

    private static void printResultActions(ResultActions resultActions) throws UnsupportedEncodingException {
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }

}