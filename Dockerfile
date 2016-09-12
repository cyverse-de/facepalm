FROM clojure:alpine

RUN apk add --update git && \
    rm -rf /var/cache/apk

ARG git_commit=unknown
ARG version=unknown
LABEL org.iplantc.de.facepalm.git-ref="$git_commit" \
      org.iplantc.de.facepalm.version="$version"

ARG branch=dev

COPY . /usr/src/app

RUN git clone https://github.com/cyverse-de/de-db.git /de-db && \
    cd /de-db && \
    git checkout $branch && \
    ./build.sh && \
    mv database.tar.gz /usr/src/app/database.tar.gz

RUN git clone https://github.com/cyverse-de/metadata-db.git /metadata-db && \
    cd /metadata-db && \
    git checkout $branch && \
    ./build.sh && \
    mv metadata-db.tar.gz /usr/src/app/metadata-db.tar.gz

RUN git clone https://github.com/cyverse-de/notifications-db.git /notifications-db && \
    cd /notifications-db && \
    git checkout $branch && \
    ./build.sh && \
    mv notification-db.tar.gz /usr/src/app/notification-db.tar.gz

RUN git clone https://github.com/cyverse-de/permissions-db.git /permissions-db && \
    cd /permissions-db && \
    git checkout $branch && \
    ./build.sh && \
    mv permissions-db.tar.gz /usr/src/app/permissions-db.tar.gz

WORKDIR /usr/src/app

RUN lein uberjar && \
    cp target/facepalm-standalone.jar .

RUN ln -s "/usr/bin/java" "/bin/facepalm"

ENTRYPOINT ["facepalm", "-jar", "facepalm-standalone.jar"]
CMD ["--help"]
