# build stage
FROM node:18.16.0-alpine3.16 as builder
WORKDIR /app
COPY ./frontend/package*.json ./
RUN  npm install
COPY ./frontend .

ARG ENV_FILE

COPY ./${ENV_FILE} ./.env
RUN npm run build

FROM nginx:1.23-alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY ./frontend/docker-assets/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3001
