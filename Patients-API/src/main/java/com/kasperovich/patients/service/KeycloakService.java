package com.kasperovich.patients.service;

import com.kasperovich.patients.exception.KeycloakException;
import com.kasperovich.patients.security.KeycloakProperties;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Service
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class KeycloakService {

    private final Keycloak keycloak;

    private final KeycloakProperties keycloakProperties;

    public KeycloakService(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloakProperties.getUsername())
                .password(keycloakProperties.getPassword())
                .build();
    }


    public String createPatient(String username, String password) throws KeycloakException {
        // Create a user
        Response response = createUser(username, password);
        //Extract the user id from the response
        String userId = extractIdFromResponse(response);
        // Assign a role to the user
        assignRoleToAUser(userId, "Patient");
        // Assign a scope to the user
        assignScopeToAUser(userId, "read");
        // Return the created user id
        return userId;
    }

    private Response createUser(String username, String password) throws KeycloakException {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);
        credentials.setTemporary(false);
        user.setCredentials(List.of(credentials));

        log.debug("Checking if user with this username exists: {}", username);

        if(!keycloak.realm(keycloakProperties.getRealm()).users().search(username).isEmpty()){
            throw new KeycloakException.RecurringException("User with this username already exists: " + username);
        };

        log.debug("Creating Keycloak user: {}", user);
        Response response;
        try {
            response = keycloak.realm(keycloakProperties.getRealm()).users().create(user);
            log.debug("Keycloak user created: " + username);
        } catch (Exception e) {
            throw new KeycloakException("Error creating a user in Keycloak: " + username, e);
        }
        return response;
    }

    private String extractIdFromResponse(Response response) {
        return CreatedResponseUtil.getCreatedId(response);
    }


    private void assignRoleToAUser(String userId, String roleName) throws KeycloakException {


        // Fetch the role representation
        log.debug("Searching for a Keycloak realm role: " + roleName);
        RoleRepresentation role = keycloak.realm(keycloakProperties.getRealm())
                .roles()
                .get(roleName)
                .toRepresentation();

        if (role == null) {
            throw new KeycloakException("Role" + roleName + " not found in Keycloak");
        }

        // Assign role to user
        List<RoleRepresentation> roles = new ArrayList<>();
        roles.add(role);

        keycloak.realm(keycloakProperties.getRealm())
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(roles);

        log.debug("Assigned realm role {} to user {}", roleName, userId);
    }

    private void assignScopeToAUser(String userId, String scope) throws KeycloakException {

        var clientId = keycloakProperties.getTargetClientId();

        var realm = keycloakProperties.getRealm();

        ClientRepresentation client;
        try {
            log.debug("Searching for a Keycloak client: {}", clientId);
            client = keycloak
                    .realm(realm)
                    .clients()
                    .findByClientId(clientId)
                    .get(0);
        } catch (Exception e) {
            throw new KeycloakException("Client " + clientId + " not found in Keycloak", e);
        }

        // Fetch the client role representation
        log.debug("Searching for a Keycloak client role: {}", scope);
        RoleRepresentation role = keycloak.realm(realm)
                .clients()
                .get(client.getId())
                .roles()
                .get(scope)
                .toRepresentation();


        if (role == null) {
            throw new KeycloakException("Role " + scope + " not found in client " + clientId + " in Keycloak");
        }

        try {
            log.debug("Assigning client role {} to user {}", scope, userId);
            // Assign role to user
            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .clientLevel(client.getId())
                    .add(List.of(role));

            log.debug("Assigned client role {} to user {}", scope, userId);

        } catch (Exception e) {
            throw new KeycloakException("Failed to assign client role " + scope + " to user " + userId, e);
        }

    }

    public void deletePatientByUsername(String username) throws KeycloakException {
        log.debug("Searching Keycloak user for delete: {}", username);
        UserRepresentation user;
        try {
            user = keycloak.realm(keycloakProperties.getRealm()).users().search(username).get(0);
            log.debug("Found Keycloak user: {}", user);
        } catch (Exception e) {
            throw new KeycloakException("Error getting user from Keycloak: " + username, e);
        }

        log.debug("Deleting Keycloak user: {}", user);
        try {
            keycloak.realm(keycloakProperties.getRealm()).users().delete(user.getId());
            log.debug("Keycloak user deleted: " + username);
        } catch (Exception e) {
            throw new KeycloakException("Error deleting user from Keycloak: " + username, e);
        }
    }
}
