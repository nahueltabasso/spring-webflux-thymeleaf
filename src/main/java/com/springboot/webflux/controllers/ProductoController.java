package com.springboot.webflux.controllers;

import java.time.Duration;
import java.util.Date;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.springboot.webflux.models.documents.Categoria;
import com.springboot.webflux.models.documents.Producto;
import com.springboot.webflux.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductoController {

	private static Logger logger = LoggerFactory.getLogger(ProductoController.class);
	@Autowired
	private ProductoService productoService;

	/**
	 * Este metodo retorna un flux de Categoria y se lo envia a la vista Thymeleaf
	 * 
	 * @param model
	 * @return
	 */
	@ModelAttribute("categorias")
	public Flux<Categoria> categorias() {
		return productoService.findAllCategorias();
	}

	@GetMapping({ "/listar", "/" })
	public String listar(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreUpperCase();

		productos.subscribe(producto -> logger.info(producto.getNombre()));
		model.addAttribute("titulo", "Listado de productos");
		model.addAttribute("productos", productos);
		return "listar";
	}

	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));

		productos.subscribe(producto -> logger.info(producto.getNombre()));
		model.addAttribute("titulo", "Listado de productos");
		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2));
		return "listar";
	}

	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreUpperCase();

		model.addAttribute("titulo", "Listado de productos");
		model.addAttribute("productos", productos);
		return "listar";
	}

	// Otra forma
	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("titulo", "Formulario Producto");
		model.addAttribute("producto", new Producto());
		return Mono.just("form");
	}

	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> producto = productoService.findById(id).doOnNext(p -> {
			logger.info("Producto: " + p.getNombre());
		}).defaultIfEmpty(new Producto());
		model.addAttribute("titulo", "Editar Producto");
		model.addAttribute("producto", producto);
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> save(@Valid Producto producto, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("titulo", "Errores en el formulario");
			return Mono.just("form");
		}
		Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());
		producto.setCreateAt(producto.getCreateAt() != null ? producto.getCreateAt() : new Date());

		return categoria.flatMap(c -> {
			producto.setCategoria(c);
			return productoService.save(producto);
		}).doOnNext(p -> {
			logger.info("Producto guardado: " + p.getNombre());
		}).thenReturn("redirect:/listar");
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return productoService.findById(id).flatMap(p -> {
			return productoService.delete(p);
		}).then(Mono.just("redirect:/listar"));
	}
}
