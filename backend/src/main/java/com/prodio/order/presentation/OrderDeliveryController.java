package com.prodio.order.presentation;

import com.prodio.order.application.DeliveryCommand;
import com.prodio.order.application.DeliveryContext;
import com.prodio.order.application.OrderDeliveryService;
import com.prodio.order.domain.DeliverySnapshot;
import com.prodio.order.exception.OrderErrorCode;
import com.prodio.order.exception.OrderException;
import com.prodio.shared.ApiResponse;
import com.prodio.user.UserDirectory;
import com.prodio.user.UserRef;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/delivery")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
@RequiredArgsConstructor
class OrderDeliveryController {
    private final OrderDeliveryService deliveryService;
    private final UserDirectory userDirectory;

    @GetMapping
    ApiResponse<DeliveryContextResponse> context(@RequestParam String clientId,
            Authentication authentication) {
        UserRef user = currentUser(authentication);
        DeliveryContext context = deliveryService.context(parseId(clientId), user.id(),
                isPrivileged(authentication));
        return ApiResponse.success(DeliveryContextResponse.from(context));
    }

    @PostMapping("/addresses")
    ApiResponse<DeliveryResponse> create(@Valid @RequestBody CreateDeliveryRequest request,
            Authentication authentication) {
        UserRef user = currentUser(authentication);
        DeliverySnapshot saved = deliveryService.create(parseId(request.clientId()),
                request.delivery().toCommand(), user.id(), isPrivileged(authentication));
        return ApiResponse.success("배송지를 등록했습니다.", DeliveryResponse.from(saved));
    }

    @PutMapping("/addresses/{id}")
    ApiResponse<DeliveryResponse> update(@PathVariable String id,
            @Valid @RequestBody DeliveryRequest request, Authentication authentication) {
        UserRef user = currentUser(authentication);
        DeliverySnapshot saved = deliveryService.update(parseId(id), request.toCommand(),
                user.id(), isPrivileged(authentication));
        return ApiResponse.success("배송지를 수정했습니다.", DeliveryResponse.from(saved));
    }

    @DeleteMapping("/addresses/{id}")
    ApiResponse<Void> delete(@PathVariable String id, Authentication authentication) {
        UserRef user = currentUser(authentication);
        deliveryService.delete(parseId(id), user.id(), isPrivileged(authentication));
        return ApiResponse.success("배송지를 삭제했습니다.", null);
    }

    private UserRef currentUser(Authentication authentication) {
        return userDirectory.findActiveByEmail(authentication.getName())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_CREATOR_NOT_FOUND));
    }

    private boolean isPrivileged(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN"));
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

    record CreateDeliveryRequest(@NotBlank String clientId,
            @NotNull @Valid DeliveryRequest delivery) {}

    record DeliveryRequest(@NotBlank @Size(max = 100) String name,
            @Size(max = 100) String recipientName, @Size(max = 50) String recipientPhone,
            @Size(max = 20) String postalCode, @NotBlank @Size(max = 2000) String addressLine1,
            @Size(max = 2000) String addressLine2) {
        DeliveryCommand toCommand() {
            return new DeliveryCommand(null, name, recipientName, recipientPhone,
                    postalCode, addressLine1, addressLine2);
        }
    }

    record DeliveryResponse(String addressId, String name, String recipientName,
            String recipientPhone, String postalCode, String addressLine1, String addressLine2) {
        static DeliveryResponse from(DeliverySnapshot snapshot) {
            if (snapshot == null) return null;
            return new DeliveryResponse(snapshot.addressId() == null ? null
                    : Long.toString(snapshot.addressId()), snapshot.name(), snapshot.recipientName(),
                    snapshot.recipientPhone(), snapshot.postalCode(), snapshot.addressLine1(),
                    snapshot.addressLine2());
        }
    }

    record DeliveryContextResponse(String clientId, String clientName,
            DeliveryResponse headquarters, DeliveryResponse recent,
            List<DeliveryResponse> savedAddresses) {
        static DeliveryContextResponse from(DeliveryContext context) {
            return new DeliveryContextResponse(Long.toString(context.clientId()), context.clientName(),
                    DeliveryResponse.from(context.headquarters()), DeliveryResponse.from(context.recent()),
                    context.savedAddresses().stream().map(DeliveryResponse::from).toList());
        }
    }
}
