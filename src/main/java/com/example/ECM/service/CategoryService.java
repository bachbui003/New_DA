package com.example.ECM.service;

import com.example.ECM.dto.CategoryDTO;
import com.example.ECM.dto.ProductDTO;  // Thêm import cho ProductDTO
import com.example.ECM.model.Category;
import com.example.ECM.model.Product; // Thêm import cho Product
import com.example.ECM.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        try {
            return categoryRepository.findAll().stream().map(category -> {
                CategoryDTO categoryDTO = convertToDTO(category);
                // Sử dụng convertProductToDTO để chuyển đổi từng sản phẩm trong danh sách
                categoryDTO.setProducts(category.getProducts().stream()
                        .map(this::convertProductToDTO) // Dùng convertProductToDTO ở đây
                        .collect(Collectors.toList()));
                return categoryDTO;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách danh mục: " + e.getMessage(), e);
        }
    }

    public CategoryDTO getCategoryById(Long id) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            // Lấy danh sách sản phẩm từ danh mục
            List<ProductDTO> productDTOs = category.getProducts().stream()
                    .map(this::convertProductToDTO)
                    .collect(Collectors.toList());

            // Cập nhật CategoryDTO với danh sách sản phẩm
            return CategoryDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .products(productDTOs)  // Thêm danh sách sản phẩm
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh mục theo ID: " + e.getMessage(), e);
        }
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        try {
            Category category = new Category();
            category.setName(categoryDTO.getName());
            return convertToDTO(categoryRepository.save(category));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo danh mục: " + e.getMessage(), e);
        }
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO updatedCategory) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            category.setName(updatedCategory.getName());
            return convertToDTO(categoryRepository.save(category));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật danh mục: " + e.getMessage(), e);
        }
    }

    public void deleteCategory(Long id) {
        try {
            if (!categoryRepository.existsById(id)) {
                throw new RuntimeException("Không tìm thấy danh mục");
            }
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa danh mục: " + e.getMessage(), e);
        }
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // Convert Product to ProductDTO
    private ProductDTO convertProductToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .rating(product.getRating())
                .categoryId(product.getCategory().getId()) // Lấy categoryId từ Product
                .build();
    }
}
