package com.example.OrdersTracking.controllers;


import com.example.OrdersTracking.dtos.ProductDTO;
import com.example.OrdersTracking.enums.MenuCategory;
import com.example.OrdersTracking.models.Product;
import com.example.OrdersTracking.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products") // Всички пътища за админ на продукти
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /admin/products
     * Показва списък с всички продукти, с филтри.
     */
    @GetMapping
    public String listProducts(@RequestParam(required = false) Boolean available,
                               @RequestParam(required = false) MenuCategory category,
                               Model model) {

        List<Product> products = productService.findAll(available, category);
        model.addAttribute("products", products);
        model.addAttribute("categories", MenuCategory.values());

        // --- ТОВА Е ПОПРАВКАТА ---
        // Подайте селектираните стойности обратно на изгледа
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedAvailable", available);
        // ------------------------

        return "admin/products-list";
    }
    /**
     * GET /admin/products/new
     * Показва формата за създаване на нов продукт.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", MenuCategory.values());
        model.addAttribute("formTitle", "Създаване на нов продукт");
        return "admin/product-form"; // Трябва да създадете "admin/product-form.html"
    }

    /**
     * POST /admin/products/save
     * Обработва създаването на нов продукт.
     */
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute ProductDTO productDTO, RedirectAttributes redirectAttributes) {
        try {
            productService.save(productDTO);
            redirectAttributes.addFlashAttribute("success", "Продуктът е създаден успешно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при запазване: " + e.getMessage());
        }
        return "redirect:/admin/products"; // Пренасочи към списъка
    }

    /**
     * GET /admin/products/edit/{id}
     * Показва формата за редакция на съществуващ продукт.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ProductDTO productDTO = productService.findById(id);
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", MenuCategory.values());
            model.addAttribute("formTitle", "Редакция на продукт");
            return "admin/product-edit"; // Използваме същата форма като за създаване
        } catch (Exception e) { // Напр. ProductNotFoundException
            return "redirect:/admin/products";
        }
    }

    /**
     * POST /admin/products/update/{id}
     * Обработва актуализацията на продукт.
     */
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute ProductDTO productDTO,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.update(id, productDTO);
            redirectAttributes.addFlashAttribute("success", "Продуктът е обновен успешно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при обновяване: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * POST /admin/products/delete/{id}
     * Изтрива продукт. (Използваме POST за безопасност, а не GET)
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Продуктът е изтрит успешно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при изтриване: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    /**
     * POST /admin/products/toggle/{id}
     * Променя наличността на продукт.
     */
    @PostMapping("/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleAvailability(id);
            redirectAttributes.addFlashAttribute("success", "Наличността е променена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

}
