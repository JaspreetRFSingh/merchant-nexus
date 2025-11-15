package com.merchant.nexus.service;

import com.merchant.nexus.dto.ProductDTO;
import com.merchant.nexus.exception.BusinessException;
import com.merchant.nexus.exception.ResourceNotFoundException;
import com.merchant.nexus.model.Product;
import com.merchant.nexus.repository.ProductDocument;
import com.merchant.nexus.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Catalog operations - demonstrates Elasticsearch integration, search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final ProductSearchRepository productSearchRepository;
    private final CatalogEventPublisher eventPublisher;

    @Transactional
    public ProductDTO.ProductResponse createProduct(ProductDTO.CreateProductRequest request, String merchantId) {
        Product.Money price = Product.Money.ofKRW(request.getPriceAmount());
        Product.Money costPrice = Product.Money.ofKRW(request.getCostPriceAmount());

        Product product = Product.createNew(
                merchantId,
                request.getName(),
                request.getCategory(),
                price,
                request.getStockQuantity()
        );

        // Update additional fields
        Product updatedProduct = Product.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .name(product.getName())
                .description(request.getDescription())
                .category(product.getCategory())
                .tags(request.getTags())
                .price(price)
                .costPrice(costPrice)
                .stockQuantity(product.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .imageUrls(request.getImageUrls())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        ProductDocument document = ProductDocument.fromProduct(updatedProduct);
        ProductDocument savedDocument = productSearchRepository.save(document);

        eventPublisher.publishProductCreated(savedDocument.toProduct());

        log.info("Created product with id: {}", savedDocument.getId());
        return ProductDTO.toResponse(savedDocument.toProduct());
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductResponse getProductById(String id) {
        ProductDocument document = productSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ProductDTO.toResponse(document.toProduct());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.ProductResponse> getProductsByMerchantId(String merchantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductDocument> documentPage = productSearchRepository.findByMerchantId(merchantId, pageable);
        return documentPage.stream()
                .map(ProductDocument::toProduct)
                .map(ProductDTO::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.ProductResponse> searchProducts(String keyword, String category,
                                                            BigDecimal minPrice, BigDecimal maxPrice,
                                                            String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDocument> documentPage;

        if (keyword != null && !keyword.isBlank()) {
            if (minPrice != null && maxPrice != null) {
                documentPage = productSearchRepository.searchByKeywordAndPriceRange(
                        keyword, minPrice, maxPrice, pageable);
            } else {
                documentPage = productSearchRepository.searchProducts(keyword, pageable);
            }
        } else if (category != null && !category.isBlank()) {
            documentPage = productSearchRepository.findByCategory(category, pageable);
        } else if (minPrice != null && maxPrice != null) {
            documentPage = productSearchRepository.findByPriceRange(minPrice, maxPrice, pageable);
        } else if (status != null && !status.isBlank()) {
            documentPage = productSearchRepository.findByStatus(status, pageable);
        } else {
            documentPage = productSearchRepository.findAll(pageable);
        }

        return documentPage.stream()
                .map(ProductDocument::toProduct)
                .map(ProductDTO::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO.ProductResponse updateProduct(String id, ProductDTO.UpdateProductRequest request) {
        ProductDocument existing = productSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Product product = existing.toProduct();

        // Update fields
        Product updatedProduct = Product.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .name(request.getName() != null ? request.getName() : product.getName())
                .description(request.getDescription() != null ? request.getDescription() : product.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : product.getCategory())
                .tags(request.getTags() != null ? request.getTags() : product.getTags())
                .price(request.getPriceAmount() != null ? 
                    Product.Money.ofKRW(request.getPriceAmount()) : product.getPrice())
                .costPrice(request.getCostPriceAmount() != null ?
                    Product.Money.ofKRW(request.getCostPriceAmount()) : product.getCostPrice())
                .stockQuantity(request.getStockQuantity() != null ? 
                    request.getStockQuantity() : product.getStockQuantity())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : product.getImageUrl())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : product.getImageUrls())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(java.time.Instant.now())
                .build();

        updatedProduct.activate();

        ProductDocument updatedDocument = ProductDocument.fromProduct(updatedProduct);
        ProductDocument savedDocument = productSearchRepository.save(updatedDocument);

        eventPublisher.publishProductUpdated(savedDocument.toProduct());

        log.info("Updated product with id: {}", id);
        return ProductDTO.toResponse(savedDocument.toProduct());
    }

    @Transactional
    public ProductDTO.ProductResponse activateProduct(String id) {
        ProductDocument existing = productSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Product product = existing.toProduct();
        product.activate();

        ProductDocument updatedDocument = ProductDocument.fromProduct(product);
        ProductDocument savedDocument = productSearchRepository.save(updatedDocument);

        eventPublisher.publishProductActivated(savedDocument.toProduct());

        log.info("Activated product with id: {}", id);
        return ProductDTO.toResponse(savedDocument.toProduct());
    }

    @Transactional
    public ProductDTO.ProductResponse updateStock(String id, int quantity, String reason) {
        ProductDocument existing = productSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Product product = existing.toProduct();
        int previousStock = product.getStockQuantity();
        product.updateStock(quantity);

        ProductDocument updatedDocument = ProductDocument.fromProduct(product);
        ProductDocument savedDocument = productSearchRepository.save(updatedDocument);

        eventPublisher.publishStockUpdated(savedDocument.toProduct(), previousStock, quantity, reason);

        log.info("Updated stock for product {}: {} -> {}", id, previousStock, quantity);
        return ProductDTO.toResponse(savedDocument.toProduct());
    }

    @Transactional
    public void deleteProduct(String id, String reason) {
        if (!productSearchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        ProductDocument document = productSearchRepository.findById(id).get();
        Product product = document.toProduct();

        productSearchRepository.deleteById(id);

        eventPublisher.publishProductDeleted(product, reason);

        log.info("Deleted product with id: {}", id);
    }

    @Transactional(readOnly = true)
    public long countByMerchantId(String merchantId) {
        return productSearchRepository.countByMerchantId(merchantId);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return productSearchRepository.countByStatus(status);
    }
}
