#!/usr/bin/env bash

# Install clojure and babashka to `./bin` and `./lib`

set -ex

CLOJURE_VERSION="1.11.1.1273"
BABASHKA_VERSION="1.3.188"
BABASHKA_ARCH="linux-amd64"

if [ ! -x "bin/clojure" ]; then
    F="linux-install-${CLOJURE_VERSION}.sh"
    curl -O "https://download.clojure.org/install/${F}"
    bash "${F}" -p "$(pwd)"
    rm -f "${F}"
fi

if [ ! -x "bin/bb" ]; then
    F="babashka-${BABASHKA_VERSION}-${BABASHKA_ARCH}.tar.gz"
    curl -LO "https://github.com/babashka/babashka/releases/download/v${BABASHKA_VERSION}/${F}"
    tar -zxf "${F}" -C bin
    rm -f "${F}"
fi
