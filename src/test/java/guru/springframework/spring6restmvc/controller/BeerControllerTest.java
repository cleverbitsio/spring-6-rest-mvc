package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    // Here we are going to tell Mockito how to return actual data
    // using the BeerServiceImpl class - since it already contains data which we manually mocked earlier in the course
    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers().get(0);

        // Use a map object to create some json to patch with
        // this needs to be used with jackson to convert it to json
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");

        // Mockito is provided an empty Optional of a BeerDTO which is its default
        // which means this test causes the controller to throw a new NotFoundException
        // and therefore returning a 404, because NotFoundException has the
        // @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Value Not Found") annotation
        // however in this test we should use a real Optional of BeerDTO object
        // so lets setup Mockito to use our beer object using the given method
        // here we are telling Mockito, when we call the beerService.patchBeerById, for any beerId and any BeerDTO
        // we return an Optional of our BeerDTO objection called beer
        given(beerService.patchBeerById(any(), any())).willReturn(Optional.of(beer));

        ResultActions resultActions =
                mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());

        MockMvCHelper.printResultActions(resultActions);

    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers().get(0);

        // After updating deleteById to return a Boolean (true if the id exists in map) in BeerServiceImpl
        // this test started to fail
        // because Mockito by default was returning false or null
        // however the expected return value should be true if we pass a valid beerId
        // so lets use the given method to ensure we always return true
        given(beerService.deleteById(any())).willReturn(true);

        ResultActions resultActions =
                mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService).deleteById(uuidArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());

        MockMvCHelper.printResultActions(resultActions);
    }

    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers().get(0);
        beer.setBeerName("updated beer name");

        // Mockito is provided an empty Optional of a BeerDTO which is its default
        // which means this test causes the controller to throw a new NotFoundException
        // and therefore returning a 404, because NotFoundException has the
        // @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Value Not Found") annotation
        // however in this test we should use a real Optional of BeerDTO object
        // so lets setup Mockito to use our beer object using the given method
        // here we are telling Mockito, when we call the beerService.updateBeerById, for any beerId and any BeerDTO
        // we return an Optional of our BeerDTO objection called beer
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));

        ResultActions resultActions =
                mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(beer.getId(), beer);

        MockMvCHelper.printResultActions(resultActions);
    }

    @Test
    void testCreateNewBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers().get(0);
        beer.setVersion(null);
        beer.setId(null);

        // Assume that when we call the saveNewBeer method for the BeeService, it will return a valid BeerDTO
        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers().get(1));

        ResultActions resultActions =
        mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        MockMvCHelper.printResultActions(resultActions);
    }

    @Test
    void testCreateBeerNullBeerName() throws Exception {

        //Create an empty beerDTO object
        BeerDTO beerDTO = BeerDTO.builder().build();

        // the assumption when saving any BeerDTO object, we return a valid BeerDTO object
        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers().get(1));

        // we expect a bad data request because we have not set the beer name
        MvcResult mvcResult = mockMvc.perform(post(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                         .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                // we expect 2 validation errors ([{"beerName":"must not be null"},{"beerName":"must not be blank"}])
                .andExpect(jsonPath("$.length()", is(2)))
                .andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
        MockMvCHelper.printMvcResult(mvcResult);

    }

    @Test
    void testListBeers() throws Exception {

        // the BeerServiceImpl class method listBeers returns a bunch of manually mocked data.
        List<BeerDTO> testBeers = beerServiceImpl.listBeers();

        // previously Mockito was returning a null body and null content type
        // here we tell Mockito to return a list of BeerDTOs when we call the
        // BeerService listBeers method
        // given the beerService listBeers method, we will return our testBeers object
        given(beerService.listBeers()).willReturn(testBeers);
        // added this additional given to match tutor code - so diff is easier to see
        given(beerService.listBeers()).willReturn(beerServiceImpl.listBeers());

        ResultActions resultActions =
                mockMvc.perform(get(BeerController.BEER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.length()", is(testBeers.size())));

        MockMvCHelper.printResultActions(resultActions);
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
        BeerDTO testBeer = beerServiceImpl.listBeers().get(0);

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

        MockMvCHelper.printResultActions(resultActions);
    }
}