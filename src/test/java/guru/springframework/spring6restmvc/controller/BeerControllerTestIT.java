package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


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
class BeerControllerTestIT {

    // Components to perform integration testing with are autowired by Spring Boot

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testListBeers() {
        List<BeerDTO> dtos = beerController.listBeers();

        log.debug("dtos.size() = " + dtos.size());
        log.debug("beerRepository.count() = " + beerRepository.count());

        //assertThat(dtos.size()).isEqualTo(beerRepository.count());
        assertThat(dtos.size()).isEqualTo(3);

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

        List<BeerDTO> dtos = beerController.listBeers();

        log.debug("dtos.size() = " + dtos.size());
        log.debug("beerRepository.count() = " + beerRepository.count());

        assertThat(dtos.size()).isEqualTo(beerRepository.count());

    }

}