package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.UnsupportedEncodingException;


@WebMvcTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    BeerServiceImpl beerServiceImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<Beer> beerArgumentCaptor;

    // Here we are going to tell Mockito how to return actual data
    // using the BeerServiceImpl class - since it already contains data which we manually mocked earlier in the course
    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testPatchBeer() throws Exception {
        Beer beer = beerServiceImpl.listBeers().get(0);

        // Use a map object to create some json to patch with
        // this needs to be used with jackson to convert it to json
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName","New Name");

        ResultActions resultActions =
                mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());

        printResultActions(resultActions);

    }

    @Test
    void testDeleteBeer() throws Exception {
        Beer beer = beerServiceImpl.listBeers().get(0);

        ResultActions resultActions =
                mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService).deleteById(uuidArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());

        printResultActions(resultActions);
    }

    @Test
    void testUpdateBeer() throws Exception {
        Beer beer = beerServiceImpl.listBeers().get(0);
        beer.setBeerName("updated beer name");

        ResultActions resultActions =
                mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(beer.getId(), beer);

        printResultActions(resultActions);
    }

    @Test
    void testCreateNewBeer() throws Exception {
        Beer beer = beerServiceImpl.listBeers().get(0);
        // reset the id and version so that we can re-use beer as a new beer object
        beer.setId(UUID.randomUUID());
        beer.setVersion(null);

        given(beerService.saveNewBeer(any(Beer.class))).willReturn(beer);

        ResultActions resultActions =
                mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        printResultActions(resultActions);

    }

    @Test
    void testListBeers() throws Exception {

        // the BeerServiceImpl class method listBeers returns a bunch of manually mocked data.
        List<Beer> testBeers = beerServiceImpl.listBeers();

        // previously Mockito was returning a null body and null content type
        // here we tell Mockito to return testBeers when we call the BeerService listBeers method
        // given the beerService listBeers method, we will return our testBeers object
        given(beerService.listBeers()).willReturn(testBeers);

        ResultActions resultActions =
                mockMvc.perform(get(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.length()", is(testBeers.size())));

        printResultActions(resultActions);
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        // we don't want the service to throw an exception
        // instead we want the controller to throw it
        // given(beerService.getBeerById(any(UUID.class))).willThrow(NotFoundException.class);
        // we are now returning an optional of empty and moving the logic handling the exception to the controller
        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        // the BeerServiceImpl class method listBeers returns a bunch of manually mocked data.
        // here we get the first one from the list
        Beer testBeer = beerServiceImpl.listBeers().get(0);

        // previously Mockito was returning a null body and null content type
        // here we tell Mockito to return testBeer for any UUID object passed to the getBeerById method of the BeerService
        // given the testBeer's id, we will return the testBeer object
        // in other words given that specific UUID we will return testBeer
        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        ResultActions resultActions =
                mockMvc.perform(get(BeerController.BEER_PATH_ID, testBeer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));

        printResultActions(resultActions);
    }

    private static void printResultActions(ResultActions resultActions) throws UnsupportedEncodingException {
        System.out.println("Response Content: " + resultActions.andReturn().getResponse().getContentAsString());
        System.out.println("Status Code: " + resultActions.andReturn().getResponse().getStatus());
    }

}