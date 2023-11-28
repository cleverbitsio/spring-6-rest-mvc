package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class BeerServiceImpl implements BeerService {

    private Map<UUID, Beer> beerMap;

    public BeerServiceImpl() {
        this.beerMap = new HashMap<>();

        Beer beer1 = Beer.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Galaxy Cat")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("12356")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(122)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        Beer beer2 = Beer.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Crank")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("12356222")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(392)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        Beer beer3 = Beer.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Sunshine City")
                .beerStyle(BeerStyle.IPA)
                .upc("12356")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(144)
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        beerMap.put(beer1.getId(), beer1);
        beerMap.put(beer2.getId(), beer2);
        beerMap.put(beer3.getId(), beer3);
    }

    @Override
    public List<Beer> listBeers(){
        return new ArrayList<>(beerMap.values());
    }

    @Override
    public Beer getBeerById(UUID id) {

        log.debug("Get Beer by Id - in service. Id: " + id.toString());

        return beerMap.get(id);
    }

    @Override
    public Beer saveNewBeer(Beer beer) {

        Beer savedBeer = Beer.builder()
                .id(UUID.randomUUID())
                .version(beer.getVersion())
                .beerName(beer.getBeerName())
                .beerStyle(beer.getBeerStyle())
                .upc(beer.getUpc())
                .price(beer.getPrice())
                .quantityOnHand(beer.getQuantityOnHand())
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        beerMap.put(savedBeer.getId(), savedBeer);
        return savedBeer;
    }

    @Override
    public void updateBeerById(UUID beerId, Beer updatedBeer) {
        Beer existingbeer = getBeerById(beerId);

        existingbeer.setBeerName(updatedBeer.getBeerName());
        existingbeer.setBeerStyle(updatedBeer.getBeerStyle());
        existingbeer.setUpc(updatedBeer.getUpc());
        existingbeer.setPrice(updatedBeer.getPrice());
        existingbeer.setQuantityOnHand(updatedBeer.getQuantityOnHand());
        existingbeer.setUpdateDate(LocalDateTime.now());

        beerMap.replace(existingbeer.getId(), existingbeer);
    }

    @Override
    public void deleteBeerById(UUID beerId) {
        beerMap.remove(beerId);
    }

    @Override
    public void updateBeerPatchById(UUID beerId, Beer updatedBeer) {

        Beer existingbeer = getBeerById(beerId);

        if(updatedBeer.getBeerName() != null) {
            existingbeer.setBeerName(updatedBeer.getBeerName());
        }

        if(updatedBeer.getBeerStyle() != null) {
            existingbeer.setBeerStyle(updatedBeer.getBeerStyle());
        }

        if(updatedBeer.getUpc() != null) {
            existingbeer.setUpc(updatedBeer.getUpc());
        }

        if(updatedBeer.getPrice() != null) {
            existingbeer.setPrice(updatedBeer.getPrice());
        }

        if(updatedBeer.getQuantityOnHand() != null) {
            existingbeer.setQuantityOnHand(updatedBeer.getQuantityOnHand());
        }

        existingbeer.setUpdateDate(LocalDateTime.now());

        beerMap.replace(existingbeer.getId(), existingbeer);

    }
}
