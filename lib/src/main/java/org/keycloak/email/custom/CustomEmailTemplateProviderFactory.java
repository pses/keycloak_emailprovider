package org.keycloak.email.custom;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.EmailTemplateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ProviderManagerDeployer;
import org.keycloak.provider.ProviderManagerRegistry;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.theme.FreeMarkerUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CustomEmailTemplateProviderFactory implements EmailTemplateProviderFactory {


    private static final Logger logger = Logger.getLogger(CustomEmailTemplateProviderFactory.class);

    private FreeMarkerUtil freeMarker;
    Thread watchTh = null;

    public CustomEmailTemplateProviderFactory() {
        logger.warn("I AM CustomEmailTemplateProviderFactory!!!!!");
        watchTh = new Thread(() -> {
            try {
                logger.warn("I AM CustomEmailTemplateProviderFactory!!!!! hackIt");
                hackIt();
            } catch (Exception e) {
                logger.error(e);
                Thread.currentThread().interrupt();
            }
        });
        watchTh.start();
        freeMarker = new FreeMarkerUtil();
    }

    @Override
    public EmailTemplateProvider create(KeycloakSession session) {
        return new CustomEmailTemplateProvider(session, freeMarker);
    }

    @Override
    public void init(Config.Scope config) {
        //init block will be not invoked (hack registration!)
        logger.warn("I AM CustomEmailTemplateProviderFactory!!!!! init");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        freeMarker = null;
    }

    @Override
    public String getId() {
        return "freemarker";
    }

    private void hackIt() {
        ProviderManagerRegistry pmr = ProviderManagerRegistry.SINGLETON;
        logger.warn("I AM CustomEmailTemplateProviderFactory!!!!! hackIt");
        try {

            DefaultKeycloakSessionFactory sessionFactory = null;

            while ((sessionFactory = getFactory(pmr)) == null) {
                TimeUnit.SECONDS.sleep(1);
            }

            Field privateField = DefaultKeycloakSessionFactory.class.getDeclaredField("factoriesMap");
            privateField.setAccessible(true);
            Map<Class<? extends Provider>, Map<String, ProviderFactory>> factoriesMap = (Map<Class<? extends Provider>, Map<String, ProviderFactory>>) privateField.get(sessionFactory);

            factoriesMap.put(EmailTemplateProvider.class,Map.of(getId(),this));
            logger.warn("I AM CustomEmailTemplateProviderFactory!!!!! hackIt DONE!!!");
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private DefaultKeycloakSessionFactory getFactory(ProviderManagerRegistry pmr) throws NoSuchFieldException, IllegalAccessException {
        Field privateField = ProviderManagerRegistry.class.getDeclaredField("deployerRef");

        privateField.setAccessible(true);

        AtomicReference<ProviderManagerDeployer> reference = (AtomicReference<ProviderManagerDeployer>) privateField.get(pmr);
        if (reference.get() == null) {
            logger.error("hackIt AtomicReference<ProviderManagerDeployer> reference not here!!!!");
            return null;
        }
        ProviderManagerDeployer providerManagerDeployer = reference.get();
        return (DefaultKeycloakSessionFactory) providerManagerDeployer;
    }


}
