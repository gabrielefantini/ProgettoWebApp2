docker network create app-tier --driver bridge
docker run -d --name zookeeper-server \
    --network app-tier \
    -e ALLOW_ANONYMOUS_LOGIN=yes \
    bitnami/zookeeper:latest
docker run -d --name kafka-server \
    --network app-tier \
    -e ALLOW_PLAINTEXT_LISTENER=yes \
    -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 \
    -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=EXTERNAL:PLAINTEXT \
    -e KAFKA_CFG_LISTENERS=EXTERNAL://:9092 \
    -e KAFKA_CFG_ADVERTISED_LISTENERS=EXTERNAL://localhost:9092 \
    -e KAFKA_INTER_BROKER_LISTENER_NAME=EXTERNAL \
    -p 9092:9092 \
    bitnami/kafka:latest
