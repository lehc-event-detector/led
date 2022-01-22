FROM node:15.14.0-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

# RUN npm run build

EXPOSE 8888

CMD npm run start & npm run serve