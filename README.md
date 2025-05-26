# OAuth2 Authentication with Spring Boot and React

This project demonstrates OAuth2 authentication using Google as the provider. The backend is built with Java Spring Boot, and the frontend is a React application.

## Project Structure

```
/
├── oauth2-auth-service/  # Spring Boot backend application
│   ├── src/
│   └── pom.xml
├── frontend/             # React frontend application
│   ├── public/
│   ├── src/
│   └── package.json
└── README.md
```

## Prerequisites

*   Java JDK (version 17 or later recommended)
*   Maven (usually bundled with Spring Boot or your IDE)
*   Node.js and npm (for the React frontend)
*   A Google Cloud Platform project with OAuth 2.0 credentials (Client ID and Client Secret)

## Backend Setup (Spring Boot - `oauth2-auth-service`)

1.  **Configure Google OAuth2 Credentials:**
    *   Navigate to `oauth2-auth-service/src/main/resources/application.properties`.
    *   Replace the placeholder values for `YOUR_GOOGLE_CLIENT_ID` and `YOUR_GOOGLE_CLIENT_SECRET` with your actual Google OAuth2 credentials:
        ```properties
        spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
        spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
        spring.security.oauth2.client.registration.google.scope=openid,profile,email
        ```
    *   **Important Security Note:** Do not commit your actual client secret to a public repository. Use environment variables or a secure secrets management system for production.

2.  **Build the Backend:**
    *   Navigate to the `oauth2-auth-service` directory:
        ```bash
        cd oauth2-auth-service
        ```
    *   Build the project using Maven:
        ```bash
        ./mvnw clean package 
        # or if you don't have the wrapper: mvn clean package
        ```

3.  **Run the Backend:**
    *   After a successful build, run the application:
        ```bash
        java -jar target/oauth2-auth-service-0.0.1-SNAPSHOT.jar 
        # The jar file name might vary based on your artifactId and version
        ```
    *   Alternatively, you can run directly using the Maven Spring Boot plugin (from the `oauth2-auth-service` directory):
        ```bash
        ./mvnw spring-boot:run
        ```
    *   The backend will start on `http://localhost:8080`.

## Frontend Setup (React - `frontend`)

1.  **Navigate to the frontend directory:**
    ```bash
    cd frontend 
    # If you are at the root, otherwise adjust path accordingly
    ```

2.  **Install Dependencies:**
    *   If you haven't already, or if dependencies have changed:
        ```bash
        npm install
        ```

3.  **Run the Frontend:**
    *   Start the React development server:
        ```bash
        npm start
        ```
    *   The frontend will open in your browser, usually at `http://localhost:3000`.

## How It Works

*   The React frontend runs on `localhost:3000` and is configured to proxy API requests to the Spring Boot backend running on `localhost:8080`. This is set up in `frontend/package.json` via the `"proxy": "http://localhost:8080"` line.
*   When you click the "Login with Google" button (assuming frontend components were fully implemented):
    *   The frontend redirects to `/oauth2/authorization/google` on the backend.
    *   The Spring Boot backend handles the OAuth2 flow with Google.
    *   Upon successful authentication, Google redirects back to a callback URL handled by the backend.
    *   The backend then (typically) establishes a session and redirects the user to a frontend path (e.g., `/authenticated` or `/profile`).
*   The frontend's `AuthContext` (if implemented) would then fetch user details from the backend's `/api/user` endpoint.
*   Logging out involves clearing the frontend state and making a request to the backend's `/logout` endpoint, which invalidates the Spring Security session.

## Available API Endpoints (Backend)

*   `GET /`: Public welcome page.
*   `GET /api/user`: Returns authenticated user's information (name, email, attributes). Requires login.
*   `/oauth2/authorization/google`: Initiates Google OAuth2 login (handled by Spring Security).
*   `/login`: Default Spring Security login page (may redirect to Google).
*   `/logout`: Logs the user out (handled by Spring Security).

## Development Notes

*   Ensure your Google Cloud OAuth 2.0 consent screen is configured and your authorized redirect URIs in Google Cloud Console include `http://localhost:8080/login/oauth2/code/google`. This is the default redirect URI pattern for Spring Security's OAuth2 client with the registration ID "google".
*   Due to skipping Step 5 ("Implement frontend components for authentication in the React app"), the React frontend will not have functional login/profile pages. The basic structure and proxy are in place, but UI components for interacting with the authentication system need to be built.

## Production Considerations

When deploying this application to a production environment, consider the following:

### Backend (Spring Boot)

1.  **Configuration Management:**
    *   **Credentials:** Do NOT hardcode your `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` in `application.properties` for production. Use environment variables, configuration server (like Spring Cloud Config), or secrets management services provided by your cloud provider (e.g., AWS Secrets Manager, Google Secret Manager, Azure Key Vault).
        *   Example using environment variables:
            ```properties
            spring.security.oauth2.client.registration.google.client-id=\${GOOGLE_CLIENT_ID}
            spring.security.oauth2.client.registration.google.client-secret=\${GOOGLE_CLIENT_SECRET}
            ```
    *   **Profiles:** Use Spring Profiles (e.g., `dev`, `prod`) to manage different configurations for different environments.

2.  **HTTPS:**
    *   Always use HTTPS in production. Configure your Spring Boot application to run behind a reverse proxy (like Nginx or Apache) that handles SSL termination, or configure SSL directly in the embedded server (Tomcat, Jetty, Undertow) if appropriate for your setup.

3.  **Database:**
    *   If you were to add features requiring a database (e.g., storing user preferences, application-specific user data), use a production-grade database (e.g., PostgreSQL, MySQL, Oracle) and configure connection pooling.

4.  **Logging:**
    *   Configure robust logging (e.g., using Logback or Log4j2) to output logs to files or a centralized logging system (e.g., ELK stack, Splunk). Set appropriate log levels for production.

5.  **Packaging and Running:**
    *   Build an executable JAR: `./mvnw clean package`.
    *   Run the JAR: `java -jar target/oauth2-auth-service-0.0.1-SNAPSHOT.jar`.
    *   Consider containerizing the application (e.g., using Docker) for easier deployment and scaling.

### Frontend (React)

1.  **Build for Production:**
    *   Create an optimized static build of the React application:
        ```bash
        cd frontend
        npm run build
        ```
    *   This will create a `build` directory inside `frontend` with static assets.

2.  **Serving Static Files:**
    *   **Option 1 (Serve from Spring Boot):** Copy the contents of `frontend/build` into the `src/main/resources/static` directory of your Spring Boot application. Spring Boot will then serve the React app from the root path.
    *   **Option 2 (Separate Web Server/CDN):** Host the static files from `frontend/build` on a dedicated web server (like Nginx, Apache) or a Content Delivery Network (CDN) for better performance. In this case, you'll need to configure CORS on your Spring Boot backend to allow requests from the frontend's domain.

3.  **HTTPS:**
    *   Ensure the frontend is also served over HTTPS.

4.  **API Endpoint Configuration:**
    *   The `proxy` setting in `package.json` is for development only. In production, your frontend will make API calls to the absolute URL of your backend (e.g., `https://api.yourdomain.com/api/user`). This might require configuring environment variables for your React app at build time or having a runtime configuration file.

### General

*   **Security Headers:** Implement security-related HTTP headers (e.g., `Content-Security-Policy`, `X-Content-Type-Options`, `Strict-Transport-Security`) usually via your reverse proxy.
*   **Monitoring and Alerting:** Set up monitoring for your application (CPU, memory, response times, error rates) and configure alerts for critical issues.
*   **Data Backup and Recovery:** If storing data, have a robust backup and recovery plan.
```
