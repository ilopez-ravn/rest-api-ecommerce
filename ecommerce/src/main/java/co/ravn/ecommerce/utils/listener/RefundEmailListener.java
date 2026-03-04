package co.ravn.ecommerce.utils.listener;

import co.ravn.ecommerce.dto.EmailType;
import co.ravn.ecommerce.dto.RefundApprovedEvent;
import co.ravn.ecommerce.dto.RefundDeniedEvent;
import co.ravn.ecommerce.dto.RefundProcessedEvent;
import co.ravn.ecommerce.dto.RefundRequestedEvent;
import co.ravn.ecommerce.dto.ReturnInTransitEvent;
import co.ravn.ecommerce.entities.Email;
import co.ravn.ecommerce.entities.order.RefundRequest;
import co.ravn.ecommerce.entities.order.ReturnShipment;
import co.ravn.ecommerce.repositories.EmailRepository;
import co.ravn.ecommerce.services.MailService;
import co.ravn.ecommerce.utils.enums.EmailStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RefundEmailListener {

    private final ResourceLoader resourceLoader;
    private final MailService mailService;
    private final EmailRepository emailRepository;

    @Async
    @EventListener
    public void onRefundRequested(RefundRequestedEvent event) {
        RefundRequest refundRequest = event.refundRequest();
        String clientEmail = refundRequest.getRequestedBy().getPerson().getEmail();
        String clientName = refundRequest.getRequestedBy().getPerson().getFullName();
        String orderId = String.valueOf(refundRequest.getOrder().getId());

        Email email = new Email(clientEmail, "", "", "Refund Request Received", "",
                EmailStatusEnum.SENT, EmailType.REFUND_REQUESTED);
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/refund-requested-email.html");
            String html = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", clientName)
                    .replace("{{order_id}}", orderId)
                    .replace("{{reason}}", refundRequest.getReason());
            email.setBody(html);
            mailService.sendHtml(clientEmail, "Refund Request Received", html);
            emailRepository.save(email);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send refund requested email for order id={}: {}", orderId, e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void onRefundApproved(RefundApprovedEvent event) {
        RefundRequest refundRequest = event.refundRequest();
        String clientEmail = refundRequest.getRequestedBy().getPerson().getEmail();
        String clientName = refundRequest.getRequestedBy().getPerson().getFullName();
        String orderId = String.valueOf(refundRequest.getOrder().getId());

        String templatePath;
        String subject;
        if (!refundRequest.isRequiresReturn()) {
            templatePath = "classpath:templates/refund-approved-pre-shipment-email.html";
            subject = "Your Refund Has Been Approved";
        } else {
            templatePath = "classpath:templates/refund-approved-return-email.html";
            subject = "Refund Approved - Please Return the Product";
        }

        Email email = new Email(clientEmail, "", "", subject, "",
                EmailStatusEnum.SENT, EmailType.REFUND_APPROVED);
        try {
            Resource resource = resourceLoader.getResource(templatePath);
            String html = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", clientName)
                    .replace("{{order_id}}", orderId)
                    .replace("{{refund_amount}}", refundRequest.getRefundAmount() != null
                            ? refundRequest.getRefundAmount().toPlainString() : "");
            email.setBody(html);
            mailService.sendHtml(clientEmail, subject, html);
            emailRepository.save(email);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send refund approved email for order id={}: {}", orderId, e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void onRefundDenied(RefundDeniedEvent event) {
        RefundRequest refundRequest = event.refundRequest();
        String clientEmail = refundRequest.getRequestedBy().getPerson().getEmail();
        String clientName = refundRequest.getRequestedBy().getPerson().getFullName();
        String orderId = String.valueOf(refundRequest.getOrder().getId());

        Email email = new Email(clientEmail, "", "", "Refund Request Denied", "",
                EmailStatusEnum.SENT, EmailType.REFUND_DENIED);
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/refund-denied-email.html");
            String html = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", clientName)
                    .replace("{{order_id}}", orderId)
                    .replace("{{manager_notes}}", refundRequest.getManagerNotes() != null
                            ? refundRequest.getManagerNotes() : "");
            email.setBody(html);
            mailService.sendHtml(clientEmail, "Refund Request Denied", html);
            emailRepository.save(email);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send refund denied email for order id={}: {}", orderId, e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void onReturnInTransit(ReturnInTransitEvent event) {
        RefundRequest refundRequest = event.refundRequest();
        String clientEmail = refundRequest.getRequestedBy().getPerson().getEmail();
        String clientName = refundRequest.getRequestedBy().getPerson().getFullName();
        String orderId = String.valueOf(refundRequest.getOrder().getId());

        ReturnShipment returnShipment = refundRequest.getReturnShipment();
        String trackingNumber = returnShipment != null ? returnShipment.getTrackingNumber() : "";
        String carrierName = returnShipment != null ? returnShipment.getCarrierName() : "";

        Email email = new Email(clientEmail, "", "", "Return Shipment Registered", "",
                EmailStatusEnum.SENT, EmailType.RETURN_IN_TRANSIT);
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/return-in-transit-email.html");
            String html = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", clientName)
                    .replace("{{order_id}}", orderId)
                    .replace("{{tracking_number}}", trackingNumber)
                    .replace("{{carrier_name}}", carrierName);
            email.setBody(html);
            mailService.sendHtml(clientEmail, "Return Shipment Registered", html);
            emailRepository.save(email);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send return in transit email for order id={}: {}", orderId, e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void onRefundProcessed(RefundProcessedEvent event) {
        RefundRequest refundRequest = event.refundRequest();
        String clientEmail = refundRequest.getRequestedBy().getPerson().getEmail();
        String clientName = refundRequest.getRequestedBy().getPerson().getFullName();
        String orderId = String.valueOf(refundRequest.getOrder().getId());

        Email email = new Email(clientEmail, "", "", "Your Refund Has Been Processed", "",
                EmailStatusEnum.SENT, EmailType.REFUND_PROCESSED);
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/refund-processed-email.html");
            String html = new String(resource.getInputStream().readAllBytes())
                    .replace("{{name}}", clientName)
                    .replace("{{order_id}}", orderId)
                    .replace("{{refund_amount}}", refundRequest.getRefundAmount() != null
                            ? refundRequest.getRefundAmount().toPlainString() : "");
            email.setBody(html);
            mailService.sendHtml(clientEmail, "Your Refund Has Been Processed", html);
            emailRepository.save(email);
        } catch (Exception e) {
            email.setStatus(EmailStatusEnum.NOT_SENT);
            emailRepository.save(email);
            log.error("Failed to send refund processed email for order id={}: {}", orderId, e.getMessage(), e);
        }
    }
}
