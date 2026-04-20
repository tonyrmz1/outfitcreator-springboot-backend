# OutfitCreator Backend

The OutfitCreator backend is developed using several key technologies that enhance its performance and maintainability. Below is an overview of the main components used in this project:

## Technologies Used
- **Spring Boot**: A framework that simplifies the setup and development of new Spring applications.
- **Hibernate**: An Object-Relational Mapping (ORM) tool for managing database interactions.
- **MySQL**: The database management system used to store application data.
- **Spring Security**: A framework that provides authentication and authorization to secure the application.

## Getting Started

To start working on the OutfitCreator backend, follow these steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/tonyrmz1/outfitcreator-springboot-backend.git
   cd outfitcreator-springboot-backend
   ```

2. **Set Up the Database**:
   Ensure that MySQL is installed and running. Create a database for the application and update the `application.properties` file with your database configurations.

3. **Build the Project**:
   Use Maven to build the project:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   You can run the application using:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the API**:
   Once the application is running, you can access the API at `http://localhost:8080/api`. 

For further documentation and API details, refer to the project's Wiki or code comments.