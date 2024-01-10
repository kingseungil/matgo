#! /bin/bash

cd ../docker

# nginx container가 없으면 실행
if [ $(docker ps | grep -c "matgo-proxy") -eq 0 ]; then
  echo "### Starting Nginx ###"
  docker-compose up -d matgo-proxy
else
  echo "### Nginx already running ###"
fi

# db container가 없으면 실행
if [ $(docker ps | grep -c "matgo-db") -eq 0 ]; then
  echo "### Starting database ###"
  docker-compose up -d matgo-db
else
  echo "### Database already running ###"
fi
echo "pwd: $(pwd)"
# redis container가 없으면 실행
if [ $(docker ps | grep -c "matgo-redis") -eq 0 ]; then
  echo "### Starting redis ###"
  docker-compose up -d matgo-redis
else
  echo "### Redis already running ###"
fi

# es container가 없으면 실행
if [ $(docker ps | grep -c "matgo-es") -eq 0 ]; then
  echo "### Starting elasticsearch ###"
  docker-compose up -d matgo-es
else
  echo "### Elasticsearch already running ###"
fi

echo

IS_BLUE=$(docker ps | grep -c "blue")

if [ "$IS_BLUE" -eq 1 ]; then
  echo "### BLUE => GREEN ###"

  echo "1. green container up"
  docker-compose up -d matgo-server-green

  echo "Waiting for the green application to fully start..."
  sleep 45

  echo "2. reload nginx"
  cd /root/matgo/docker/nginx || exit
  sed -i "s/server matgo-server-blue:8080/server matgo-server-green:8080/" nginx.conf
  docker-compose exec matgo-proxy nginx -s reload

  MAX_ATTEMPTS=10
  ATTEMPTS=0

  while [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
    echo "3. green container health check"
    sleep 3

    REQUEST=$(curl http://115.85.180.17/)
    if [ -n "$REQUEST" ]; then
      echo "4. green container health check success"
      break
    fi

    ATTEMPTS=$((ATTEMPTS+1))

    if [ $ATTEMPTS -eq $MAX_ATTEMPTS ]; then
      echo "Green health check failed after $MAX_ATTEMPTS attempts. Reverting Nginx configuration."
      sed -i '' "s/server matgo-server-green:8080/server matgo-server-blue:8080/" nginx.conf
      docker-compose exec matgo-proxy nginx -s reload
      exit 1
    fi
  done

  echo "5. blue container down"
  docker-compose stop matgo-server-blue
else
  echo "### GREEN => BLUE ###"

  echo "1. blue container up"
  docker-compose up -d matgo-server-blue

  echo "Waiting for the blue application to fully start..."
  sleep 45

  echo "2. reload nginx"
  cd /root/matgo/docker/nginx || exit
  sed -i "s/server matgo-server-green:8080/server matgo-server-blue:8080/" nginx.conf
  docker-compose exec matgo-proxy nginx -s reload

  MAX_ATTEMPTS=10
  ATTEMPTS=0

  while [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
    echo "3. blue container health check"
    sleep 3

    REQUEST=$(curl http://115.85.180.17/)
    if [ -n "$REQUEST" ]; then
      echo "4. blue container health check success"
      break
    fi

    ATTEMPTS=$((ATTEMPTS+1))

    if [ $ATTEMPTS -eq $MAX_ATTEMPTS ]; then
      echo "Blue health check failed after $MAX_ATTEMPTS attempts. Reverting Nginx configuration."
      sed -i "s/server matgo-server-blue:8080/server matgo-server-green:8080/" nginx.conf
      docker-compose exec matgo-proxy nginx -s reload
      exit 1
    fi
  done

  echo "5. green container down"
  docker-compose stop matgo-server-green
fi