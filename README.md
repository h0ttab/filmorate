# Filmorate

Filmorate is a REST API service for rating movies, leaving reviews, and finding friends with similar cinematic tastes. 

This is an educational project developed collaboratively by a team of 4 people. The main goal of the project was to practice Java backend development, relational database design, raw SQL/JDBC usage, and team collaboration via Git.

## Features
* **User Management:** Create, update, and delete users. Add/remove friends, view common friends.
* **Movie Catalog:** Manage movies, genres, MPA ratings, and directors.
* **Interactions:** Like movies, write text reviews, and rate other users' reviews (useful/useless).
* **Recommendations:** Get movie recommendations based on a basic collaborative filtering algorithm (intersection of likes).
* **Activity Feed:** Track recent actions (likes, reviews, new friends) of a user.

## My Contribution
In this team project, I was responsible for the following tasks:
* **Search Functionality:** Implemented dynamic movie search by title and/or director.
* **Directors Domain:** Developed the full CRUD and database relationships for movie directors, including sorting movies by year or popularity.
* **Cascading Deletions:** Added safe deletion of users and movies, ensuring all related data (likes, friendships, reviews) is properly cleared from the DB.
* **Query Optimization:** Solved the N+1 query problem by implementing an attribute enrichment method (`addAttributes`) that fetches genres, directors, and likes in batches using SQL `IN` clauses.

## Tech Stack & Skills Applied
* **Language:** Java 21
* **Framework:** Spring Boot 3.5 (Web, Validation)
* **Database:** H2 Database. We deliberately avoided ORM (Hibernate) and used `NamedParameterJdbcTemplate` with raw SQL queries to practice database interactions and optimizations.
* **Mapping:** MapStruct
* **Testing:** JUnit 5, Spring Boot Test, WebTestClient
* **Tools:** Docker, GitHub Actions (Checkstyle, SpotBugs)

## How to Run

You can run the application using Docker. It requires no local Java or database installation.

1. Clone the repository:
   ```bash
   git clone https://github.com/h0ttab/filmorate.git
   cd filmorate
   ```
2. Build and start the container:
   ```bash
   docker build -t filmorate .
   docker run -p 8080:8080 filmorate
   ```

## API Documentation
Once the application is running, you can explore the endpoints and test the API using the built-in Swagger UI:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**