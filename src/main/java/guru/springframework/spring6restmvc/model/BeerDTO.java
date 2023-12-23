package guru.springframework.spring6restmvc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by jt, Spring Framework Guru.
 */
@Builder
@Data
public class BeerDTO {
    private UUID id;
    private Integer version;

    @NotBlank
    @NotNull
    // this prevents exceptions hitting the JPA layer, if the beerName is too long
    // I am commenting this out to demonstrate exception handling in jpa layer using custom JPA validation Error Handling
    //But moving forward, i will include @Size(max = 30) as a validation on the dto
    // so that validation fails before the JPA layer - which gives us free and contextual error handling/messaging
    // i.e [{"beerName":"size must be between 0 and 30"}]
    @Size(max = 30)
    private String beerName;

    @NotNull
    private BeerStyle beerStyle;

    @NotBlank
    @NotNull
    private String upc;

    private Integer quantityOnHand;

    @NotNull
    private BigDecimal price;
    private LocalDateTime createdDate;
    private LocalDateTime updateDate;
}
