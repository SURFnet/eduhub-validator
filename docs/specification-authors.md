# For Eduhub specification authors

This document is intended for authors of the Open Education
Specification and related profiles.

Before writing your own profiles and rules take a look at the
available configuration files in the [config](../config) directory.

# Open Education Specification Profiles

The Open Education Specification has profiles for different consumers
(use cases). We describe the profiles as variants of the general
specification, using standard OpenAPI documents.

Profiles are created using
[merge-yaml-tree](https://git.sr.ht/~jomco/merge-yaml-tree). 

This tool takes two directory trees with YAML files and generates a
merged tree.  This can be used to create subsets of the [Open
Education
Specification](https://github.com/open-education-api/specification/tree/master/v5).

The validator in this repository expects a JSON formatted single-file
specification. To generate a JSON version of a YAML directory tree you
can use the Redocly command line tool:

```sh
npx @redocly/openapi-cli bundle --ext=json spec.yaml --force >profile.json
```

Where `spec.yaml` is the root YAML document.

In this repository, the resulting JSON specifications are added to the
[config](../config) directory.

# Rules files

An OpenAPI specification alone is not enough to index a service. The
spider also uses rules. These provide seed requests, and rules that
generate new requests based on the previous response.

Rules files are `edn` maps and have two keys:

- `:seeds` - a list of request maps. Request maps need at least a
  `:method` and a `:path` key. Seeds represent the initial requests
  for spidering. There must be at least one seed in order to spider an
  endpoint.

- `:rules` - a list of rule maps. A rule will `:match` an interaction
  map and `:generates` one or more request maps.

When matching or generating requests and responses are described as
EDN maps. These follow the [RING Request and Response
Map](https://github.com/ring-clojure/ring/blob/master/SPEC#L44)
format.

## Matching

A rule will match a particular interaction if every clause in the
`match` list matches. A clause represents a path of literal values and
placeholders. When placeholders are used, rules can match multiple
times.  This `generates` a list of requests from a template that can
use the placeholders:

```clojure
{:match     [[:request :method "get"]
             [:request :path ?path]
             [:response :status 200]
             [:response :body "pageNumber" ?pageNumber]
             [:response :body "hasNextPage" true]]
 :generates [{:method "get"
              :path   "{?path}?pageNumber={(inc ?pageNumber)}"}]}
```

Literal entries are integers, keywords (starting with `:`) and quoted
strings. Placeholders are symbols (identifiers) starting with a `?`.

In the above example we have a match if all of the following hold:

- The request is a GET request, with the path matching placeholder
  `?path`
- The response has 200 OK status
- The response body has a field `"pageNumber"` stored in placeholder
  `?pageNumber`
- Response body has a field `"hasNextPage"` with value `true`.

This will generate one GET request on the same `?path` with a
`pageNumber` parameter that is one more than the `?pageNumber` in the
interaction's response.

The reponse body is automatically parsed as json if it has the correct
content type.

Placeholders appearing multiple times in set of `match` clauses are
unified; clauses only match when their placeholders have the same
value.  This can be used to match multiple entries in a list:

```clojure
{:match     [[:request :method "get"]
             [:request :path "/customers"]
             [:response :status 200]
             [:response :body "customers" ?index "id" ?id]
             [:response :body "customers" ?index "name" ?name]]

 :generates [{:method "get"
              :path   "/customer?id={?id}&name={?name}"}]}
```

If the interaction response body contains a `"customers"` list of maps
with `"id"` and `"name"` attributes, this will generate a request for
every customer in `"customers"`, with their name and id. The `?index`
placeholder here is used to unify two clauses so that `?id` and
`?name` match in the same customer map.

## Generating with templates

A generator template looks like a RING request map:

```clojure
{:method "get"
 :path   "/some/path"
 :headers {"X-Header" "Header-value"}}
```

You can insert expressions in the template by using `{...}`
brackets. Placeholders are available in expressions.  S-expressions
can be used for function calls.

The following functions are available in expressions:

- `(inc EXPR)` increases EXPR by one
- `(dec EXPR)` decreases EXPR by one
- `(+ A B)` add A to B
- `(- A B)` subtract B from A
- `(not EXPR)` boolean not
- `(and A B)` true when both A and B are not false
- `(or A B)` true when either A and B are not false
- `(if A B C)` evaluate B when A is not false, otherwise evaluate C
- `(= A B)` true if A equals B

## See also

[https://git.sr.ht/~jomco/spider](https://git.sr.ht/~jomco/spider) -
the spidering and rules implementation.

[https://git.sr.ht/~jomco/openapi-v3-validator](https://git.sr.ht/~jomco/openapi-v3-validator) -
the OpenAPI validation implementation.