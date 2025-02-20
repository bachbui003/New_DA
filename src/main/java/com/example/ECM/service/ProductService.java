package com.example.ECM.service;

import com.example.ECM.dto.ProductDTO;
import com.example.ECM.model.Category;
import com.example.ECM.model.Product;
import com.example.ECM.repository.CategoryRepository;
import com.example.ECM.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // ✅ Inject CategoryRepository

    // 🔹 Tìm kiếm sản phẩm với bộ lọc & phân trang
    public Page<ProductDTO> searchProducts(String name, Long categoryId, Double minPrice, Double maxPrice, Double minRating, int page, int size, String sort) {
        try {
            Sort sorting = switch (sort) {
                case "price_asc" -> Sort.by("price").ascending();
                case "price_desc" -> Sort.by("price").descending();
                case "rating_desc" -> Sort.by("rating").descending();
                default -> Sort.unsorted();
            };

            Pageable pageable = PageRequest.of(page, size, sorting);
            return productRepository.searchProducts(name, categoryId, minPrice, maxPrice, minRating, pageable)
                    .map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), e);
        }
    }

    // 🔹 Lấy sản phẩm theo ID
    public Optional<ProductDTO> getProductById(Long id) {
        try {
            return productRepository.findById(id).map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy sản phẩm theo ID: " + e.getMessage(), e);
        }
    }

    // 🔹 Tạo sản phẩm mới
    public ProductDTO createProduct(ProductDTO productDTO) {
        try {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

            Product product = new Product();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setStockQuantity(productDTO.getStockQuantity());
            product.setImageUrl(productDTO.getImageUrl());
            product.setRating(productDTO.getRating());
            product.setCategory(category);  // Gán Category cho Product

            return convertToDTO(productRepository.save(product));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo sản phẩm: " + e.getMessage(), e);
        }
    }

    // 🔹 Cập nhật sản phẩm
    public Optional<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {
        try {
            return productRepository.findById(id).map(existingProduct -> {
                existingProduct.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
                existingProduct.setName(productDTO.getName());
                existingProduct.setDescription(productDTO.getDescription());
                existingProduct.setPrice(productDTO.getPrice());
                existingProduct.setImageUrl(productDTO.getImageUrl());
                existingProduct.setRating(productDTO.getRating());

                // ✅ Cập nhật category nếu có
                if (productDTO.getCategoryId() != null) {
                    Category category = categoryRepository.findById(productDTO.getCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found"));
                    existingProduct.setCategory(category);
                }

                existingProduct = productRepository.save(existingProduct);
                return convertToDTO(existingProduct);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    // 🔹 Cập nhật số lượng tồn kho
    public Optional<ProductDTO> updateStock(Long id, int quantity) {
        try {
            return productRepository.findById(id).map(existingProduct -> {
                existingProduct.setStockQuantity(quantity);
                productRepository.save(existingProduct);
                return convertToDTO(existingProduct);
            });
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật số lượng tồn kho: " + e.getMessage(), e);
        }
    }

    // 🔹 Xóa sản phẩm
    public void deleteProduct(Long id) {
        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    // 🔹 Lấy tất cả sản phẩm
    public List<ProductDTO> getAllProducts() {
        try {
            return productRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy tất cả sản phẩm: " + e.getMessage(), e);
        }
    }

    // Convert Product to ProductDTO
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .categoryId(product.getCategory().getId()) // Lấy ID của Category
                .build();
    }

    private Product convertToEntity(ProductDTO productDTO) {
        // ✅ Lấy category từ database
        try {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            return Product.builder()
                    .id(productDTO.getId())
                    .name(productDTO.getName())
                    .description(productDTO.getDescription())
                    .price(productDTO.getPrice())
                    .stockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0)
                    .imageUrl(productDTO.getImageUrl())
                    .rating(productDTO.getRating())
                    .category(category)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển đổi từ DTO sang Entity: " + e.getMessage(), e);
        }
    }
}
