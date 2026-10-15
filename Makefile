.PHONY: clean test

clean:
	rm -rf target
	rm -rf cljs-test-runner-out

.PHONY: pom.xml

# Generate the base library's Maven manifest for GitHub dependency submission.
pom.xml:
	clojure -X:deps mvn-pom :user nil

test-clj:
	clojure -A:test:runner-clj

test-cljs:
	clojure -A:test:runner-cljs

ci: test-clj test-cljs

