package com.example.ODC_Academy.notification;

import com.example.ODC_Academy.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Notifications e-mail, désactivées par défaut. Activation : APP_MAIL_ENABLED=true + SPRING_MAIL_HOST / _PORT /
 * _USERNAME / _PASSWORD. Le contenu des messages n'est jamais envoyé par e-mail, seulement une alerte.
 */
@Component
public class Notifier {
    private static final Logger log = LoggerFactory.getLogger(Notifier.class);
    private final ObjectProvider<JavaMailSender> sender;
    private final boolean enabled;
    private final String from;
    private final String publicUrl;
    private final boolean logLinks;

    public Notifier(ObjectProvider<JavaMailSender> sender,
                    @Value("${app.mail.enabled:false}") boolean enabled,
                    @Value("${app.mail.from:no-reply@odc-academy.local}") String from,
                    @Value("${app.public-url:http://localhost:4200}") String publicUrl,
                    @Value("${app.mail.log-links:false}") boolean logLinks) {
        this.logLinks = logLinks;
        this.sender = sender; this.enabled = enabled; this.from = from; this.publicUrl = publicUrl;
    }

    /** @Async : appelée depuis un autre bean (via le proxy Spring), donc exécutée hors de la requête HTTP. */
    @Async
    public void newMessage(User to, String fromName) {
        send(to, "Nouveau message sur ODC Academy",
                fromName + " vous a envoyé un message. Lisez-le ici : " + publicUrl + "/messages");
    }

    @Async
    public void forumReply(User to, String fromName, String threadTitle) {
        send(to, "Nouvelle réponse dans le forum",
                fromName + " a répondu à votre discussion « " + threadTitle + " ». Consultez-la sur " + publicUrl);
    }

    @Async
    public void passwordReset(User to, String link) {
        if (!enabled && logLinks) log.info("[DEV] Lien de réinitialisation pour {} : {}", to.getEmail(), link); // jamais en production
        send(to, "Réinitialisation de votre mot de passe",
                "Vous avez demandé à réinitialiser votre mot de passe. Ce lien est valable 30 minutes et ne fonctionne qu'une fois :\n"
                        + link + "\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez ce message : votre mot de passe reste inchangé.");
    }

    @Async
    public void passwordChanged(User to) {
        send(to, "Votre mot de passe a été modifié",
                "Le mot de passe de votre compte vient d'être modifié. Si ce n'est pas vous, contactez l'administrateur immédiatement.");
    }

    private void send(User to, String subject, String text) {
        JavaMailSender s = sender.getIfAvailable();
        if (!enabled || s == null) return;
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(to.getEmail());
            m.setSubject(subject);
            m.setText("Bonjour " + to.getPrenom() + ",\n\n" + text + "\n\nOrange Digital Center — ODC Academy");
            s.send(m);
        } catch (Exception ex) {
            log.warn("Envoi e-mail impossible : {}", ex.getMessage());
        }
    }
}
