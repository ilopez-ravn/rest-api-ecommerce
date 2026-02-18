package co.ravn.ecommerce.DTO;


import co.ravn.ecommerce.Entities.Auth.SysUser;

import java.util.Date;

public record PasswordResetEvent(
        SysUser user,
        String email,
        String name,
        String token,
        Date expiryDate) {
}
