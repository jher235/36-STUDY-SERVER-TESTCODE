package sopt.study.testcode.jaeheon.spring.api.service.order;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sopt.study.testcode.jaeheon.spring.api.controller.order.request.OrderCreateRequest;
import sopt.study.testcode.jaeheon.spring.api.controller.order.response.OrderResponse;
import sopt.study.testcode.jaeheon.spring.domain.OrderProduct.OrderProductRepository;
import sopt.study.testcode.jaeheon.spring.domain.order.OrderRepository;
import sopt.study.testcode.jaeheon.spring.domain.product.Product;
import sopt.study.testcode.jaeheon.spring.domain.product.ProductRepository;
import sopt.study.testcode.jaeheon.spring.domain.product.ProductType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static sopt.study.testcode.jaeheon.spring.domain.product.ProductType.HANDMADE;
import static sopt.study.testcode.jaeheon.spring.domain.product.SellingStatus.*;

@ActiveProfiles("test")
@SpringBootTest
//@DataJpaTest
class OrderServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private OrderService orderService;

    /**
     * 외래 키가 존재한다면 삭제 순서를 고려할 것
     */
    @AfterEach
    void clearInAfterTest(){
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder(){
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);

        productRepository.saveAll(List.of(product1, product2, product3));


        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "002"))
                .build();
        // when
        OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);



        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 4000);
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", 1000),
                        Tuple.tuple("002", 3000)
                );
    }

    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있음")
    @Test
    void createOrderWithDuplicatedProductNumbers(){
        // given
        LocalDateTime registeredDateTime = LocalDateTime.now();

        Product product1 = createProduct(HANDMADE, "001", 1000);
        Product product2 = createProduct(HANDMADE, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);

        productRepository.saveAll(List.of(product1, product2, product3));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001"))
                .build();

        // when
        OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);



        // then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime", "totalPrice")
                .contains(registeredDateTime, 2000);
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", 1000),
                        Tuple.tuple("001", 1000)
                );
    }

    private Product createProduct(ProductType type, String productNumber, int price){
        return Product.builder()
                .type(type)
                .productNumber(productNumber)
                .price(price)
                .name("메뉴명")
                .build();
    }

}