package com.merchant.nexus.repository;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch document for Product - demonstrates search indexing, analyzers
 */
@Data
@Builder
@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/settings.json")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String merchantId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Nested)
    private Money price;

    @Field(type = FieldType.Nested)
    private Money costPrice;

    @Field(type = FieldType.Integer)
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Keyword)
    private List<String> imageUrls;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    @Data
    @Builder
    public static class Money {
        @Field(type = FieldType.Double)
        private BigDecimal amount;

        @Field(type = FieldType.Keyword)
        private String currency;
    }

    public static ProductDocument fromProduct(com.coupang.merchant.model.Product product) {
        return ProductDocument.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .tags(product.getTags())
                .price(product.getPrice() != null ? 
                    Money.builder()
                        .amount(product.getPrice().getAmount())
                        .currency(product.getPrice().getCurrency())
                        .build() : null)
                .costPrice(product.getCostPrice() != null ?
                    Money.builder()
                        .amount(product.getCostPrice().getAmount())
                        .currency(product.getCostPrice().getCurrency())
                        .build() : null)
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .imageUrl(product.getImageUrl())
                .imageUrls(product.getImageUrls())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public com.coupang.merchant.model.Product toProduct() {
        com.coupang.merchant.model.Product.Money priceMoney = null;
        if (this.price != null) {
            priceMoney = com.coupang.merchant.model.Product.Money.builder()
                    .amount(this.price.getAmount())
                    .currency(this.price.getCurrency())
                    .build();
        }

        com.coupang.merchant.model.Product.Money costPriceMoney = null;
        if (this.costPrice != null) {
            costPriceMoney = com.coupang.merchant.model.Product.Money.builder()
                    .amount(this.costPrice.getAmount())
                    .currency(this.costPrice.getCurrency())
                    .build();
        }

        return com.coupang.merchant.model.Product.builder()
                .id(this.id)
                .merchantId(this.merchantId)
                .name(this.name)
                .description(this.description)
                .category(this.category)
                .tags(this.tags)
                .price(priceMoney)
                .costPrice(costPriceMoney)
                .stockQuantity(this.stockQuantity)
                .status(this.status != null ? 
                    com.coupang.merchant.model.Product.ProductStatus.valueOf(this.status) : null)
                .imageUrl(this.imageUrl)
                .imageUrls(this.imageUrls)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
