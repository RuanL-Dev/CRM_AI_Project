package com.synkra.crm.service;

import com.synkra.crm.model.Contact;
import com.synkra.crm.model.EmailCampaign;
import com.synkra.crm.model.EmailProvider;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class EmailDeliveryService {

    public void sendTest(EmailProvider provider, String recipientEmail) {
        JavaMailSenderImpl sender = buildSender(provider);
        MimeMessage message = sender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(recipientEmail);
            helper.setFrom(provider.getFromEmail(), fallbackSenderName(provider));
            if (provider.getReplyTo() != null && !provider.getReplyTo().isBlank()) {
                helper.setReplyTo(provider.getReplyTo());
            }
            helper.setSubject("Teste de integracao SMTP");
            helper.setText("""
                <div style="font-family:Arial,sans-serif;background:#07111f;padding:32px;color:#e2f5ff">
                  <h1 style="margin:0 0 12px;color:#67e8f9">Integracao validada</h1>
                  <p style="margin:0 0 12px">O provedor <strong>%s</strong> respondeu corretamente.</p>
                  <p style="margin:0;color:#94a3b8">Agora ele pode ser usado para campanhas na plataforma.</p>
                </div>
                """.formatted(provider.getName()), true);
            sender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao enviar email de teste: " + exception.getMessage(), exception);
        }
    }

    public void sendCampaign(EmailProvider provider, EmailCampaign campaign, Contact contact) {
        JavaMailSenderImpl sender = buildSender(provider);
        MimeMessage message = sender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(contact.getEmail());
            helper.setFrom(provider.getFromEmail(), campaign.getSenderName() == null || campaign.getSenderName().isBlank()
                ? fallbackSenderName(provider)
                : campaign.getSenderName());
            if (provider.getReplyTo() != null && !provider.getReplyTo().isBlank()) {
                helper.setReplyTo(provider.getReplyTo());
            }
            helper.setSubject(campaign.getSubject());
            helper.setText(campaign.getPlainTextContent(), campaign.getHtmlContent());
            sender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao enviar campanha para " + contact.getEmail() + ": " + exception.getMessage(), exception);
        }
    }

    private JavaMailSenderImpl buildSender(EmailProvider provider) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(provider.getHost());
        sender.setPort(provider.getPort());
        sender.setUsername(provider.getUsername());
        sender.setPassword(provider.getPassword());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(provider.isTlsEnabled()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");

        return sender;
    }

    private String fallbackSenderName(EmailProvider provider) {
        return provider.getFromName() == null || provider.getFromName().isBlank()
            ? provider.getName()
            : provider.getFromName();
    }
}
