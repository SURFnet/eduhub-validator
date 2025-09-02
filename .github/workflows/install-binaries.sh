#!/usr/bin/env bash

# Install clojure and babashka to `./bin` and `./lib`

set -ex

if [ ! -x "${HOME}/bin/clojure" ]; then
    curl -L https://github.com/clojure/brew-install/releases/latest/download/posix-install.sh > install-clojure.sh
    chmod +x install-clojure.sh
    sudo ./install-clojure.sh
    rm install-clojure.sh
fi

if [ ! -x "${HOME}/bin/bb" ]; then
    curl -L "https://raw.githubusercontent.com/babashka/babashka/master/install" > install-bb.sh
    chmod +x install-bb.sh
    sudo ./install-bb.sh
    rm install-bb.sh
fi
