package com.ai.template.controller;

import com.ai.template.annotation.AuthCheck;
import com.ai.template.common.BaseResponse;
import com.ai.template.common.ResultUtils;
import com.ai.template.constant.UserConstant;
import com.ai.template.exception.BusinessException;
import com.ai.template.exception.ErrorCode;
import com.ai.template.model.entity.PaymentRecord;
import com.ai.template.model.entity.User;
import com.ai.template.service.PaymentService;
import com.ai.template.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/payment")
@Slf4j
@Tag(name = "PaymentController", description = "Payment API")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private UserService userService;

    
    @PostMapping("/create-vip-session")
    @Operation(summary = "Create VIP payment session")
    public BaseResponse<String> createVipPaymentSession(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        try {
            String sessionUrl = paymentService.createVipPaymentSession(loginUser.getId());
            return ResultUtils.success(sessionUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Create payment session failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Create payment session failed");
        }
    }

    
    @PostMapping("/refund")
    @Operation(summary = "Request refund")
    @AuthCheck(mustRole = UserConstant.VIP_ROLE)
    public BaseResponse<Boolean> refund(
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        try {
            boolean success = paymentService.handleRefund(loginUser.getId(), reason);
            return ResultUtils.success(success);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refund failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Refund failed");
        }
    }

    
    @GetMapping("/records")
    @Operation(summary = "Get current user payment records")
    public BaseResponse<List<PaymentRecord>> getPaymentRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<PaymentRecord> records = paymentService.getPaymentRecords(loginUser.getId());
        return ResultUtils.success(records);
    }
}