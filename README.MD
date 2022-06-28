$ ./gradlew clean build

move lib/build/libs/keycloak-email-plugin.jar -> docker/keycloak/provider/keycloak-email-plugin.jar

$ cd docker

docker-compose -f docker-compose-linux.yml up

http:/localhost/auth  -keycloak