package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvc.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
class CustomerControllerIT {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerMapper customerMapper;

    @Test
    void testUpdateNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.updateCustomerByID(UUID.randomUUID(),
                    CustomerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void testUpdateExistingCustomerById() {

        // START: Generate Test Data
        // To do any integration testing between the controller and service
        // we need some data to test with

        // the data needs to already exist - so we get a customer from the repository
        Customer customerEntity = customerRepository.findAll().get(0);

        // Controllers use dtos so lets convert the test data to a dto
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customerEntity);

        // Next we prepare the test data to pass to customerController.updateCustomerByID(...);
        UUID customerId = customerDTO.getId();
        final String updatedCustomerName = "UPDATED";
        customerDTO.setName(updatedCustomerName);
        // END: Generate Test Data

        // Now run the controller method with the test data
        ResponseEntity responseEntity = customerController.updateCustomerByID(customerId, customerDTO);

        // Now we test if it worked with assertions
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(responseEntity.getStatusCode().toString()).isEqualTo(HttpStatus.NO_CONTENT.toString());
        assertThat(customerController.getCustomerById(customerId).getName()).isEqualTo(updatedCustomerName);
        assertThat(customerService.getCustomerById(customerId).get().getName()).isEqualTo(updatedCustomerName);
        assertThat(customerMapper.customerToCustomerDto(customerRepository.findById(customerId).get()).getName()).isEqualTo(updatedCustomerName);

    }

    @Test
    void testCustomerIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            customerController.getCustomerById(UUID.randomUUID());
        });

        try {
            log.debug("customerController.getCustomerById(UUID.randomUUID()) = " + customerController.getCustomerById(UUID.randomUUID()).getId().toString());
        }
        catch (NotFoundException notFoundException) {
            log.debug("Since we are using a random UUID we throw the notFoundException");
            log.debug("notFoundException.getMessage() = " + notFoundException.getMessage());
        }
    }

    @Rollback
    @Transactional
    @Test
    void saveNewCustomerTest() {
        final String name = "New Customer";
        CustomerDTO customerDTO = CustomerDTO.builder()
                    .name(name)
                    .build();

        ResponseEntity<CustomerDTO> responseEntity = customerController.handlePost(customerDTO);

        // Response code should be 201 = Created success status response code
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.CREATED.value()));

        // The header should return the location of the new created object
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();
        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        // Test the newly created entity has been created in the repository
        Customer savedCustomerEntity = customerRepository.findById(savedUUID).get();
        assertThat(savedCustomerEntity).isNotNull();
        assertThat(savedCustomerEntity.getId()).isEqualTo(savedUUID);
        assertThat(savedCustomerEntity.getName()).isEqualTo(name);
    }

    @Rollback
    @Transactional
    @Test
    void testListAllEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> dtos = customerController.listAllCustomers();

        log.debug("dtos.size() = " + dtos.size());
        log.debug("customerRepository.count() = " + customerRepository.count());

        assertThat(dtos.size()).isEqualTo(0);
    }

    @Test
    void testListAll() {
        List<CustomerDTO> dtos = customerController.listAllCustomers();
        log.debug("dtos.size() = " + dtos.size());
        log.debug("customerRepository.count() = " + customerRepository.count());
        assertThat(dtos.size()).isEqualTo(3);
    }

    @Test
    void testGetByIdNotFound() {

        UUID randomUUID = UUID.randomUUID();
        log.debug("randomUUID = " + randomUUID);
        for(Customer customer : customerRepository.findAll()) {
            log.debug("customer.getId() = " + customer.getId());
            if(customer.getId() == randomUUID) {
                log.debug("unexpected match between randomUUID and customer.getId() in repo");
            }
        }

        assertThrows(NotFoundException.class, () -> {
            customerController.getCustomerById(randomUUID);
        });
    }

    @Test
    void testGetById() {
        Customer customer = customerRepository.findAll().get(0);
        log.debug("customer.getId() = " + customer.getId());

        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());
        log.debug("dto.getId() = " + customerDTO.getId());

        assertThat(customerDTO).isNotNull();
    }
}