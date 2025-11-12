package com.merchant.nexus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch repository for Product search - demonstrates advanced search capabilities
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByMerchantId(String merchantId, Pageable pageable);

    Page<ProductDocument> findByStatus(String status, Pageable pageable);

    Page<ProductDocument> findByCategory(String category, Pageable pageable);

    Page<ProductDocument> findByMerchantIdAndStatus(String merchantId, String status, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 2.0}}}], \"filter\": [{\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ProductDocument> searchByName(String name, Pageable pageable);

    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 2.0}}}, {\"match\": {\"description\": \"?0\"}}, {\"term\": {\"tags\": \"?0\"}}], \"minimum_should_match\": 1}}")
    Page<ProductDocument> searchProducts(String query, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"range\": {\"price.amount\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": [{\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ProductDocument> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"terms\": {\"category\": ?0}}], \"filter\": [{\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ProductDocument> findByCategories(List<String> categories, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 2.0}}}, {\"match\": {\"description\": \"?0\"}}], \"filter\": [{\"range\": {\"price.amount\": {\"gte\": ?1, \"lte\": ?2}}}, {\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ProductDocument> searchByKeywordAndPriceRange(String keyword, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    long countByMerchantId(String merchantId);

    long countByStatus(String status);

    void deleteByMerchantId(String merchantId);
}
