# Perform CI pipeline steps
ci:
    clojure -T:build ci

# Use clj-kondo to lint
lint:
    clojure -T:build lint

# Use antq to check for outdated dependencies
outdated:
    clojure -T:build outdated

# Run vulnerability scan of dependency tree
vulnerability-scan:
    clojure -M:clj-watson scan scan -p deps.edn

# Runs application's main
run:
    clojure -M:run-m

# Package application as a standalone jar.
package:
    clojure -T:build package

# Run cljfmt check
cljfmt-check:
    clojure -T:build cljfmt-check

# Run cljfmt fix
cljfmt-fix:
    clojure -T:build cljfmt-fix
