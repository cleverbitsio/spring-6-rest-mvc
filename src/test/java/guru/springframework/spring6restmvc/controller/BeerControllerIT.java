package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Here we want to test the interaction between the service and the controller i.e. an integration test!
// previously we were using the test splice (@DataJpaTest) to specifically test the JPA repository (a unit test)
// Here we are bringing up the full Spring Boot context (using @SpringBootTest) which includes the BootStrapData
// that is going to run and populate the H2 database - this gives us a known set of data we can run our tests with
// previous to this and the test splice, we were testing the interaction of the controller and the framework using
// MockMVC - i.e. within the web context.
// Now we want to test the controller and its interaction with the JPA service layer - and we can call the controller
// directly - because in this case we are testing the controller methods as if were the spring framework
// i.e. we don't have the web context, we're not dealing with the web context specifically, instead we are looking at
// the interaction of the controller with the underlying service layer
@SpringBootTest
@Slf4j
class BeerControllerIT {

    // Components to perform integration testing with are autowired by Spring Boot

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void testListBeersByStyleAndName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)));
    }

    @Test
    void testListBeersByStyle() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                        .queryParam("beerStyle",BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(548)));
    }

    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(get(BeerController.BEER_PATH)
                .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(336)));
    }

    @Test
    void testPatchBeerBadName() throws Exception {
        Beer beer = beerRepository.findAll().get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew NameNew Name");
        beerMap.put("price", "0.99");
        beerMap.put("upc", "0.99");
        beerMap.put("beerStyle", "PALE_ALE");

        ResultActions resultActions =
                mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(beerMap)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.length()", is(1)));

        Assertions.assertThat(resultActions.andReturn().getResponse().getStatus()).isEqualTo(400);

        MockMvCHelper.printResultActions(resultActions);
    }

    @Test
    void testPatchBeerByIDNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.updateBeerPatchById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void patchExistingBeer() {

        // START: Mock data to emulate Patch request data ---------
        // Get a beer entity from the repository to update
        Beer beer = beerRepository.findAll().get(0);

        // Convert the entity to a dto before make changes to it
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);

        // Change the returned dto as if it was being sent via a patch endpoint
        final String beerName = "UPDATED BY PATCH";
        beerDTO.setBeerName(beerName);
        log.debug("beerDTO.getBeerName() = " + beerDTO.getBeerName());
        // END: Mock data to emulate Put request data ---------

        // Now use the mocked data to emulate a PATCH request
        ResponseEntity responseEntity = beerController.updateBeerPatchById(beer.getId(), beerDTO);

        // Assert that the status code returned is a 204
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        log.debug("responseEntity.getStatusCode() = " + responseEntity.getStatusCode());

        // Assert that the database has updated the entity by reading it back and comparing beerName
        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        log.debug("updatedBeer.getId() = " + updatedBeer.getId());
        log.debug("updatedBeer.getBeerName() = " + updatedBeer.getBeerName());
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void testDeleteByIDNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.deleteById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity responseEntity = beerController.deleteById(beer.getId());

        // here I test that responseEntity returns the response code 204
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        // After deleting the entity, check if we throw the expected exceptions when trying to find the deleted beer:
        // 1. Check foundBeer is null
        // The getBeerById controller method calls the BeerService.getBeerById(xxx)
        // The BeerServiceJPA in turn calls the beerRepositiory.findById(xxx).orElse(null)
        // which handles the expected NoSuchElementException using orElse(null)
        // I have used the same beerRepositiory.findById(xxx).orElse(null) method to handle the exception and instead
        // return null
        Beer foundBeer = beerRepository.findById(beer.getId()).orElse(null);
        assertThat(foundBeer).isNull();

        // 2. here I test that beerRepository.findById(beer.getId()) is empty - similar to 1. Check foundBeer is Null
        assertThat(beerRepository.findById(beer.getId()).isEmpty());

        // 3. Here I test beerRepository.findById(beer.getId()).get() w/o .orElse(null)
        // I do this out of curiousity and check that the expected exception NoSuchElementException is thrown
        assertThrows(NoSuchElementException.class, () -> {
            beerRepository.findById(beer.getId()).get();
        });

        // 4. here I check the controller throws the expected custom higher level NotFoundException
        // when trying to get the deleted item
        assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(beer.getId());
        });
    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> {
           beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {

        // START: Mock data to emulate Put request data ---------
        // Get a beer entity from the repository to update
        Beer beer = beerRepository.findAll().get(0);

        // Convert the entity to a dto before make changes to it
        BeerDTO beerDTO = beerMapper.beerToBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        // Change the returned dto as if it was being sent via a put endpoint
        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);
        log.debug("beerDTO.getBeerName() = " + beerDTO.getBeerName());
        // END: Mock data to emulate Put request data ---------

        // Now use the mocked data to emulate a PUT request
        ResponseEntity responseEntity = beerController.updateById(beer.getId(), beerDTO);

        // Assert that the status code returned is a 204
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        log.debug("responseEntity.getStatusCode() = " + responseEntity.getStatusCode());

        // Assert that the database has updated the entity by reading it back and comparing beerName
        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        log.debug("updatedBeer.getId() = " + updatedBeer.getId());
        log.debug("updatedBeer.getBeerName() = " + updatedBeer.getBeerName());
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Rollback
    @Transactional
    @Test
    void saveNewBeerTest() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New Beer")
                .build();

        ResponseEntity responseEntity = beerController.handlePost(beerDTO);

        // Response code should be 201 = Created success status response code
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.CREATED.value()));

        // The header should return the location of the new created object
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();
        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        // beerRepository.findById(xxx) returns an optional, so you got to call it with .get on the end
        Beer savedBeer = beerRepository.findById(savedUUID).get();
        assertThat(savedBeer).isNotNull();

        log.debug("responseEntity.getStatusCode() = " + responseEntity.getStatusCode());
        log.debug("responseEntity.getHeaders().getLocation() = " + responseEntity.getHeaders().getLocation());
        log.debug("responseEntity.getHeaders().getLocation().getPath().split(\"/\")[4] = " + responseEntity.getHeaders().getLocation().getPath().split("/")[4]);
        log.debug("savedBeer.getId() = " + savedBeer.getId());
        log.debug("savedBeer.getBeerName() = " + savedBeer.getBeerName());
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(UUID.randomUUID());
        });

        try {
            log.debug("beerController.getBeerById(UUID.randomUUID()).getId().toString() = " + beerController.getBeerById(UUID.randomUUID()).getId().toString());
        }
        catch (NotFoundException notFoundException) {
            log.debug("Since we are using a random UUID we throw the notFoundException");
            log.debug("notFoundException.getMessage() = " + notFoundException.getMessage());
        }

//        // The below assert will fail
//        Beer beer = beerRepository.findAll().get(0);
//        assertThrows(NotFoundException.class, () -> {
//            beerController.getBeerById(beer.getId());
//        });
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().get(0);

        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

    @Test
    void testListBeers() {
        List<BeerDTO> dtos = beerController.listBeers(null, null, false);

        log.debug("dtos.size() = " + dtos.size());
        log.debug("beerRepository.count() = " + beerRepository.count());

        //assertThat(dtos.size()).isEqualTo(beerRepository.count());
        assertThat(dtos.size()).isEqualTo(2413);

    }

    // The test is to get an empty list back but not throw an exception
    // This test changes the state of the database and will make testListBeers fail if testEmptyListBeers runs first
    // To fix this we want to roll back to the original database state after running testEmptyListBeers
    // This is done with the @Rollback annotation
    // The @Transactional indicates we are altering the database state
    @Rollback
    @Transactional
    @Test
    void testEmptyListBeers() {

        beerRepository.deleteAll();
        List<BeerDTO> dtos = beerController.listBeers(null, null, false);

        log.debug("dtos.size() = " + dtos.size());
        log.debug("beerRepository.count() = " + beerRepository.count());

        assertThat(dtos.size()).isEqualTo(0);

    }

}