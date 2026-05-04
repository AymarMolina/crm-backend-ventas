package com.crmventas.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void enviarResetPassword(String emailDestino, String nombreUsuario, String rawToken) {
        String link = frontendUrl + "/reset-password?token=" + rawToken;

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: 'Segoe UI', sans-serif; background:#f4f4f5; padding: 40px 0; margin:0;">
              <div style="max-width:520px; margin:0 auto; background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,.08);">
                
                <!-- Header -->
                <div style="background:#0d0f14; padding:32px 40px;">
                  <p style="margin:0; color:#fff; font-size:20px; font-weight:600; letter-spacing:.3px;">
                    CRM <span style="color:rgb(99,200,150)">Ventas</span>
                  </p>
                </div>

                <!-- Body -->
                <div style="padding:36px 40px;">
                  <h2 style="margin:0 0 8px; font-size:22px; color:#0d0f14;">Recupera tu contraseña</h2>
                  <p style="margin:0 0 24px; color:#666; font-size:14px; line-height:1.6;">
                    Hola <strong>%s</strong>, recibimos una solicitud para restablecer la contraseña de tu cuenta.
                  </p>

                  <a href="%s"
                     style="display:inline-block; background:#0d0f14; color:#fff; text-decoration:none;
                            padding:14px 32px; border-radius:8px; font-size:14px; font-weight:500;">
                    Restablecer contraseña
                  </a>

                  <p style="margin:24px 0 0; color:#999; font-size:12px; line-height:1.6;">
                    Este enlace expira en <strong>1 hora</strong>.<br>
                    Si no solicitaste este cambio, puedes ignorar este correo.
                  </p>
                </div>

                <!-- Footer -->
                <div style="padding:20px 40px; border-top:1px solid #f0f0f0;">
                  <p style="margin:0; color:#bbb; font-size:11px;">
                    No respondas a este correo · CRM Ventas © 2026
                  </p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(nombreUsuario, link);

        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(emailDestino)
                .subject("Restablecer contraseña — CRM Ventas")
                .html(html)
                .build();

            var response = resend.emails().send(params);
            log.info("Email enviado a {} — id: {}", emailDestino, response.getId());

        } catch (ResendException e) {
            log.error("Error enviando email a {}: {}", emailDestino, e.getMessage());
            // No lanzamos excepción para no revelar si el email existe
        }
    }
}