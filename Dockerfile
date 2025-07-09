# Dockerfile
FROM docker.elastic.co/elasticsearch/elasticsearch:8.14.1

# Korean nori analyzer 플러그인 설치
RUN elasticsearch-plugin install --batch analysis-nori