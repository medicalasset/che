# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - Initial implementation
#
# Prerequisite : typescript library compiled
# see ../lib/typescript/README.md folder
#
# To build, see README.md
#
# To use it:
#  `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock codenvy/che-test <post-check> [URL of Che/Codenvy] [login] [password]`

FROM mhart/alpine-node

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.6.0

RUN set -x \
    && apk add --no-cache \
    ca-certificates \
    curl \
    openssl \
    && curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
    > /usr/bin/docker; chmod +x /usr/bin/docker


COPY lib/typescript/lib /lib
COPY lib/typescript/dependencies/runtime/node_modules /lib/node_modules
COPY lib/typescript/bin /bin

ENTRYPOINT ["/bin/che", "che-test"]
