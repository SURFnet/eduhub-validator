<!-- WARNING! THIS FILE IS GENERATED, EDIT README.src.md INSTEAD -->
# SURFeduhub API validator

A command-line tool to spider and validate [Open Education
API](https://openonderwijsapi.nl/) endpoints to ensure compatibility
with services in
[SURFeduhub](https://www.surf.nl/surfeduhub-veilig-uitwisselen-van-onderwijsdata).

This tool is intended for developers of OOAPI endpoints at educational
institutions or their software suppliers.

## Downloading a release

This repository contains the source code & configuration of the
validator. If you only need to run the builtin validations, download
the latest build for your platform from [the Releases
page](https://github.com/SURFnet/eduhub-validator/releases).

The released builds contain a standalone binary `eduhub-validator`
that has the configuration for multiple OOAPI profiles builtin.

## SYNOPSIS

<!-- INCLUDE USAGE HERE -->
```
Usage: eduhub-validator [OPTIONS] [SEED...]

OPTIONS:
  -u, --base-url BASE-URL                                 Base URL of service to validate.
  -o, --observations OBSERVATIONS-PATH  observations.edn  Path to read/write spidering observations.
  -p, --report REPORT-PATH              report.html       Path to write report.
  -r, --profile PROFILE                 ooapi             Path to profile or name of builtin profile
  -S, --no-spider                       false             Disable spidering (re-use observations from OBSERVATIONS-PATH).
  -P, --no-report                       false             Disable report generation (spidering will write observations).
  -h, --add-header 'HEADER: VALUE'      {}                Add header to request. Can be used multiple times.
  -b, --bearer-token TOKEN              nil               Add bearer token to request.
  -M, --max-total-requests N            Infinity          Maximum number of requests.
  -m, --max-requests-per-operation N    Infinity          Maximum number of requests per operation in OpenAPI spec.
  -v, --version                                           Print version and exit.
      --help                                              Print usage information and exit.
  -a, --basic-auth 'USER:PASS'          nil               Send basic authentication header.

SEEDs are full URLs matching BASE-URL, or paths relative to BASE-URL.

If SEEDs are not provided, uses seeds in profile.

To validate all reachable paths from a service, use
eduhub-validator --profile=rio --base-url=http://example.com

To validate one specific path, use
eduhub-validator --profile=rio --base-url=http://example.com -M1 '/courses/some-course-id'
```

## For OOAPI endpoint developers

The eduhub-validator binary contains a set of profiles which can be
used to validate an OpenAPI endpoint for specific use cases.

Endpoints are not required to implement every path in the
specification, 

Validating an endpoint works in two steps:

  - Spidering the endpoint and validating the responses. This will
    create a large file with "observations"; a sequence of
    request/response pairs and the associated validation issues.
    
  - Aggregating the observations into a readable HTML report.
  
## Spidering an endpoint directly

```sh
eduhub-validator --base-url https://your-endpoint/
```

This will exhaustively index your endpoint paths, validate against the
default `rio` profile and write a report to `report.html` which can be
opened using any web browser.

The intermediate validation results are written to
`observations.edn`. This file is in [EDN
format](https://github.com/edn-format/edn) which is similar to JSON
and can be read as text, but it will probably be very large.

## Spidering via gateway

To run the spider through the SURFeduhub gateway, you can use the
`--basic-auth` and `--headers` options:

```sh
eduhub-validator \
  --profile rio
  --base-url https://gateway.test.surfeduhub.nl/ \
  --basic-auth USERNAME:PASS \
  --add-header 'x-route: endpoint=demo04.test.surfeduhub.nl' \
  --add-header 'accept: application/json; version=5' \
  --add-header 'x-envelope-response: false'
```

## Available SURFeduhub profiles

A few SURFeduhub profiles are available in the [profiles](./profiles)
directory and are built into the binary releases:

  - `ooapi` -- the full OOAPI v5 specification
  - `rio` -- the RIO profile of OOAPI v5.
  - `eduxchange` -- the eduxchange profile of OOAPI v5.

# Extending the validator

## Common validator source

The SURFeduhub Validator is a specialzed build of the [Apie 🙈 OpenAPI
Service Validator](https://github.com/SURFnet/apie). This repository
contains the SURFeduhub configuration and build tooling to create a
standalone validator for SURFeduhub.

## Prerequisites for running/building from source

Building requires a Clojure runtime. You can install either
[Babashka](https://github.com/babashka/babashka#installation) for a
standalone environment with quick startup time and slightly slower
runtime, or the full [Clojure
installation](https://clojure.org/guides/install_clojure) which
requires Java and is slower to start. For generating a standalone
validator you need Babashka.

The `validator` script in the root of the repository will use Babashka
if `bb` is on the PATH, and `clojure` otherwise.

## For specification authors

Information about writing specification profiles can be found in
[docs/specification-authors.md](./docs/specification-authors.md).

# Reporting vulnerabilities

If you have found a vulnerability in the code, we would like to hear
about it so that we can take appropriate measures as quickly as
possible. We are keen to cooperate with you to protect users and
systems better. See https://www.surf.nl/.well-known/security.txt for
information on how to report vulnerabilities responsibly.
