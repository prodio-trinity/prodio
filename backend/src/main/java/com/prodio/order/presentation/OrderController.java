package com.prodio.order.presentation;

import com.prodio.order.application.CreateOrderCommand;
import com.prodio.order.application.OrderPage;
import com.prodio.order.application.OrderService;
import com.prodio.order.application.UpdateOrderCommand;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import com.prodio.shared.ApiResponse;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {
    private final OrderService orderService;
    private final UserDirectory userDirectory;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderPageResponse> list(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        OrderStatus parsedStatus = parseStatus(status);
        return ApiResponse.success(OrderPageResponse.from(orderService.list(parsedStatus, q, page, size)));
    }

    @GetMapping("/mine")
    ApiResponse<OrderPageResponse> listMine(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UserRef creator = currentUser(authentication);
        return ApiResponse.success(OrderPageResponse.from(
                orderService.listMine(creator.id(), parseStatus(status), q, page, size)));
    }

    @GetMapping("/mine/{id}")
    ApiResponse<OrderResponse> getMine(@PathVariable String id, Authentication authentication) {
        UserRef creator = currentUser(authentication);
        return ApiResponse.success(OrderResponse.from(orderService.getMine(parseId(id), creator.id())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderResponse> get(@PathVariable String id) {
        return ApiResponse.success(OrderResponse.from(orderService.get(parseId(id))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        UserRef creator = currentUser(authentication);
        CreateOrderCommand command = new CreateOrderCommand(parseId(request.clientId()),
                parseId(request.productId()), request.quantity(), request.vatIncluded(),
                request.dueDate(), request.deliveryAddress(), request.note(), creator.id());
        return ApiResponse.success("수주를 등록했습니다.", OrderResponse.from(orderService.create(command)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderResponse> update(@PathVariable String id,
            @Valid @RequestBody UpdateOrderRequest request) {
        UpdateOrderCommand command = new UpdateOrderCommand(parseId(request.productId()),
                request.quantity(), request.vatIncluded(), request.dueDate(),
                request.deliveryAddress(), request.note());
        return ApiResponse.success("수주를 수정했습니다.",
                OrderResponse.from(orderService.update(parseId(id), command)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderResponse> confirm(@PathVariable String id) {
        return ApiResponse.success("입금을 확인하고 주문을 확정했습니다.",
                OrderResponse.from(orderService.confirm(parseId(id))));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderResponse> cancel(@PathVariable String id,
            @Valid @RequestBody CancelOrderRequest request) {
        return ApiResponse.success("주문을 취소했습니다.",
                OrderResponse.from(orderService.cancel(parseId(id), request.reason())));
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "식별자가 올바르지 않습니다.");
        }
    }

    private UserRef currentUser(Authentication authentication) {
        return userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_CREATOR_NOT_FOUND));
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OrderStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "알 수 없는 수주 상태입니다.");
        }
    }

    record CreateOrderRequest(@NotBlank String clientId, @NotBlank String productId,
            @Positive int quantity, boolean vatIncluded, @NotNull LocalDate dueDate,
            @Size(max = 2000) String deliveryAddress, @Size(max = 2000) String note) {}

    record UpdateOrderRequest(@NotBlank String productId, @Positive int quantity,
            boolean vatIncluded, @NotNull LocalDate dueDate,
            @Size(max = 2000) String deliveryAddress, @Size(max = 2000) String note) {}

    record CancelOrderRequest(@NotBlank @Size(max = 1000) String reason) {}

    record OrderResponse(String id, String clientId, String clientName, String clientPhone,
            String productId, String productName, long unitPrice, int quantity,
            boolean vatIncluded, long totalAmount, LocalDate dueDate,
            String deliveryAddress, String status, String cancellationReason,
            String createdBy, String note, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        static OrderResponse from(Order order) {
            return new OrderResponse(Long.toString(order.id()), Long.toString(order.clientId()),
                    order.clientNameSnapshot(), order.clientPhoneSnapshot(),
                    Long.toString(order.productId()), order.productNameSnapshot(),
                    order.unitPriceSnapshot(), order.quantity(), order.vatIncluded(),
                    order.totalAmount(), order.dueDate(), order.deliveryAddress(),
                    order.status().name(), order.cancellationReason(), Long.toString(order.createdBy()),
                    order.note(), order.createdAt(), order.updatedAt());
        }
    }

    record OrderPageResponse(List<OrderResponse> orders, int page, int size,
            long totalElements, int totalPages) {
        static OrderPageResponse from(OrderPage page) {
            List<OrderResponse> orders = page.orders().stream().map(OrderResponse::from).toList();
            int totalPages = page.size() == 0 ? 0
                    : (int) Math.ceil((double) page.totalElements() / page.size());
            return new OrderPageResponse(orders, page.page(), page.size(),
                    page.totalElements(), totalPages);
        }
    }
}
