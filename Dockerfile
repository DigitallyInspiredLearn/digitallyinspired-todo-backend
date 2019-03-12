FROM openjdk:8-jre-alpine

WORKDIR /usr/todo

COPY . .

CMD ["/usr/bin/java", "java", "-jat", "TodoList-0.0.1-SNAPSHOT.jar"]
