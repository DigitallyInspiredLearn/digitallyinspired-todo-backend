FROM openjdk:11-jre-slim

WORKDIR /usr/todo

COPY . .

CMD ["/usr/bin/java", "-jar", "target/TodoList-0.0.1-SNAPSHOT.jar"]
