package com.example.ECM.controller;

import com.example.ECM.dto.ProductDTO;
import com.example.ECM.service.ProductService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    // 🔹 Tìm kiếm sản phẩm với bộ lọc & phân trang
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating, // ✅ Lọc theo đánh giá
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        try {
            Page<ProductDTO> products = productService.searchProducts(name, categoryId, minPrice, maxPrice, minRating, page, size, sort);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm sản phẩm: {}", e.getMessage(), e); // Ghi lại lỗi cụ thể cho hành động tìm kiếm
            // Trả về trang trống nếu có lỗi và thêm thông báo lỗi
            Page<ProductDTO> emptyPage = Page.empty();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emptyPage);
        }
    }

    // 🔹 Lấy tất cả danh sách sản phẩm
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sản phẩm: {}", e.getMessage(), e); // Ghi lại lỗi cho hành động lấy danh sách
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🔹 Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            Optional<ProductDTO> productDTO = productService.getProductById(id);
            return productDTO.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            logger.error("Lỗi khi lấy sản phẩm với ID {}: {}", id, e.getMessage(), e); // Ghi lại lỗi cho hành động lấy sản phẩm theo ID
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🔹 Thêm sản phẩm mới (chỉ admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            return ResponseEntity.ok(productService.createProduct(productDTO));
        } catch (Exception e) {
            logger.error("Lỗi khi thêm sản phẩm: {}", e.getMessage(), e); // Ghi lại lỗi cho hành động thêm sản phẩm
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🔹 Cập nhật sản phẩm (chỉ admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            Optional<ProductDTO> updatedProduct = productService.updateProduct(id, productDTO);
            return updatedProduct.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật sản phẩm với ID {}: {}", id, e.getMessage(), e); // Ghi lại lỗi cho hành động cập nhật sản phẩm
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🔹 Xóa sản phẩm (chỉ admin)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Lỗi khi xóa sản phẩm với ID {}: {}", id, e.getMessage(), e); // Ghi lại lỗi cho hành động xóa sản phẩm
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 🔹 Cập nhật số lượng tồn kho (chỉ admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(@PathVariable Long id, @RequestParam @Min(1) int quantity) {
        try {
            Optional<ProductDTO> updatedProduct = productService.updateStock(id, quantity);
            return updatedProduct.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(null));
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật số lượng tồn kho cho sản phẩm với ID {}: {}", id, e.getMessage(), e); // Ghi lại lỗi cho hành động cập nhật tồn kho
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
