package co.ravn.ecommerce.Entities;

import co.ravn.ecommerce.DTO.EmailType;
import co.ravn.ecommerce.Entities.Auth.SysUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_log")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SysUser user;

    @OneToOne
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private SysUser createdBy;

    @Column(name = "recipient_email")
    private String recipientEmail;
    private String cc;
    private String bcc;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String status;

    @Column(name = "email_type")
    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    public Email(String recipientEmail, String cc, String bcc, String subject, String body, String status, EmailType emailType) {
        this.recipientEmail = recipientEmail;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.emailType = emailType;
    }
}
