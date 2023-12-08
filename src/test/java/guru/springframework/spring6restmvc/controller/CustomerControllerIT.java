package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    CustomerController customerController;

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
    void testGetByIdNotFound() throws Exception {

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