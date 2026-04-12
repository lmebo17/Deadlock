FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache bash
RUN adduser -D -u 1000 sandbox
COPY runner.sh /runner.sh
RUN chmod +x /runner.sh
USER sandbox
ENTRYPOINT ["/runner.sh"]
