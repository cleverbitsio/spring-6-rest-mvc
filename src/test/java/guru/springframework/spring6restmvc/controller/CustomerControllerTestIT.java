package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Slf4j
class CustomerControllerTestIT {

    @Autowired
    CustomerController controller;

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testGetCustomers() {
        List<CustomerDTO> dtos = controller.listAllCustomers();
        assertThat(dtos.size()).isEqualTo(3);
    }
}