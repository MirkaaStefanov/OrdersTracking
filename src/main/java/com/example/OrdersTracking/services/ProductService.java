package com.example.OrdersTracking.services;

import com.example.OrdersTracking.dtos.ProductDTO;
import com.example.OrdersTracking.enums.MenuCategory;
import com.example.OrdersTracking.models.Product;
import com.example.OrdersTracking.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ModelMapper mapper;
    private static final BigDecimal EURO_EXCHANGE_RATE = new BigDecimal("1.95583");

    public List<Product> findAll(Boolean available, MenuCategory category) {
        List<Product> items = repository.findAll();

        return items.stream()
                .filter(item -> (available == null || item.isAvailable() == available) &&
                        (category == null || item.getCategory() == category))
                .collect(Collectors.toList());
    }

    public ProductDTO findById(Long id) {
        Product item = repository.findById(id).orElseThrow();
        ProductDTO dto = mapper.map(item, ProductDTO.class);
        return dto;
    }

    public ProductDTO save(ProductDTO dto) {
        Product entity = mapper.map(dto, Product.class);
        BigDecimal euroPrice = entity.getPrice().divide(EURO_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        entity.setEuroPrice(euroPrice);

        Product savedEntity = repository.save(entity);
        ProductDTO savedDto = mapper.map(savedEntity, ProductDTO.class);


        return savedDto;
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        Product updated = mapper.map(dto, Product.class);

        BigDecimal euroPrice = dto.getPrice().divide(EURO_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        updated.setEuroPrice(euroPrice);

        updated.setId(id);
        return mapper.map(repository.save(updated), ProductDTO.class);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public ProductDTO toggleAvailability(Long id) throws ChangeSetPersister.NotFoundException {
        Product item = repository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        item.setAvailable(!item.isAvailable());
        return mapper.map(repository.save(item), ProductDTO.class);
    }

    public List<Product> getMenuForOrder(){
        List<Product> allProducts = repository.findAll();

        List<Product> availableProducts = allProducts.stream()
                .filter(product -> product.isAvailable()) // or product.getAvailable()
                .collect(Collectors.toList());

        // 3. Return the filtered list
        return availableProducts;
    }
}

