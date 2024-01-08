#!/bin/bash

if [[ -f .env ]]; then
  export $(cat .env | grep -v '^#' | xargs)
fi

VERSION="1.0.1"
HUB_USER="$MY_HUB_ID"

cd ..

echo "Building image : api"
./gradlew clean build -x test
#./gradlew build
echo "Dockerfile을 이용하여 이미지 생성"
docker build -f docker/api/Dockerfile -t "matgo-api:$VERSION" .
echo "생성된 이미지를 docker hub에 업로드"
docker tag "matgo-api:$VERSION" "$HUB_USER/matgo-api:$VERSION"
echo "docker push"
docker push "$HUB_USER/matgo-api:$VERSION"

echo "image built successfully."