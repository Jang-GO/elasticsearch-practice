# 1. 사용할 공식 Elasticsearch 이미지 버전을 명시합니다.
# 반드시 실제 사용 중인 버전과 일치시켜 주세요. (예: 8.14.1)
FROM docker.elastic.co/elasticsearch/elasticsearch:8.14.1

# 2. nori 형태소 분석기 플러그인을 설치합니다.
RUN bin/elasticsearch-plugin install analysis-nori

# 3. 동의어 파일을 컨테이너 안으로 복사하면서 소유자를 elasticsearch 유저로 지정합니다.
COPY --chown=elasticsearch:elasticsearch src/main/resources/analysis/telecom_synonyms.txt /usr/share/elasticsearch/config/analysis/