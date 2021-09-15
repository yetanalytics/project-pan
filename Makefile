.PHONY: clean test

clean:
	rm -rf target
	rm -rf cljs-test-runner-out

test-clj:
	clojure -A:test:runner-clj

test-cljs:
	clojure -A:test:runner-cljs

ci: test-clj test-cljs

