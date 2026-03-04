package co.ravn.ecommerce.entities;

import co.ravn.ecommerce.dto.EmailType;
import co.ravn.ecommerce.entities.auth.SysUser;
import co.ravn.ecommerce.utils.enums.EmailStatusEnum;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    private String cc;
    private String bcc;
    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "email_status_enum")
    private EmailStatusEnum status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "email_type", nullable = false, columnDefinition = "email_type_enum")
    private EmailType emailType;

    public Email(String recipientEmail, String cc, String bcc, String subject, String body, EmailStatusEnum status, EmailType emailType) {
        this.recipientEmail = recipientEmail;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.emailType = emailType;
    }
}
