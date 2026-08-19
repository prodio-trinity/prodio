package com.prodio.order.presentation;

import com.prodio.order.application.CreateOrderCommand;
import com.prodio.order.application.OrderPage;
import com.prodio.order.application.OrderItemCommand;
import com.prodio.order.application.DeliveryCommand;
import com.prodio.order.application.OrderClientContext;
import com.prodio.order.application.OrderFormContext;
import com.prodio.order.application.OrderProductContext;
import com.prodio.order.application.OrderService;
import com.prodio.order.application.UpdateOrderCommand;
import com.prodio.order.domain.Order;
import com.prodio.order.domain.OrderStatus;
import com.prodio.order.domain.OrderItem;
import com.prodio.order.domain.DeliverySnapshot;
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

    @GetMapping("/client-context")
    ApiResponse<ClientContextResponse> clientContext(Authentication authentication) {
        UserRef user = currentUser(authentication);
        return ApiResponse.success(ClientContextResponse.from(orderService.clientContext(user.id())));
    }

    @GetMapping("/form-context")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    ApiResponse<FormContextResponse> formContext(@RequestParam(required = false) String clientId,
            Authentication authentication) {
        UserRef user = currentUser(authentication);
        OrderFormContext context = isPrivileged(authentication) && clientId != null
                ? orderService.formContextForClient(parseId(clientId))
                : orderService.formContext(user.id());
        return ApiResponse.success(FormContextResponse.from(context));
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
        long clientId = isPrivileged(authentication) ? parseId(request.clientId())
                : orderService.clientContext(creator.id()).clientId();
        CreateOrderCommand command = new CreateOrderCommand(clientId,
                toItemCommands(request.items()), request.vatIncluded(), request.dueDate(),
                toDeliveryCommand(request.delivery()), request.note(), creator.id());
        return ApiResponse.success("수주를 등록했습니다.", OrderResponse.from(orderService.create(command)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    ApiResponse<OrderResponse> update(@PathVariable String id,
            @Valid @RequestBody UpdateOrderRequest request) {
        UpdateOrderCommand command = toUpdateCommand(request);
        return ApiResponse.success("수주를 수정했습니다.",
                OrderResponse.from(orderService.update(parseId(id), command)));
    }

    @PutMapping("/mine/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    ApiResponse<OrderResponse> updateMine(@PathVariable String id,
            @Valid @RequestBody UpdateOrderRequest request, Authentication authentication) {
        UserRef creator = currentUser(authentication);
        return ApiResponse.success("주문을 수정했습니다.", OrderResponse.from(
                orderService.updateMine(parseId(id), creator.id(), toUpdateCommand(request))));
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

    private boolean isPrivileged(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_STAFF")
                        || authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private OrderStatus parseStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OrderStatus.from(value);
        } catch (IllegalArgumentException exception) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_REQUEST, "알 수 없는 수주 상태입니다.");
        }
    }

    private List<OrderItemCommand> toItemCommands(List<OrderItemRequest> items) {
        return items.stream().map(item -> new OrderItemCommand(
                parseId(item.productId()), item.quantity())).toList();
    }

    private UpdateOrderCommand toUpdateCommand(UpdateOrderRequest request) {
        return new UpdateOrderCommand(toItemCommands(request.items()), request.vatIncluded(),
                request.dueDate(), toDeliveryCommand(request.delivery()), request.note());
    }

    private DeliveryCommand toDeliveryCommand(DeliveryRequest delivery) {
        Long addressId = delivery.addressId() == null || delivery.addressId().isBlank()
                ? null : parseId(delivery.addressId());
        return new DeliveryCommand(addressId, delivery.name(), delivery.recipientName(),
                delivery.recipientPhone(), delivery.postalCode(), delivery.addressLine1(),
                delivery.addressLine2());
    }

    record CreateOrderRequest(@NotBlank String clientId,
            @NotEmpty List<@Valid OrderItemRequest> items,
            boolean vatIncluded, @NotNull LocalDate dueDate,
            @NotNull @Valid DeliveryRequest delivery, @Size(max = 2000) String note) {}

    record UpdateOrderRequest(@NotEmpty List<@Valid OrderItemRequest> items,
            boolean vatIncluded, @NotNull LocalDate dueDate,
            @NotNull @Valid DeliveryRequest delivery, @Size(max = 2000) String note) {}

    record OrderItemRequest(@NotBlank String productId, @Positive int quantity) {}

    record DeliveryRequest(String addressId, @NotBlank @Size(max = 100) String name,
            @Size(max = 100) String recipientName, @Size(max = 50) String recipientPhone,
            @Size(max = 20) String postalCode, @NotBlank @Size(max = 2000) String addressLine1,
            @Size(max = 2000) String addressLine2) {}

    record CancelOrderRequest(@NotBlank @Size(max = 1000) String reason) {}

    record OrderResponse(String id, String clientId, String clientName, String clientPhone,
            List<OrderItemResponse> items, boolean vatIncluded, long totalAmount, LocalDate dueDate,
            DeliveryResponse delivery, String status, String cancellationReason,
            String createdBy, String note, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        static OrderResponse from(Order order) {
            return new OrderResponse(Long.toString(order.id()), Long.toString(order.clientId()),
                    order.clientNameSnapshot(), order.clientPhoneSnapshot(),
                    order.items().stream().map(OrderItemResponse::from).toList(), order.vatIncluded(),
                    order.totalAmount(), order.dueDate(), DeliveryResponse.from(order.delivery()),
                    order.status().name(), order.cancellationReason(), Long.toString(order.createdBy()),
                    order.note(), order.createdAt(), order.updatedAt());
        }
    }

    record OrderItemResponse(String productId, String productName, long unitPrice,
            int quantity, long lineAmount) {
        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(Long.toString(item.productId()), item.productNameSnapshot(),
                    item.unitPriceSnapshot(), item.quantity(), item.lineAmount());
        }
    }

    record DeliveryResponse(String addressId, String name, String recipientName,
            String recipientPhone, String postalCode, String addressLine1, String addressLine2) {
        static DeliveryResponse from(DeliverySnapshot delivery) {
            return new DeliveryResponse(delivery.addressId() == null ? null
                    : Long.toString(delivery.addressId()), delivery.name(), delivery.recipientName(),
                    delivery.recipientPhone(), delivery.postalCode(), delivery.addressLine1(),
                    delivery.addressLine2());
        }
    }

    record ClientContextResponse(String clientId, String clientCode, String companyName,
            String representative, String businessRegistrationNumber, String defaultAddress,
            String phone, String managerName, String memo) {
        static ClientContextResponse from(OrderClientContext context) {
            return new ClientContextResponse(Long.toString(context.clientId()), context.clientCode(),
                    context.companyName(), context.representative(), context.businessRegistrationNumber(),
                    context.defaultAddress(), context.phone(), context.managerName(), context.memo());
        }
    }

    record ProductContextResponse(String productId, String productCode, String name,
            String subCategoryId, String unit, String description, String memo, long unitPrice) {
        static ProductContextResponse from(OrderProductContext context) {
            return new ProductContextResponse(Long.toString(context.productId()), context.productCode(),
                    context.name(), Long.toString(context.subCategoryId()), context.unit(),
                    context.description(), context.memo(), context.unitPrice());
        }
    }

    record FormContextResponse(ClientContextResponse client, List<ProductContextResponse> products) {
        static FormContextResponse from(OrderFormContext context) {
            return new FormContextResponse(ClientContextResponse.from(context.client()),
                    context.products().stream().map(ProductContextResponse::from).toList());
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
