package org.keycloak.email.custom;

import org.jboss.logging.Logger;
import org.keycloak.email.EmailException;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.theme.FreeMarkerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomEmailTemplateProvider extends FreeMarkerEmailTemplateProvider {

    private static final Logger logger = Logger.getLogger(CustomEmailTemplateProvider.class);


    public CustomEmailTemplateProvider(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
        logger.debug("I AM CustomEmailTemplateProvider!!!!!");
    }

    @Override
    public void sendExecuteActions(String link, long expirationInMinutes) throws EmailException {

        logger.error("I AM CustomEmailTemplateProvider sendExecuteActions!!!!!");
        logger.info("attr=" + attributes);


        if (isAction("UPDATE_PASSWORD")) {
            sendPasswordReset(link, expirationInMinutes);
        }
        else if (isAction("VERIFY_EMAIL")) {
            sendVerifyEmail(link, expirationInMinutes);
        }
        else {
            Map<String, Object> attributes = new HashMap<>(this.attributes);
            attributes.put("user", new ProfileBean(user));
            addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);
            attributes.put("realmName", getRealmName());
            send("executeActionsSubject", "executeActions.ftl", attributes);
        }
        //attr={requiredActions=[UPDATE_PASSWORD]}
        //attr={requiredActions=[VERIFY_EMAIL]}
    }

    private boolean isAction(String action) {
        List<String> actions = (List<String>) attributes.get("requiredActions");
        return actions != null && actions.contains(action);
    }

    @Override
    public void sendPasswordReset(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        send("passwordResetSubject", "password-reset.ftl", attributes);
    }


    @Override
    public void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        send("emailVerificationSubject", "email-verification.ftl", attributes);
    }


    @Override
    public void sendSmtpTestEmail(Map<String, String> config, UserModel user) throws EmailException {
        logger.error("I AM CustomEmailTemplateProvider sendExecuteActions sendSmtpTestEmail!!!!!");

        setRealm(session.getContext().getRealm());
        setUser(user);

        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        attributes.put("realmName", realm.getName());

        EmailTemplate email = processTemplate("emailTestSubject", Collections.emptyList(), "email-test.ftl", attributes);
        send(config, email.getSubject()+" customTemplateProvider", email.getTextBody(), email.getHtmlBody());
    }

}
