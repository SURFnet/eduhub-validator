#!/bin/sh

if bb -e '(System/exit 0)' 2>/dev/null; then
    RUNTIME="bb"
else
    RUNTIME="clojure -M"
fi

$RUNTIME -m nl.jomco.eduhub-validator.report -o ooapi-rio.json "$@"
