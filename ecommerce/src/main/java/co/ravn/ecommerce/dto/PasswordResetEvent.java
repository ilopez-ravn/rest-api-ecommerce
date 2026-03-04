package co.ravn.ecommerce.dto;


import co.ravn.ecommerce.entities.auth.SysUser;

import java.util.Date;

public record PasswordResetEvent(
        SysUser user,
        String email,
        String name,
        String token,
        Date expiryDate) {
}
